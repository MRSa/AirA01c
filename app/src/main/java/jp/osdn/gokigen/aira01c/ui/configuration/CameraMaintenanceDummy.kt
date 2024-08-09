package jp.osdn.gokigen.aira01c.ui.configuration

import android.util.Log
import androidx.fragment.app.FragmentActivity
import jp.osdn.gokigen.aira01c.R
import jp.osdn.gokigen.aira01c.camera.interfaces.ICameraMaintenanceCommandSequence
import java.util.HashMap

class CameraMaintenanceDummy(private val activity: FragmentActivity, userAgent: String = "OlympusCameraKit"): ICameraMaintenanceCommandSequence
{
    private val headerMap: MutableMap<String, String> = HashMap()
    private var callback: IBusyProgressDrawer? = null

    init
    {
        headerMap["User-Agent"] = userAgent // "OlympusCameraKit" or "OI.Share"
        headerMap["X-Protocol"] = userAgent // "OlympusCameraKit" or "OI.Share"
    }

    override fun getCommandTitle(): String {
        return ("DUMMY COMMAND")
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
                // --- 3秒待って、Closeボタンを有効にする
                Thread.sleep(3000)
                activity.runOnUiThread {
                    callback?.setCommandFinished(true)
                    callback?.setResponseText(activity.getString(R.string.finish_refresh), false)
                    callback?.controlCloseButton(true)
                }
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

    override fun abortMaintenanceCommand(parameter: String?) {
        Log.v(TAG, "ABORT COMMAND : $parameter")
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
    }
}