package jp.osdn.gokigen.aira01c.camera.omds.status

import android.graphics.Color
import android.util.Log
import jp.osdn.gokigen.aira01c.camera.interfaces.ICameraStatus
import jp.osdn.gokigen.aira01c.camera.interfaces.ICameraStatusUpdateNotify
import jp.osdn.gokigen.aira01c.camera.interfaces.ICameraStatusWatcher
import jp.osdn.gokigen.aira01c.camera.interfaces.IMessageDrawer
import jp.osdn.gokigen.aira01c.camera.omds.IOmdsProtocolNotify
import jp.osdn.gokigen.aira01c.camera.utils.communication.SimpleHttpClient
import java.io.ByteArrayOutputStream
import java.lang.Exception
import java.net.Socket
import java.util.*
import kotlin.collections.ArrayList

class OmdsCameraStatusWatcher(userAgent: String = "OlympusCameraKit", private val executeUrl : String = "http://192.168.0.10") : ICameraStatusWatcher, ICameraStatus, IOmdsCommunicationInfo, IOmdsProtocolNotify
{
    private val headerMap: MutableMap<String, String> = HashMap()
    private val http = SimpleHttpClient()

    private var useOpcProtocol = false

    private var buffer: ByteArray? = null
    private var isWatching = false
    private var isWatchingEvent = false
    private var whileEventReceive = false
    private var statusReceived = false
    private var latestEventResponse : String = ""

    private var currentTakeMode = ""
    private var currentShutterSpeed = ""
    private var currentAperture = ""
    private var currentExpRev = ""
    private var currentCaptureMode = ""
    private var currentIsoSensitivity = ""
    private var currentWhiteBalance = ""
    private var currentPictureEffect = ""
    private var currentRemainBattery = ""
    private var currentFocusStatus = ""
    private var currentFocalLength = ""
    private var currentRemainShots = ""
    private var currentExposureWarning = ""
    private var currentFocusType = ""

    private var opcTakeModeSelectionList = ""
    private var opcWhiteBalanceSelectionList = ""
    private var opcColorToneSelectionList = ""
    private var opcDriveModeSelectionList = ""
    private var opcShutterSpeedSelectionList = ""
    private var opcApertureSelectionList = ""
    private var opcIsoSensitivitySelectionList = ""
    private var opcExpRevSelectionList = ""
    private var opcFocusModeSelectionList = ""

    override fun setOmdsCommandList(commandList: String)
    {
        startStatusWatch(null, null)
    }

    override fun startReceiveOpcEvent()
    {
        // OPCの場合は、、イベントも監視する
        startEventWatch()
    }

