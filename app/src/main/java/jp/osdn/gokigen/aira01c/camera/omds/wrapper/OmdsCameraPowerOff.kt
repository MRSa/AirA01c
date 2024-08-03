package jp.osdn.gokigen.aira01c.camera.omds.wrapper

import androidx.appcompat.app.AppCompatActivity
import jp.osdn.gokigen.aira01c.R
import jp.osdn.gokigen.aira01c.camera.omds.connection.OmdsCameraDisconnectSequence
import jp.osdn.gokigen.aira01c.camera.utils.ConfirmationDialog

class OmdsCameraPowerOff(private val context: AppCompatActivity) : ConfirmationDialog.ConfirmationCallback
{
    fun startCameraPowerOffSequence()
    {
        try
        {
            context.runOnUiThread {
                val dialog: ConfirmationDialog = ConfirmationDialog.newInstance(context)
                dialog.show(
                    context.getString(R.string.dialog_title_exit_application),
                    context.getString(R.string.dialog_message_exit_application), this)
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    override fun confirm()
    {
        try
        {
            // カメラの電源をOFFにする
            val thread = Thread { OmdsCameraDisconnectSequence(context, true) }
            thread.start()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }
}
