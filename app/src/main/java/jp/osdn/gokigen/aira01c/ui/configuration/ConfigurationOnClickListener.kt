package jp.osdn.gokigen.aira01c.ui.configuration

import android.view.View
import androidx.fragment.app.FragmentActivity
import jp.osdn.gokigen.aira01c.AppSingleton.Companion.cameraControl
import jp.osdn.gokigen.aira01c.R
import jp.osdn.gokigen.aira01c.camera.interfaces.ICameraConnectionStatus
import jp.osdn.gokigen.aira01c.camera.utils.ConfirmationDialog

class ConfigurationOnClickListener(private val activity: FragmentActivity) : View.OnClickListener
{

    override fun onClick(p0: View?)
    {
        try
        {
            if (!checkCameraConnection())
            {
                // --- カメラと接続中ではないときは処理を行わない
                return
            }

        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    private fun checkCameraConnection() : Boolean
    {
        try
        {
            val status = cameraControl.getConnectionStatus()
            val connectionStatus = (status  == ICameraConnectionStatus.CameraConnectionStatus.CONNECTED)
            if (!connectionStatus) {
                activity.runOnUiThread {
                    // 「カメラに接続していません」ダイアログを表示する。
                    val confirmationDialog = ConfirmationDialog.newInstance(activity)
                    confirmationDialog.show(android.R.drawable.ic_dialog_alert, activity.getString(R.string.camera_not_connected), activity.getString(R.string.initial_message))
                }
            }
            return (connectionStatus)
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
        return (false)
    }

    companion object
    {
        private val TAG = ConfigurationOnClickListener::class.java.simpleName
    }
}