    fun setRtpHeader(byteBuffer: ByteArray?)
    {
        try
        {
            if (byteBuffer != null)
            {
                buffer = byteBuffer
                statusReceived = true
            }
            else
            {
                statusReceived = false
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
            statusReceived = false
        }
    }

    private fun startEventWatch(portNumber: Int = 65000)
    {
        if (whileEventReceive)
        {
            Log.v(TAG, "startReceiveStream() : already starting.")
            return
        }

        // イベント受信用の準備...
        finishEventReceiverThread()
        requestOpcEventWatch()

        // 受信スレッドを動かす
        val thread = Thread { eventReceiverThread(portNumber) }
        try
        {
            thread.start()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    private fun requestOpcEventWatch(portNo: Int = 65000)
    {
        try
        {
            // OPC機のイベント通知開始
            val eventWatchUrl = "$executeUrl/start_pushevent.cgi?port=$portNo"
            Log.v(TAG, " requestOpcEventWatch : $eventWatchUrl")
            val response = http.httpGetWithHeader(eventWatchUrl, headerMap, null, TIMEOUT_MS) ?: ""
            if (response.isNotEmpty())
            {
                dumpLog(eventWatchUrl, response)
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    private fun eventReceiverThread(portNumber: Int)
    {
        try
        {
            //finishEventReceiverThread()
            //requestOpcEventWatch()
            whileEventReceive = true
            val bufferSize = RECEIVE_BUFFER_SIZE
            val byteArray = ByteArray(bufferSize)
            val hostName = executeUrl.substring("http://".length)
            Log.v(TAG, " OPC: EVENT LISTEN : $hostName, $portNumber")
            val eventReceiveSocket = Socket(hostName, portNumber)
            val inputStream = eventReceiveSocket.getInputStream()
            while (whileEventReceive)
            {
                try
                {
                    sleep(SLEEP_EVENT_TIME_MS)
                    val dataBytes = inputStream.available()
                    if (dataBytes > 0)
                    {
                        // データがあった...受信する
                        Log.v(TAG, " RECEIVE OPC EVENT : $dataBytes bytes")
                        val byteStream = ByteArrayOutputStream()
                        var readIndex = 0
                        while (readIndex < dataBytes)
                        {
                            val readBytes = inputStream.read(byteArray, 0, bufferSize)
                            if (readBytes <= 0)
                            {
                                Log.v(TAG, " RECEIVED MESSAGE FINISHED ($dataBytes)")
                                break
                            }
                            readIndex += readBytes
                            byteStream.write(byteArray, 0, readBytes)
                        }
                        val dataString = byteStream.toString()
                        if (dataString.indexOf("<root><result>") >= 0)
                        {
                            if ((dataString.indexOf("ok") >= 0)&&(dataString.indexOf("<location>") >= 0))
                            {
                                // Focus Locked
                                Log.v(TAG, " FOCUS OK! ")
                            }
                            else if ((dataString.indexOf("ng") >= 0)||(dataString.indexOf("none") >= 0))
                            {
                                Log.v(TAG, " FOCUS NG... ")
                            }
                            else
                            {
                                Log.v(TAG, " --- RECEIVE OPC EVENT $dataString ---")
                            }
                        }
                        else
                        {
                            Log.v(TAG, " RECEIVE OPC EVENT $dataString")
                        }
                        //if (isDumpLog)
                        //{
                        //    SimpleLogDumper.dumpBytes("[RX EVT(OPC):$dataBytes]", byteStream.toByteArray())
                        //}
                    }
                    else
                    {
                        if (isDumpLog)
                        {
                            Log.v(TAG, " NOT RECEIVE OPC EVENT ...WAIT AGAIN...")
                        }
                    }
                    sleep(SLEEP_EVENT_TIME_MS)
                }
                catch (e: Exception)
                {
                    e.printStackTrace()
                    whileEventReceive = false
                    finishEventReceiverThread()
                }
            }
            //finishEventReceiverThread()
            System.gc()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    private fun finishEventReceiverThread()
    {
        try
        {
            Log.v(TAG, "finishEventReceiverThread()")

            // OPC機のイベント通知開始
            val eventWatchUrl = "$executeUrl/stop_pushevent.cgi"
            val response = http.httpGetWithHeader(eventWatchUrl, headerMap, null, TIMEOUT_MS) ?: ""
            if (response.isNotEmpty())
            {
                dumpLog(eventWatchUrl, response)
            }
         }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    override fun startStatusWatch(indicator: IMessageDrawer?, notifier: ICameraStatusUpdateNotify?)
    {
        try
        {
            startRtpStatusWatch()
            startEventStatusWatch()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    private fun startRtpStatusWatch()
    {
        try
        {
            Log.v(TAG, " startStatusWatch()")
            val thread = Thread {
                isWatching = true
                while (isWatching)
                {
                    if (statusReceived)
                    {
                        // データを解析する
                        parseRtpHeader()
                        statusReceived = false
                    }
                    sleep(SLEEP_TIME_MS)
                }
            }
            thread.start()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    private fun startEventStatusWatch()
    {
        try
        {
            Log.v(TAG, " startEventStatusWatch() : $useOpcProtocol")
            val thread = Thread {
                isWatchingEvent = true
                while (isWatchingEvent)
                {
                    // ----- EVENT POLLING
                    if (useOpcProtocol)
                    {
                        watchOpcStatus()
                    }
                    else
                    {
                        watchOmdsStatus()
                    }
                    sleep(SLEEP_EVENT_TIME_MS)
                }
            }
            thread.start()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    private fun watchOmdsStatus()
    {
        try
        {
            // OMDS機のイベント受信
            val omdsEventUrl = "$executeUrl/get_camprop.cgi?com=desc&propname=desclist"
            latestEventResponse = http.httpGetWithHeader(omdsEventUrl, headerMap, null, TIMEOUT_MS) ?: ""
            if (latestEventResponse.isNotEmpty())
            {
                dumpLog(omdsEventUrl, latestEventResponse)
                parseOmdsProperties(latestEventResponse)
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    private fun watchOpcStatus()
    {
        try
        {
            // OPC機のイベント受信
            val opcEventUrl = "$executeUrl/get_camprop.cgi?com=getlist"
            val postData = "<?xml version=\"1.0\"?><get><prop name=\"AE\"/><prop name=\"APERTURE\"/><prop name=\"BATTERY_LEVEL\"/><prop name=\"COLORTONE\"/><prop name=\"EXPREV\"/><prop name=\"ISO\"/><prop name=\"RECENTLY_ART_FILTER\"/><prop name=\"SHUTTER\"/><prop name=\"TAKEMODE\"/><prop name=\"TAKE_DRIVE\"/><prop name=\"WB\"/><prop name=\"AE_LOCK_STATE\"/><prop name=\"AF_LOCK_STATE\"/></get>"
            latestEventResponse = http.httpPostWithHeader(opcEventUrl, postData, headerMap, null, TIMEOUT_MS) ?: ""
            dumpLog(opcEventUrl, latestEventResponse)
            parseOpcProperties(latestEventResponse)
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    private fun parseOmdsProperties(eventResponse: String)
    {
        try
        {
            currentTakeMode = getPropertyValue(eventResponse, "<propname>takemode</propname>")
            currentWhiteBalance = "WB: " + decideWhiteBalance(getPropertyValue(eventResponse, "<propname>wbvalue</propname>"))
            currentPictureEffect = getPropertyValue(eventResponse, "<propname>colortone</propname>")
            currentCaptureMode = " DRIVE: " + getPropertyValue(eventResponse, "<propname>drivemode</propname>")
            //currentShutterSpeed = getPropertyValue(eventResponse, "<propname>shutspeedvalue</propname>")
            //currentAperture = "F" + getPropertyValue(eventResponse, "<propname>focalvalue</propname>")
            // currentIsoSensitivity = "ISO " + getPropertyValue(eventResponse, "<propname>isospeedvalue</propname>")
            // currentExpRev = getPropertyValue(eventResponse, "<propname>expcomp</propname>")
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    private fun decideWhiteBalance(wbValue: String) : String
    {
        try
        {
            return (when (wbValue)
            {
                "0" -> "AUTO"
                "18" -> "Daylight"
                "16" -> "Shade"
                "17" -> "Cloudy"
                "20" -> "Incandescent"
                "35" -> "Fluorescent"
                "64" -> "Underwater"
                "23" -> "Flash"
                "256" -> "WB1"
                "257" -> "WB2"
                "258" -> "WB3"
                "259" -> "WB4"
                "512" -> "CWB"
                else -> "($wbValue)"
            })
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
        return ("($wbValue)")
    }

    private fun decideWhiteBalanceValue(wbName: String) : String
    {
        try
        {
            return (when (wbName)
            {
                "AUTO" -> "0"
                "Daylight" -> "18"
                "Shade" -> "16"
                "Cloudy" -> "17"
                "Incandescent" -> "20"
                "Fluorescent" -> "35"
                "Underwater" -> "64"
                "Flash" -> "23"
                "WB1" -> "256"
                "WB2" -> "257"
                "WB3" -> "258"
                "WB4" -> "259"
                "CWB" -> "512"
                else -> "0"
            })
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
        return ("0")
    }

    private fun getPropertySelectionList(responseString: String, propertyString: String) : List<String>
    {
        try
        {
            if (responseString.isNotEmpty())
            {
                val propertyIndex = responseString.indexOf(propertyString)
                if (propertyIndex > 0)
                {
                    val propertyValueIndex =
                        responseString.indexOf("<enum>", propertyIndex) + "<enum>".length
                    val propertyValueLastIndex = responseString.indexOf("</enum>", propertyIndex)
                    val propertyListString =
                        responseString.substring(propertyValueIndex, propertyValueLastIndex)
                    if (propertyListString.isNotEmpty())
                    {
                        val propertyList = propertyListString.split(" ")
                        val selectionList: ArrayList<String> = ArrayList()
                        selectionList.addAll(propertyList)
                        return (selectionList)
                    }
                }
            }
            Log.v(TAG, "getPropertySelectionList($propertyString) $responseString ..." )
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
        return (ArrayList())
    }


    private fun getPropertyValue(responseString: String, propertyString: String) : String
    {
        try
        {
            val propertyIndex = responseString.indexOf(propertyString)
            if (propertyIndex > 0)
            {
                val propertyValueIndex = responseString.indexOf("<value>", propertyIndex) + "<value>".length
                val propertyValueLastIndex = responseString.indexOf("</value>", propertyIndex)
                return (responseString.substring(propertyValueIndex, propertyValueLastIndex))
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
        return ("")
    }

    private fun parseOpcProperties(eventResponse: String)
    {
        try
        {
            val takeMode = getPropertyValue(eventResponse, "<prop name=\"TAKEMODE\">")
            currentWhiteBalance = getPropertyValue(eventResponse, "<prop name=\"WB\">")
            currentPictureEffect = getPropertyValue(eventResponse, "<prop name=\"COLORTONE\">")
            currentCaptureMode = getPropertyValue(eventResponse, "<prop name=\"TAKE_DRIVE\">")

/*
            //val aeLockState = getPropertyValue(eventResponse, "<prop name=\"AE_LOCK_STATE\">")
            //val afLockState = getPropertyValue(eventResponse, "<prop name=\"AF_LOCK_STATE\">")
            if (afLockState != currentAfLockState)
            {
                // AFロック状態 or AEロック状態が変化したとき
                currentAfLockState = afLockState
                if (currentAfLockState == "LOCK")
                {
                    notifier?.updateFocusedStatus(true, false)
                }
                else
                {
                    notifier?.updateFocusedStatus(false, false)
                }
                //notifier?.updateFocusedStatus(focus, isError)
            }
*/

            if (takeMode != currentTakeMode)
            {
                currentTakeMode = takeMode

                // 撮影モードが変わったときには、選択肢を更新する
                updateOpcSelectionList(true)
            }
            else
            {
                // 撮影モードが変わらないときには、選択肢がない場合のみ補完する
                updateOpcSelectionList(false)
            }

        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    private fun updateOpcSelectionList(isResetList: Boolean = false)
    {
        if (isResetList)
        {
            opcTakeModeSelectionList = ""
            opcWhiteBalanceSelectionList = ""
            opcColorToneSelectionList = ""
            opcDriveModeSelectionList = ""
            opcShutterSpeedSelectionList = ""
            opcApertureSelectionList = ""
            opcIsoSensitivitySelectionList = ""
            opcExpRevSelectionList = ""
            opcFocusModeSelectionList = ""
        }

        if (opcTakeModeSelectionList.isEmpty())
        {
            updateOpcPropertyList("TAKEMODE", object: IPropertyListCallback {
                override fun receivedReply(replyValue: String) { opcTakeModeSelectionList = replyValue }
            })
        }

        if (opcWhiteBalanceSelectionList.isEmpty())
        {
            updateOpcPropertyList("WB", object: IPropertyListCallback {
                override fun receivedReply(replyValue: String) { opcWhiteBalanceSelectionList = replyValue }
            })
        }

        if (opcColorToneSelectionList.isEmpty())
        {
            updateOpcPropertyList("COLORTONE", object: IPropertyListCallback {
                override fun receivedReply(replyValue: String) { opcColorToneSelectionList = replyValue }
            })
        }

        if (opcDriveModeSelectionList.isEmpty())
        {
            updateOpcPropertyList("TAKE_DRIVE", object: IPropertyListCallback {
                override fun receivedReply(replyValue: String) { opcDriveModeSelectionList = replyValue }
            })
        }

        if (opcShutterSpeedSelectionList.isEmpty())
        {
            updateOpcPropertyList("SHUTTER", object: IPropertyListCallback {
                override fun receivedReply(replyValue: String) { opcShutterSpeedSelectionList = replyValue }
            })
        }

        if (opcApertureSelectionList.isEmpty())
        {
            updateOpcPropertyList("APERTURE", object: IPropertyListCallback {
                override fun receivedReply(replyValue: String) { opcApertureSelectionList = replyValue }
            })
        }

        if (opcIsoSensitivitySelectionList.isEmpty())
        {
            updateOpcPropertyList("ISO", object: IPropertyListCallback {
                override fun receivedReply(replyValue: String) { opcIsoSensitivitySelectionList = replyValue }
            })
        }

        if (opcExpRevSelectionList.isEmpty())
        {
            updateOpcPropertyList("EXPREV", object: IPropertyListCallback {
                override fun receivedReply(replyValue: String) { opcExpRevSelectionList = replyValue }
            })
        }
        if (opcFocusModeSelectionList.isEmpty())
        {
            updateOpcPropertyList("FOCUS_STILL", object: IPropertyListCallback {
                override fun receivedReply(replyValue: String) { opcFocusModeSelectionList = replyValue }
            })
        }
    }

    private fun updateOpcPropertyList(propertyName: String, callback : IPropertyListCallback)
    {
        try
        {
            val thread = Thread {

                // OPC機のイベント受信
                val propertyGetUrl = "$executeUrl/get_camprop.cgi?com=desc&propname=$propertyName"
                val selectionListResponse = http.httpGetWithHeader(propertyGetUrl, headerMap, null, TIMEOUT_MS) ?: ""
                dumpLog(propertyGetUrl, selectionListResponse)
                if (selectionListResponse.isNotEmpty())
                {
                    callback.receivedReply(selectionListResponse)
                }
            }
            thread.start()

        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    private fun dumpLog(header: String, data: String)
    {
        if (isDumpLog)
        {
            val dataStep = 1536
            Log.v(TAG, "     ------------------------------------------ ")
            for (pos in 0..data.length step dataStep) {
                val lastIndex = if ((pos + dataStep) > data.length)
                {
                    data.length
                }
                else
                {
                    pos + dataStep
                }
                Log.v(TAG, " $header ($pos/${data.length}) ${data.substring(pos, lastIndex)}")
            }
            Log.v(TAG, "     ------------------------------------------ ")
        }
    }


    private fun sleep(waitMs: Int)
    {
        try
        {
            Thread.sleep(waitMs.toLong())
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    private fun parseRtpHeader()
    {
        try
        {
            if (buffer == null)
            {
                Log.v(TAG, " parseRtpHeader() : null")
                return
            }
            var position = 16
            val maxLength = buffer?.size ?: 0
            if (maxLength <= 0)
            {
                // データがないので何もしない
                return
            }

            if (isDumpLog)
            {
                // 受信データのバッファをダンプする
                Log.v(TAG," parseRtpHeader size: $maxLength")
            }

            while (position + 4 < maxLength)
            {
                val commandId: Int = ((buffer?.get(position) ?: 0).toInt() and 0xff) * 256 + ((buffer?.get(position + 1) ?: 0).toInt() and 0xff)
                val length: Int = ((buffer?.get(position + 2) ?: 0).toInt() and 0xff) * 256 + ((buffer?.get(position + 3) ?: 0).toInt() and 0xff)
                when (commandId)
                {
                    ID_AF_FRAME_INFO -> {  }  // { checkFocused(buffer, position, length) }
                    ID_FRAME_SIZE -> { }
                    ID_MEDIA_INFO -> { }
                    ID_ROTATION_INFO -> { }
                    ID_AVAILABLE_SHOTS -> { }
                    ID_OMDS_UNKNOWN_01 -> { }
                    ID_OMDS_UNKNOWN_02 -> { }
                    ID_SHUTTER_SPEED -> { checkShutterSpeed(buffer, position, length)  }
                    ID_APERTURE -> { checkAperture(buffer, position, length) }
                    ID_EXPOSURE_COMPENSATION -> { checkExposureCompensation(buffer, position, length) }
                    ID_OMDS_UNKNOWN_03 -> { }
                    ID_ISO_SENSITIVITY -> { checkIsoSensitivity(buffer, position, length) }
                    ID_OMDS_UNKNOWN_04 -> { }
                    ID_OMDS_UNKNOWN_05 -> { }
                    ID_OMDS_UNKNOWN_06 -> { }
                    ID_EXPOSURE_WARNING -> { checkExposureWarning(buffer, position, length) }
                    ID_FOCUS_TYPE -> { checkFocusType(buffer, position, length) }
                    ID_ZOOM_LENS_INFO -> { }
                    ID_REMAIN_VIDEO_TIME -> { }
                    ID_POSITION_LEVEL_INFO -> { }
                    ID_FACE_DETECT_1 -> { }
                    ID_FACE_DETECT_2 -> { }
                    ID_FACE_DETECT_3 -> { }
                    ID_FACE_DETECT_4 -> { }
                    ID_FACE_DETECT_5 -> { }
                    ID_FACE_DETECT_6 -> { }
                    ID_FACE_DETECT_7 -> { }
                    else -> { }
                }
                position += 4 + length * 4  // header : 4bytes , data : length * 4 bytes
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

/*
    private fun checkFocused(buffer: ByteArray?, position: Int, length: Int)
    {
        // 結局 OPC機でも動いていなさそうなので、このロジックはお蔵入りとする
        if ((length != 5)||(buffer == null))
        {
            // データがそろっていないので何もしない
            return
        }
        val status: Int = (buffer[position + 11].toUInt()).toInt() and 0xff
        if (status != focusingStatus)
        {
            // ドライブ停止時には、マーカの色は消さない
            if (status > 0)
            {
                val focus = status == 1     // 合焦成功
                val isError = status == 2   // 合焦失敗
            }
            focusingStatus = status
        }
    }
*/

    private fun checkShutterSpeed(buffer: ByteArray?, position: Int, length: Int)
    {
        if ((length != 3)||(buffer == null))
        {
            // データがそろっていないので何もしない
            return
        }

        val numerator = ((((buffer[position + 12].toUInt()).toInt() and 0xff) * 256)) + ((buffer[position + 13].toUInt()).toInt() and 0x00ff)
        val denominator = ((((buffer[position + 14].toUInt()).toInt() and 0xff) * 256)) + ((buffer[position + 15].toUInt()).toInt() and 0x00ff)
        if ((numerator == 0)||(denominator == 0))
        {
            // 値が変なので、なにもしない
            return
        }
        currentShutterSpeed = if (numerator > denominator) {
            // 分子が大きい
            if (denominator == 1)  { String.format("%d\"", numerator) } else { String.format("%.1f\"", (numerator.toFloat() / denominator.toFloat())) }
        } else {
            // 分母が大きい
            if (numerator == 1) { String.format("%d/%d", numerator, denominator) } else {String.format("1/%.1f", (denominator.toFloat() / numerator.toFloat())) }
        }
    }

    private fun checkAperture(buffer: ByteArray?, position: Int, length: Int)
    {
        if ((length != 3)||(buffer == null))
        {
            // データがそろっていないので何もしない
            return
        }

        val focalValue = ((((buffer[position + 12].toUInt()).toInt() and 0xff) * 16777216)) + (((buffer[position + 13].toUInt()).toInt() and 0xff) * 65536) + (((buffer[position + 14].toUInt()).toInt() and 0xff) * 256) + ((buffer[position + 15].toUInt()).toInt() and 0x00ff)
        currentAperture = String.format("F%.1f", (focalValue.toFloat() / 10.0f))
    }

    private fun checkExposureCompensation(buffer: ByteArray?, position: Int, length: Int)
    {
        if ((length != 3)||(buffer == null))
        {
            // データがそろっていないので何もしない
            return
        }

        var expRevValue = ((((buffer[position + 12].toUInt()).toInt() and 0xff) * 16777216)) + (((buffer[position + 13].toUInt()).toInt() and 0xff) * 65536) + (((buffer[position + 14].toUInt()).toInt() and 0xff) * 256) + ((buffer[position + 15].toUInt()).toInt() and 0x00ff)
        if (expRevValue > 2147483647) // 0x7fffffff を超えた場合は、反転
        {
            expRevValue = (expRevValue.toLong() - 4294967296).toInt()
        }
        currentExpRev = String.format("%+.1f", (expRevValue.toFloat() / 10.0f))
    }

    private fun checkIsoSensitivity(buffer: ByteArray?, position: Int, length: Int)
    {
        if ((length != 3) || (buffer == null))
        {
            // データがそろっていないので何もしない
            return
        }
        val autoFlag = ((((buffer[position + 8].toUInt()).toInt() and 0xff) * 256)) + ((buffer[position + 9].toUInt()).toInt() and 0x00ff)
        val isoSensValue = ((((buffer[position + 4].toUInt()).toInt() and 0xff) * 16777216)) + (((buffer[position + 5].toUInt()).toInt() and 0xff) * 65536) + (((buffer[position + 6].toUInt()).toInt() and 0xff) * 256) + ((buffer[position + 7].toUInt()).toInt() and 0x00ff)
        if (autoFlag != 0)
        {
            currentIsoSensitivity = "ISO-A($isoSensValue)"
            if (isoSensValue == 0xfffe)
            {
                currentIsoSensitivity = "ISO-A(LOW)"
            }
        }
        else
        {
            currentIsoSensitivity = "ISO $isoSensValue"
            if (isoSensValue == 0xfffe)
            {
                currentIsoSensitivity = "ISO LOW"
            }
        }
    }

    private fun checkExposureWarning(buffer: ByteArray?, position: Int, length: Int)
    {
        if ((length != 1)||(buffer == null))
        {
            // データがそろっていないので何もしない
            return
        }
        val exposureWarningValue = ((((buffer[position + 4].toUInt()).toInt() and 0xff) * 16777216)) + (((buffer[position + 5].toUInt()).toInt() and 0xff) * 65536) + (((buffer[position + 6].toUInt()).toInt() and 0xff) * 256) + ((buffer[position + 7].toUInt()).toInt() and 0x00ff)
        currentExposureWarning = if (exposureWarningValue != 0) { "Exp.WARN" } else { "" }
    }

    private fun checkFocusType(buffer: ByteArray?, position: Int, length: Int)
    {
        if ((length != 1)||(buffer == null))
        {
            // データがそろっていないので何もしない
            return
        }
        val focusType = ((((buffer[position + 4].toUInt()).toInt() and 0xff) * 256)) + ((buffer[position + 5].toUInt()).toInt() and 0x00ff)
        currentFocusType = when (focusType)
        {
            0 -> "S-AF"
            1 -> "C-AF"
            2 -> "MF"
            else -> ""
        }
    }

    override fun stopStatusWatch()
    {
        isWatching = false
        isWatchingEvent = false
        whileEventReceive = false

        finishEventReceiverThread()
    }

    override fun getStatusList(key: String): List<String>
    {
        return (if (useOpcProtocol) { getStatusListOpc(key) } else { getStatusListOmds(key, latestEventResponse) })
    }

    private fun getStatusListOpc(key: String): List<String>
    {
        try
        {
            Log.v(TAG, " getStatusListOpc($key)")
            return (when (key) {
                ICameraStatus.TAKE_MODE -> getPropertySelectionList(opcTakeModeSelectionList, "<propname>TAKEMODE</propname>")
                ICameraStatus.SHUTTER_SPEED -> getPropertySelectionList(opcShutterSpeedSelectionList, "<propname>SHUTTER</propname>")
                ICameraStatus.APERTURE -> getPropertySelectionList(opcApertureSelectionList, "<propname>APERTURE</propname>")

                ICameraStatus.ISO_SENSITIVITY -> getPropertySelectionList(opcIsoSensitivitySelectionList, "<propname>ISO</propname>")
                ICameraStatus.EXPREV -> getPropertySelectionList(opcExpRevSelectionList, "<propname>EXPREV</propname>")
                ICameraStatus.AE -> getPropertySelectionList(opcFocusModeSelectionList, "<propname>FOCUS_STILL</propname>")

                ICameraStatus.WHITE_BALANCE -> getPropertySelectionList(opcWhiteBalanceSelectionList, "<propname>WB</propname>")
                ICameraStatus.EFFECT -> getPropertySelectionList(opcColorToneSelectionList, "<propname>COLORTONE</propname>")
                ICameraStatus.CAPTURE_MODE -> getPropertySelectionList(opcDriveModeSelectionList, "<propname>TAKE_DRIVE</propname>")
/*
                ICameraStatus.TORCH_MODE -> getAvailableTorchMode()
                ICameraStatus.BATTERY -> getAvailableRemainBattery()
                ICameraStatus.FOCUS_STATUS -> getAvailableFocusStatus()
                ICameraStatus.FOCAL_LENGTH  -> getAvailableFocalLength()
                ICameraStatus.REMAIN_SHOTS  -> getAvailableRemainShots()
*/
                else -> ArrayList()
            })
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
        return (ArrayList())
    }


    private fun getStatusListOmds(key: String, eventString: String): List<String>
    {
        try
        {
            Log.v(TAG, " getStatusListOmds($key)")
            return (when (key) {
                ICameraStatus.TAKE_MODE -> getPropertySelectionList(eventString, "<propname>takemode</propname>")
                ICameraStatus.SHUTTER_SPEED -> getPropertySelectionList(eventString, "<propname>shutspeedvalue</propname>")
                ICameraStatus.APERTURE -> getPropertySelectionList(eventString, "<propname>focalvalue</propname>")

                ICameraStatus.ISO_SENSITIVITY -> getPropertySelectionList(eventString, "<propname>isospeedvalue</propname>")
                ICameraStatus.EXPREV -> getPropertySelectionList(eventString, "<propname>expcomp</propname>")

                ICameraStatus.WHITE_BALANCE -> getAvailableWhiteBalance(eventString)
                ICameraStatus.EFFECT -> getPropertySelectionList(eventString, "<propname>colortone</propname>")
                ICameraStatus.CAPTURE_MODE -> getPropertySelectionList(eventString, "<propname>drivemode</propname>")

/*
                ICameraStatus.AE -> getAvailableMeteringMode()
                ICameraStatus.TORCH_MODE -> getAvailableTorchMode()
                ICameraStatus.BATTERY -> getAvailableRemainBattery()
                ICameraStatus.FOCUS_STATUS -> getAvailableFocusStatus()
                ICameraStatus.FOCAL_LENGTH  -> getAvailableFocalLength()
                ICameraStatus.REMAIN_SHOTS  -> getAvailableRemainShots()
*/
                else -> ArrayList()
            })
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
        return (ArrayList())
    }

    private fun getAvailableWhiteBalance(eventResponse: String) : List<String>
    {
        try
        {
            val wbValueList = getPropertySelectionList(eventResponse, "<propname>wbvalue</propname>")
            val wbItemList : ArrayList<String> = ArrayList()
            for (wbValue in wbValueList)
            {
                wbItemList.add(decideWhiteBalance(wbValue))
            }
            return (wbItemList)
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
        return (ArrayList())
    }


    override fun getStatus(key: String): String
    {
        try
        {
            return (when (key) {
                ICameraStatus.TAKE_MODE -> currentTakeMode
                ICameraStatus.SHUTTER_SPEED -> currentShutterSpeed
                ICameraStatus.APERTURE -> currentAperture
                ICameraStatus.EXPREV -> currentExpRev
                ICameraStatus.CAPTURE_MODE -> currentCaptureMode
                ICameraStatus.ISO_SENSITIVITY -> currentIsoSensitivity
                ICameraStatus.WHITE_BALANCE -> currentWhiteBalance
                ICameraStatus.AE -> currentFocusType               // currentMeteringMode
                ICameraStatus.EFFECT -> currentPictureEffect
                ICameraStatus.TORCH_MODE -> currentExposureWarning // currentTorchMode
                ICameraStatus.BATTERY -> currentRemainBattery
                ICameraStatus.FOCUS_STATUS -> currentFocusStatus
                ICameraStatus.FOCAL_LENGTH  -> currentFocalLength
                ICameraStatus.REMAIN_SHOTS  -> currentRemainShots
                else -> ""
            })
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
        return ("")
    }

    override fun getStatusColor(key: String): Int
    {
        if (key == ICameraStatus.TORCH_MODE)
        {
            return (Color.YELLOW) // 黄色にしてみる
        }
        return (Color.WHITE)
    }

    override fun setStatus(key: String, value: String)
    {
        if (useOpcProtocol)
        {
            setStatusOpc(key, value)
        }
        else
        {
            setStatusOmds(key, value)
        }
    }

    private fun setStatusOmds(key: String, value: String)
    {
        try
        {
            when (key)
            {
                ICameraStatus.TAKE_MODE ->  sendStatusRequest("takemode", value)
                ICameraStatus.SHUTTER_SPEED ->  sendStatusRequest("shutspeedvalue", value)
                ICameraStatus.APERTURE ->  sendStatusRequest("focalvalue", value)
                ICameraStatus.EXPREV ->  sendStatusRequest("expcomp", value)
                ICameraStatus.ISO_SENSITIVITY ->  sendStatusRequest("isospeedvalue", value)
                ICameraStatus.CAPTURE_MODE ->  sendStatusRequest("drivemode", value)
                ICameraStatus.WHITE_BALANCE ->  sendStatusRequest("wbvalue", decideWhiteBalanceValue(value))
                ICameraStatus.EFFECT ->  sendStatusRequest("colortone", value)
                ICameraStatus.AE ->  { }
                ICameraStatus.TORCH_MODE ->  { }
                ICameraStatus.BATTERY ->  { }
                ICameraStatus.FOCUS_STATUS ->  { }
                ICameraStatus.FOCAL_LENGTH  ->  { }
                ICameraStatus.REMAIN_SHOTS  -> { }
                else -> { }
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    private fun sendStatusRequest(property: String, value: String)
    {
        val requestUrl = "$executeUrl/set_camprop.cgi?com=set&propname=$property"
        val postData = "<?xml version=\"1.0\"?><set><value>$value</value></set>"
        val response: String = http.httpPostWithHeader(requestUrl, postData, headerMap, null, TIMEOUT_MS) ?: ""
        dumpLog(requestUrl, response)
    }

    private fun setStatusOpc(key: String, value: String)
    {
        try
        {
            when (key)
            {
                ICameraStatus.TAKE_MODE -> sendStatusRequest("TAKEMODE", value)
                ICameraStatus.SHUTTER_SPEED -> sendStatusRequest("SHUTTER", value)
                ICameraStatus.APERTURE -> sendStatusRequest("APERTURE", value)

                ICameraStatus.ISO_SENSITIVITY -> sendStatusRequest("ISO", value)
                ICameraStatus.EXPREV -> sendStatusRequest("EXPREV", value)
                ICameraStatus.AE -> sendStatusRequest("FOCUS_STILL", value)

                ICameraStatus.WHITE_BALANCE -> sendStatusRequest("WB", value)
                ICameraStatus.EFFECT -> sendStatusRequest("COLORTONE", value)
                ICameraStatus.CAPTURE_MODE -> sendStatusRequest("TAKE_DRIVE", value)

                ICameraStatus.TORCH_MODE ->  { }
                ICameraStatus.BATTERY ->  { }
                ICameraStatus.FOCUS_STATUS ->  { }
                ICameraStatus.FOCAL_LENGTH  ->  { }
                ICameraStatus.REMAIN_SHOTS  -> { }
                else -> { }
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    override fun detectedOpcProtocol(opcProtocol: Boolean)
    {
        Log.v(TAG, " --- detectedOpcProtocol($opcProtocol)")
        useOpcProtocol = opcProtocol
    }

    init
    {
        headerMap["User-Agent"] = userAgent // "OlympusCameraKit" // "OI.Share"
        headerMap["X-Protocol"] = userAgent // "OlympusCameraKit" // "OI.Share"

        //startEventWatch()
    }

    interface IPropertyListCallback
    {
        fun receivedReply(replyValue: String)
    }

    companion object
    {
        private val TAG = OmdsCameraStatusWatcher::class.java.simpleName

        // TIMEOUT VALUES
        private const val SLEEP_TIME_MS = 300
        private const val SLEEP_EVENT_TIME_MS = 500 // 500ms
        private const val TIMEOUT_MS = 2500

        private const val RECEIVE_BUFFER_SIZE = 16384

        // RTP HEADER IDs
        private const val ID_FRAME_SIZE = 0x01
        private const val ID_AF_FRAME_INFO = 0x02
        private const val ID_MEDIA_INFO = 0x03
        private const val ID_ROTATION_INFO = 0x04
        private const val ID_AVAILABLE_SHOTS = 0x05
        private const val ID_OMDS_UNKNOWN_01 = 0x06
        private const val ID_OMDS_UNKNOWN_02 = 0x07
        private const val ID_SHUTTER_SPEED = 0x08
        private const val ID_APERTURE = 0x09
        private const val ID_EXPOSURE_COMPENSATION = 0x0a
        private const val ID_OMDS_UNKNOWN_03 = 0x0b
        private const val ID_ISO_SENSITIVITY = 0x0c
        private const val ID_OMDS_UNKNOWN_04 = 0x0d
        private const val ID_OMDS_UNKNOWN_05 = 0x0e
        private const val ID_OMDS_UNKNOWN_06 = 0x0f
        private const val ID_EXPOSURE_WARNING = 0x10
        private const val ID_FOCUS_TYPE = 0x11
        private const val ID_ZOOM_LENS_INFO = 0x12
        private const val ID_REMAIN_VIDEO_TIME = 0x6a
        private const val ID_POSITION_LEVEL_INFO = 0x6b
        private const val ID_FACE_DETECT_1 = 0x6c
        private const val ID_FACE_DETECT_2 = 0x6d
        private const val ID_FACE_DETECT_3 = 0x6e
        private const val ID_FACE_DETECT_4 = 0x6f
        private const val ID_FACE_DETECT_5 = 0x70
        private const val ID_FACE_DETECT_6 = 0x71
        private const val ID_FACE_DETECT_7 = 0x72

        private const val isDumpLog = false
    }
}
