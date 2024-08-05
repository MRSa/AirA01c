package jp.osdn.gokigen.aira01c.ui.configuration

import android.content.res.Resources.Theme
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.fragment.app.FragmentActivity
import jp.osdn.gokigen.aira01c.AppSingleton.Companion.cameraControl
import jp.osdn.gokigen.aira01c.R
import jp.osdn.gokigen.aira01c.camera.interfaces.ICameraConnectionStatus
import jp.osdn.gokigen.aira01c.camera.utils.ConfirmationDialog
import jp.osdn.gokigen.aira01c.camera.utils.ConfirmationDialog.ConfirmationCallback
import jp.osdn.gokigen.aira01c.camera.utils.SendCommandDialog

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
            when (p0?.id) {
                R.id.btnFormatSd -> { executeFormatSd() }
                R.id.btnDeleteAllContent -> { executeDeleteAllContent() }
                R.id.btnLevelReset -> { executeLevelAdjustReset() }
                R.id.btnLevelAdjust -> { executeLevelAdjustAdjust() }
                R.id.btnPixelMapping -> { executePixelMapping() }
                R.id.btnResetHardware -> { executeFactoryReset() }
                R.id.btnSendCommand -> { executeSendCommand() }
                R.id.btnNetworkSettings -> { }
                R.id.btnOthers -> { }
                R.id.btnStandalone01 -> { }
                R.id.btnStandalone02 -> { }
                R.id.btnStandalone03 -> { }
                R.id.btnStandalone11 -> { }
                R.id.btnStandalone01 -> { }
                R.id.btnStandalone13 -> { }
                R.id.btnSdCardReserve -> { }
                R.id.btnLevelReserve00 -> { }
                R.id.btnLevelReserve01 -> { }
                R.id.btnHardwareReserve -> { }
                else -> { Log.v(TAG, "clicked unknown ID: ${p0?.id}") }
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

    private fun executeFormatSd()
    {
        try
        {
            ConfirmationDialog.newInstance(activity).show(activity.getString(R.string.dialog_title_format_sd), activity.getString(R.string.dialog_message_format_sd),
                object : ConfirmationCallback {
                    override fun confirm() {
                        val thread = Thread {
                            try
                            {
                                Log.v(TAG, "executeFormatSd() : START")


                            }
                            catch (e: Exception)
                            {
                                e.printStackTrace()
                            }
                        }
                        thread.start()
                    }
                })
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    private fun executeDeleteAllContent()
    {
        try
        {
            ConfirmationDialog.newInstance(activity).show(activity.getString(R.string.dialog_title_delete_all), activity.getString(R.string.dialog_message_delete_all),
                object : ConfirmationCallback {
                    override fun confirm() {
                        val thread = Thread {
                            try
                            {
                                Log.v(TAG, "executeDeleteAllContent() : START")


                            }
                            catch (e: Exception)
                            {
                                e.printStackTrace()
                            }
                        }
                        thread.start()
                    }
                })
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    private fun executeLevelAdjustReset()
    {
        try
        {
            ConfirmationDialog.newInstance(activity).show(activity.getString(R.string.dialog_title_level_adjust_reset), activity.getString(R.string.dialog_message_level_adjust_reset),
                object : ConfirmationCallback {
                    override fun confirm() {
                        val thread = Thread {
                            try
                            {
                                Log.v(TAG, "executeLevelAdjustReset() : START")

                            }
                            catch (e: Exception)
                            {
                                e.printStackTrace()
                            }
                        }
                        thread.start()
                    }
                })
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    private fun executeLevelAdjustAdjust()
    {
        try
        {
            ConfirmationDialog.newInstance(activity).show(activity.getString(R.string.dialog_title_level_adjust_adjust), activity.getString(R.string.dialog_message_level_adjust_adjust),
                object : ConfirmationCallback {
                    override fun confirm() {
                        val thread = Thread {
                            try
                            {
                                Log.v(TAG, "executeLevelAdjustAdjust() : START")

                            }
                            catch (e: Exception)
                            {
                                e.printStackTrace()
                            }
                        }
                        thread.start()
                    }
                })
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    private fun executePixelMapping()
    {
        try
        {
            ConfirmationDialog.newInstance(activity).show(activity.getString(R.string.dialog_title_pixel_mapping), activity.getString(R.string.dialog_message_pixel_mapping),
                object : ConfirmationCallback {
                    override fun confirm() {
                        val thread = Thread {
                            try
                            {
                                Log.v(TAG, "executePixelMapping() : START")

                            }
                            catch (e: Exception)
                            {
                                e.printStackTrace()
                            }
                        }
                        thread.start()
                    }
                })
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    private fun executeFactoryReset()
    {
        try
        {
            ConfirmationDialog.newInstance(activity).show(activity.getString(R.string.dialog_title_factory_reset), activity.getString(R.string.dialog_message_factory_reset),
                object : ConfirmationCallback {
                    override fun confirm() {
                        val thread = Thread {
                            try
                            {
                                Log.v(TAG, "executeFactoryReset() : START")

                            }
                            catch (e: Exception)
                            {
                                e.printStackTrace()
                            }
                        }
                        thread.start()
                    }
                })
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    private fun executeSendCommand()
    {
        try
        {
            // コマンド送信ダイアログの表示
            SendCommandDialog.newInstance(activity).show()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }


    companion object
    {
        private val TAG = ConfigurationOnClickListener::class.java.simpleName
    }
}