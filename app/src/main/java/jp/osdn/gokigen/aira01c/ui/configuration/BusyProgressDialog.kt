package jp.osdn.gokigen.aira01c.ui.configuration

import android.annotation.SuppressLint
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import jp.osdn.gokigen.aira01c.R
import jp.osdn.gokigen.aira01c.camera.interfaces.ICameraMaintenanceCommandSequence
import jp.osdn.gokigen.aira01c.camera.interfaces.IVibrator
import java.util.HashMap

class BusyProgressDialog : DialogFragment(), View.OnClickListener
{
    private val headerMap: MutableMap<String, String> = HashMap()

    private lateinit var myContext : FragmentActivity
    private lateinit var myView: View
    private lateinit var myCommand: ICameraMaintenanceCommandSequence
    private var vibrator: IVibrator? = null
    private var isEnabledClose = false

    private fun prepare(context: FragmentActivity, command: ICameraMaintenanceCommandSequence, vibrator: IVibrator?, userAgent: String = "OlympusCameraKit")
    {
        this.myContext = context
        this.vibrator = vibrator
        this.myCommand = command
        headerMap["User-Agent"] = userAgent // "OlympusCameraKit" // "OI.Share"
        headerMap["X-Protocol"] = userAgent // "OlympusCameraKit" // "OI.Share"
    }

    @SuppressLint("SetTextI18n")
    fun show()
    {
        // まずはシーケンスの初期化
        myCommand.reset()

        // 表示イアログの生成
        val alertDialog = AlertDialog.Builder(myContext)
        val inflater = myContext.layoutInflater
        myView = inflater.inflate(R.layout.dialog_busy_progress, null, false)

        val previousButton = myView.findViewById<Button>(R.id.dialog_button_previous)
        val nextButton = myView.findViewById<Button>(R.id.dialog_button_proceed)
        val closeButton = myView.findViewById<Button>(R.id.busy_button_close)

        previousButton.setOnClickListener(this)
        nextButton.setOnClickListener(this)
        closeButton.setOnClickListener(this)

        myView.findViewById<TextView>(R.id.busy_message_title).text = myCommand.getCommandTitle()
        myView.findViewById<TextView>(R.id.busy_message_message).text = ""
        myView.findViewById<TextView>(R.id.busy_message_response).text = ""

        previousButton.isEnabled = myCommand.isEnabledPrevious()
        previousButton.visibility = if (myCommand.isVisiblePrevious()) { View.VISIBLE } else { View.INVISIBLE }
        nextButton.isEnabled = myCommand.isEnableNext()
        nextButton.visibility = if (myCommand.isVisibleNext()) { View.VISIBLE } else { View.INVISIBLE }

        isEnabledClose = myCommand.isEnableClose()
        closeButton.isEnabled = isEnabledClose

        alertDialog.setView(myView)
        alertDialog.setCancelable(false)
        //alertDialog.setPositiveButton(myContext.getString(R.string.dialog_positive_execute)) { dialog, _ -> dialog.dismiss() }
        alertDialog.show()

        if (!isEnabledClose)
        {
            // ----- タイマーののち、Closeボタンを有効化する
            val thread = Thread {
                try
                {
                    Thread.sleep(COMMAND_ABORT_TIMEOUT)
                }
                catch (e: Exception)
                {
                    Log.v(TAG, " TIMEOUT : ENABLE CLOSE BUTTON")
                }
                myContext.runOnUiThread {
                    try
                    {
                        closeButton.isEnabled = true
                        closeButton.visibility = View.VISIBLE
                        myView.invalidate()
                    }
                    catch (e: Exception)
                    {
                        e.printStackTrace()
                    }
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
    }

    override fun onClick(p0: View?)
    {
        when (p0?.id)
        {
            R.id.dialog_button_previous -> { myCommand.pressedPrevious() }
            R.id.dialog_button_proceed -> { myCommand.pressedNext() }
            R.id.busy_button_close -> { pushedClose() }
            else -> { }
        }
    }

    private fun pushedClose()
    {
        try
        {
            dismiss()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }


    companion object
    {
        private val TAG = BusyProgressDialog::class.java.simpleName
        private const val DEFAULT_TIMEOUT = 10 * 1000 // [ms]
        private const val COMMAND_ABORT_TIMEOUT = 20 * 1000L // [ms]

        fun newInstance(context: FragmentActivity, command: ICameraMaintenanceCommandSequence, vibrator: IVibrator?): BusyProgressDialog
        {
            val instance = BusyProgressDialog()
            instance.prepare(context, command, vibrator)
            return (instance)
        }
    }
}
