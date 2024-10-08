package jp.osdn.gokigen.aira01c.ui.configuration

import android.annotation.SuppressLint
import android.util.Log
import androidx.fragment.app.FragmentActivity
import jp.osdn.gokigen.aira01c.AppSingleton
import jp.osdn.gokigen.aira01c.R
import jp.osdn.gokigen.aira01c.camera.interfaces.ICameraMaintenanceCommandSequence
import jp.osdn.gokigen.aira01c.camera.interfaces.IOmdsOperationCallback
import java.util.HashMap

class CameraMaintenanceCommand(
    private val activity: FragmentActivity,
    private val command: String,
    private val parameter: String,
    private val commandTitle: String,
    private val okResponseText: String,
    private val ngResponseText: String,
    userAgent: String = "OlympusCameraKit"): ICameraMaintenanceCommandSequence
{
    private val headerMap: MutableMap<String, String> = HashMap()
    private var callback: IBusyProgressDrawer? = null

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
                    changeRunMode("standalone", sequenceNumber)
                }
                4 -> {
                    // ---- コマンド終了
                    activity.runOnUiThread {
                        callback?.setCommandFinished(true)
                        callback?.setResponseText(okResponseText, false)
                        callback?.controlCloseButton(true)
                    }
                }
                SEQ_ENTER_ERROR_SEQUENCE -> {
                    // === コマンド アボート (standaloneモードに切り替えて終了する）
                    changeRunMode("standalone", sequenceNumber)
                }
                else -> {
                    activity.runOnUiThread {
                        callback?.setCommandFinished(true)
                        callback?.setResponseText(ngResponseText, false)
                        callback?.controlCloseButton(true)
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
                            sendCommandSequence(SEQ_ENTER_ERROR_SEQUENCE)
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
                            sendCommandSequence(SEQ_ENTER_ERROR_SEQUENCE)
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

    override fun abortMaintenanceCommand(parameter: String?)
    {
        Log.v(TAG, "COMMAND ABORT: $parameter")
        sendCommandSequence(SEQ_ENTER_ERROR_SEQUENCE)
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

    companion object
    {
        private val TAG = ICameraMaintenanceCommandSequence::class.java.simpleName
        private const val SEQ_ENTER_ERROR_SEQUENCE = 1000
    }
}