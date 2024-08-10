package jp.osdn.gokigen.aira01c.ui.configuration

import android.annotation.SuppressLint
import android.util.Log
import androidx.fragment.app.FragmentActivity
import jp.osdn.gokigen.aira01c.AppSingleton
import jp.osdn.gokigen.aira01c.R
import jp.osdn.gokigen.aira01c.camera.interfaces.ICameraMaintenanceCommandSequence
import jp.osdn.gokigen.aira01c.camera.interfaces.IOmdsOperationCallback
import jp.osdn.gokigen.aira01c.camera.omds.IOpcEventNotify
import java.util.HashMap

class CameraPixelMappingCommand(
    private val activity: FragmentActivity,
    private val command: String,
    private val parameter: String,
    private val commandTitle: String,
    private val okResponseText: String,
    private val ngResponseText: String,
    userAgent: String = "OlympusCameraKit"): ICameraMaintenanceCommandSequence, IOpcEventNotify
{
    private val headerMap: MutableMap<String, String> = HashMap()
    private var callback: IBusyProgressDrawer? = null
    private var waitingForExecutionFinished = false

    init
    {
        headerMap["User-Agent"] = userAgent // "OlympusCameraKit" or "OI.Share"
        headerMap["X-Protocol"] = userAgent // "OlympusCameraKit" or "OI.Share"
    }

    override fun getCommandTitle(): String
    {
        return (commandTitle)
    }

    override fun executeMaintenanceCommand(parameter: String?)
    {
        Log.v(TAG, "EXECUTE COMMAND : $parameter")

        activity.runOnUiThread {
            callback?.setMessageText(activity.getString(R.string.action_refresh))
        }
        val thread = Thread {
            try
            {
                sendCommandSequence(0)
            }
            catch (e: Exception)
            {
                e.printStackTrace()
            }
        }
        try
        {
            AppSingleton.cameraControl.subscribeOpcEvent(this)
            thread.start()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    private fun sendCommandSequence(sequenceNumber: Int)
    {
        try
        {
            when (sequenceNumber) {
                0 -> {
                    changeRunMode("standalone", sequenceNumber)
                }
                1 -> {
                    changeRunMode("maintenance", sequenceNumber)
                }
                2 -> {
                    sendGetCommand(command, parameter, sequenceNumber)
                }
                3 -> {
                    waitForExecutionFinished(sequenceNumber)
                }
                4 -> {
                    changeRunMode("standalone", sequenceNumber)
                }
                5 -> {
                    // ---- コマンド終了
                    activity.runOnUiThread {
                        callback?.setCommandFinished(true)
                        callback?.setResponseText(okResponseText, false)
                        callback?.controlCloseButton(true)
                        AppSingleton.cameraControl.unsubscribeOpcEvent(this)
                    }
                }
                else -> {
                    // コマンド アボート (エラーが発生した場合、standaloneモードに切り替えて終了する）
                    changeRunMode("standalone", sequenceNumber)
                    activity.runOnUiThread {
                        callback?.setCommandFinished(true)
                        callback?.setResponseText(ngResponseText, false)
                        callback?.controlCloseButton(true)
                        AppSingleton.cameraControl.unsubscribeOpcEvent(this)
                    }
                }
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    private fun changeRunMode(runMode: String, index: Int)
    {
        try
        {
            callback?.setMessageText("${activity.getString(R.string.lbl_change_run_mode)} $runMode")
            AppSingleton.cameraControl.changeRunMode(runMode, object: IOmdsOperationCallback
            {
                @SuppressLint("SetTextI18n")
                override fun operationResult(isChange: Boolean, responseText: String)
                {
                    try
                    {
                        Thread.sleep(150) // ちょっと止めてみる
                        if (isChange)
                        {
                            Log.v(TAG, "RunMode: $runMode OK.")
                            sendCommandSequence(index + 1)
                        }
                        else
                        {
                            Log.v(TAG, "RunMode: $runMode NG.\n$responseText")
                            sendCommandSequence(1000)
                        }
                        activity.runOnUiThread {
                            callback?.setResponseText(responseText, true)
                        }
                    }
                    catch (e: Exception)
                    {
                        e.printStackTrace()
                    }
                }
            })
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    private fun sendGetCommand(command: String, parameter: String, index: Int)
    {
        try
        {
            callback?.setMessageText("${activity.getString(R.string.lbl_send_command)} $command")
            AppSingleton.cameraControl.sendGetCommand(command, parameter, object: IOmdsOperationCallback
            {
                override fun operationResult(isChange: Boolean, responseText: String)
                {
                    try
                    {
                        Thread.sleep(150) // 次のシーケンスに移る前に、ちょっと止めてみる
                        if (responseText.contains("200"))
                        {
                            Log.v(TAG, "$command OK.")
                            sendCommandSequence(index + 1)
                        }
                        else
                        {
                            Log.v(TAG, "$command NG.\n$responseText")
                            sendCommandSequence(1000)
                        }
                        activity.runOnUiThread {
                            callback?.setResponseText(responseText, true)
                        }
                    }
                    catch (e: Exception)
                    {
                        e.printStackTrace()
                    }
                }

            })
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    private fun waitForExecutionFinished(index: Int)
    {
        try
        {
            //  コマンド実行終了まで待つ
            waitingForExecutionFinished = true
            while (waitingForExecutionFinished)
            {
                Thread.sleep(LOOP_WAIT_MS)  // ちょっと待つ
                try
                {

                }
                catch (e: Exception)
                {
                    e.printStackTrace()
                    waitingForExecutionFinished = false
                }
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
        // "待ち"終了、次のシーケンスに移る
        sendCommandSequence(index + 1)
    }

    override fun abortMaintenanceCommand(parameter: String?)
    {
        Log.v(TAG, "COMMAND ABORT: $parameter")
        sendCommandSequence(1000)
    }

    override fun isVisiblePrevious(): Boolean {
        return (false)
    }

    override fun isEnabledPrevious(): Boolean {
        return (false)
    }

    override fun isVisibleNext(): Boolean {
        return (false)
    }

    override fun isEnableNext(): Boolean {
        return (false)
    }

    override fun isEnableClose(): Boolean {
        return (false)
    }

    override fun pressedPrevious() {
        Log.v(TAG, " PRESSED PREVIOUS")
    }

    override fun pressedNext() {
        Log.v(TAG, " PRESSED PREVIOUS")
    }

    override fun reset() {
        Log.v(TAG, "  ----- RESET -----")
    }

    override fun setCallback(callback: IBusyProgressDrawer) {
        this.callback = callback
    }

    override fun getSubscribeId(): String {
        return (TAG)
    }

    override fun receivedOpcEvent(eventMessage: String)
    {
        Log.v(TAG, "receivedOpcEvent() : $eventMessage")
        try
        {
            val processing = pickupValue("processing", eventMessage)
            if (processing.isNotEmpty())
            {
                callback?.setResponseText(processing, false)
            }
            val result = pickupValue("result", eventMessage)
            if (result.isNotEmpty())
            {
                // ---- 終了したか？
                callback?.setResponseText(result, true)
                if ((result.contains("ok"))||result.contains("OK"))
                {
                    // 正常終了 ... 後処理（standaloneモードに切り替え）を実行する
                    sendCommandSequence(4)
                }
                else
                {
                    // 異常終了
                    sendCommandSequence(1000)
                }
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    private fun pickupValue(@Suppress("SameParameterValue") tagName: String, data: String): String
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

    companion object
    {
        private val TAG = ICameraMaintenanceCommandSequence::class.java.simpleName
        private const val LOOP_WAIT_MS = 250L
    }
}
