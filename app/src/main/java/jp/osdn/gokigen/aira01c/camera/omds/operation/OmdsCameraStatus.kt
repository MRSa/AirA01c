package jp.osdn.gokigen.aira01c.camera.omds.operation

import android.util.Log
import androidx.fragment.app.FragmentActivity
import jp.osdn.gokigen.aira01c.R
import jp.osdn.gokigen.aira01c.camera.interfaces.IOmdsOperationCallback
import jp.osdn.gokigen.aira01c.camera.interfaces.IMessageDrawer
import jp.osdn.gokigen.aira01c.camera.utils.communication.SimpleHttpClient
import java.text.SimpleDateFormat
import java.util.Date
import java.util.HashMap
import java.util.Locale
import kotlin.Exception

class OmdsCameraStatus(private val activity: FragmentActivity, private val messageDrawer: IMessageDrawer, userAgent: String = "OlympusCameraKit", private val executeUrl : String = "http://192.168.0.10")
{
    private val headerMap: MutableMap<String, String> = HashMap()
    private val http = SimpleHttpClient()

    fun getCameraStatus(callback: IOmdsOperationCallback?)
    {
        try
        {
            val currentTime = Date(System.currentTimeMillis())
            val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.US)
            if (callback == null) {
                messageDrawer.setMessageToShow(activity.getString(R.string.get_camerastatus_title))
            }

            val thread = Thread {
                //  ステータスを取得する
                val getStatusUrl = "$executeUrl/get_state.cgi"
                val response: String = http.httpGetWithHeader(getStatusUrl, headerMap, null, TIMEOUT_MS) ?: ""
                Log.v(TAG, "RESP: (${response.length}) $response")
                callback?.operationResult(response)
                if (callback == null) {
                    messageDrawer.appendMessageToShow(
                        "(${dateFormat.format(currentTime)})"
                    )
                    parseReceivedStatus(response)
                    // messageDrawer.appendMessageToShow("- - - - - - - - - - - - - - - - - - - - -")
                }
            }
            thread.start()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    private fun parseReceivedStatus(response: String)
    {
        try
        {
            val cardStatus = pickupValue("cardstatus", response)
            val cardRemainNum = pickupValue("cardremainnum", response)
            val cardRemainSec = pickupValue("cardremainsec", response)
            val cardRemainByte = pickupValue("cardremainbyte", response)
            val lensMountStatus = pickupValue("lensmountstatus", response)
            val imagingState = pickupValue("imagingstate", response)

            val focalLength = pickupValue("focallength", response)
            val wideFocalLength = pickupValue("widefocallength", response)
            val teleFocalLength = pickupValue("telefocallength", response)
            val electricZoom = pickupValue("electriczoom", response)
            val macroSetting = pickupValue("macrosetting", response)

            messageDrawer.appendMessageToShow("${activity.getString(R.string.label_card_status)}: $cardStatus")
            messageDrawer.appendMessageToShow("${activity.getString(R.string.label_card_remain_bytes)}: $cardRemainByte ($cardRemainNum, ${cardRemainSec}sec)")
            messageDrawer.appendMessageToShow("${activity.getString(R.string.label_lens_mount_status)}: $lensMountStatus")
            messageDrawer.appendMessageToShow("${activity.getString(R.string.label_imaging_state)}: $imagingState")
            messageDrawer.appendMessageToShow("${activity.getString(R.string.label_focal_length)}: ${focalLength}mm ($wideFocalLength - $teleFocalLength)")
            messageDrawer.appendMessageToShow("${activity.getString(R.string.label_electric_zoom)}: $electricZoom")
            messageDrawer.appendMessageToShow("${activity.getString(R.string.label_macro_setting)}: $macroSetting")
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    private fun pickupValue(tagName: String, data: String): String
    {
        var value = ""
        try
        {
            val startTag = "<$tagName>"
            val endTag = "</$tagName>"

            val startPosition = data.indexOf(startTag) + startTag.length
            val endPosition = data.indexOf(endTag)
            if ((startPosition >= 0)&&(endPosition > 0))
            {
                value = data.substring(startPosition, endPosition)
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
        return (value)
    }

    init
    {
        headerMap["User-Agent"] = userAgent // "OlympusCameraKit" // "OI.Share"
        headerMap["X-Protocol"] = userAgent // "OlympusCameraKit" // "OI.Share"
    }

    companion object
    {
        private val TAG = OmdsTimeSync::class.java.simpleName
        private const val TIMEOUT_MS = 5000
    }
}
