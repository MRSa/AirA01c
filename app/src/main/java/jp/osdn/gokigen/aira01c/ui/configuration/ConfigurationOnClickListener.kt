package jp.osdn.gokigen.aira01c.ui.configuration

import android.content.Context.VIBRATOR_MANAGER_SERVICE
import android.content.Context.VIBRATOR_SERVICE
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import android.view.View
import androidx.fragment.app.FragmentActivity
import jp.osdn.gokigen.aira01c.AppSingleton.Companion.cameraControl
import jp.osdn.gokigen.aira01c.R
import jp.osdn.gokigen.aira01c.camera.interfaces.ICameraConnectionStatus
import jp.osdn.gokigen.aira01c.camera.interfaces.ICameraMaintenanceCommandSequence
import jp.osdn.gokigen.aira01c.camera.interfaces.IVibrator
import jp.osdn.gokigen.aira01c.camera.utils.ConfirmationDialog
import jp.osdn.gokigen.aira01c.camera.utils.ConfirmationDialog.ConfirmationCallback
import jp.osdn.gokigen.aira01c.camera.utils.SendCommandDialog

class ConfigurationOnClickListener(private val activity: FragmentActivity) : View.OnClickListener, IVibrator
{

    override fun onClick(p0: View?)
    {
        try
        {
            vibrate(IVibrator.VibratePattern.SIMPLE_SHORT)
/*
            if (!checkCameraConnection())
            {
                // --- カメラと接続中ではないときは処理を行わない
                return
            }
*/
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
        executeMaintenanceCommand(
            activity.getString(R.string.dialog_title_factory_reset),
            activity.getString(R.string.dialog_message_factory_reset),
            CameraMaintenanceDummy(activity),
            "",
            this)
    }

    private fun executeMaintenanceCommand(title: String, message: String, commandSequence: ICameraMaintenanceCommandSequence, parameter: String?, vibrator: IVibrator)
    {
        try
        {
            // ----- 確認のメッセージを表示して、実行を指示されたら実行する
            ConfirmationDialog.newInstance(activity).show(title, message,
                object : ConfirmationCallback {
                    override fun confirm() {
                        activity.runOnUiThread {
                            try
                            {
                                val busyDialog = BusyProgressDialog.newInstance(activity, commandSequence, vibrator)
                                busyDialog.isCancelable = false
                                busyDialog.show(activity.supportFragmentManager, BusyProgressDialog.TAG)
                            }
                            catch (e: Exception)
                            {
                                e.printStackTrace()
                            }
                        }
                        val thread = Thread {
                            try
                            {
                                commandSequence.executeMaintenanceCommand(parameter)
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
            // ----- コマンド送信ダイアログの表示
            SendCommandDialog.newInstance(activity, this).show()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    override fun vibrate(vibratePattern: IVibrator.VibratePattern)
    {
        try
        {
            // バイブレータをつかまえる
            val vibrator  = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            {
                val vibratorManager =  activity.getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibratorManager.defaultVibrator
            }
            else
            {
                @Suppress("DEPRECATION")
                activity.getSystemService(VIBRATOR_SERVICE) as Vibrator
            }
            if (!vibrator.hasVibrator())
            {
                Log.v(TAG, " not have Vibrator...")
                return
            }
            @Suppress("DEPRECATION") val thread = Thread {
                try
                {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    {
                        vibrator.vibrate(VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE))
                    }
                    else
                    {
                        when (vibratePattern)
                        {
                            IVibrator.VibratePattern.SIMPLE_SHORT_SHORT -> vibrator.vibrate(30)
                            IVibrator.VibratePattern.SIMPLE_SHORT ->  vibrator.vibrate(50)
                            IVibrator.VibratePattern.SIMPLE_MIDDLE -> vibrator.vibrate(100)
                            IVibrator.VibratePattern.SIMPLE_LONG ->  vibrator.vibrate(150)
                            else -> { }
                        }
                    }
                }
                catch (e : Exception)
                {
                    e.printStackTrace()
                }
            }
            thread.start()
        }
        catch (e: java.lang.Exception)
        {
            e.printStackTrace()
        }
    }

    companion object
    {
        private val TAG = ConfigurationOnClickListener::class.java.simpleName
    }
}