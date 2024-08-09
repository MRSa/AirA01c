package jp.osdn.gokigen.aira01c.ui.configuration

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import jp.osdn.gokigen.aira01c.R
import jp.osdn.gokigen.aira01c.camera.interfaces.ICameraMaintenanceCommandSequence
import jp.osdn.gokigen.aira01c.camera.interfaces.IVibrator

class BusyProgressDialog : DialogFragment(), View.OnClickListener, IBusyProgressDrawer
{
    private lateinit var myContext : FragmentActivity
    private lateinit var myView: View
    private lateinit var myCommand: ICameraMaintenanceCommandSequence
    private lateinit var alertDialog: AlertDialog.Builder
    private var vibrator: IVibrator? = null
    private var container: ViewGroup? = null
    private var isEnabledClose = false
    private var isCommandFinished = false

    private fun prepare(context: FragmentActivity, command: ICameraMaintenanceCommandSequence, vibrator: IVibrator?)
    {
        this.myContext = context
        this.vibrator = vibrator
        this.myCommand = command
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog
    {
        try
        {
            return (showDialog())
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
        return super.onCreateDialog(savedInstanceState)
    }

    @SuppressLint("SetTextI18n")
    private fun showDialog(): Dialog
    {
        // コールバックの設定と、シーケンスの初期化
        myCommand.setCallback(this)
        myCommand.reset()

        // 表示イアログの生成
        if (!::alertDialog.isInitialized)
        {
            alertDialog = AlertDialog.Builder(myContext)
        }
        if (!::myView.isInitialized)
        {
            val inflater = myContext.layoutInflater
            myView = inflater.inflate(R.layout.dialog_busy_progress, container, false)
        }

        val previousButton = myView.findViewById<Button>(R.id.dialog_button_previous)
        val nextButton = myView.findViewById<Button>(R.id.dialog_button_proceed)
        val closeButton = myView.findViewById<Button>(R.id.busy_button_close)
        val responseArea = myView.findViewById<TextView>(R.id.busy_message_response)

        previousButton.setOnClickListener(this)
        nextButton.setOnClickListener(this)
        closeButton.setOnClickListener(this)

        myView.findViewById<TextView>(R.id.busy_message_title).text = myCommand.getCommandTitle()
        myView.findViewById<TextView>(R.id.busy_message_message).text = ""
        responseArea.text= ""

        previousButton.isEnabled = myCommand.isEnabledPrevious()
        previousButton.visibility = if (myCommand.isVisiblePrevious()) { View.VISIBLE } else { View.INVISIBLE }
        nextButton.isEnabled = myCommand.isEnableNext()
        nextButton.visibility = if (myCommand.isVisibleNext()) { View.VISIBLE } else { View.INVISIBLE }

        isEnabledClose = myCommand.isEnableClose()
        closeButton.isEnabled = isEnabledClose

        alertDialog.setView(myView)
        alertDialog.setCancelable(false)

        if (!isEnabledClose)
        {
            // ----- タイマーののち、Closeボタンを有効化する
            val thread1 = Thread {
                try
                {
                    Thread.sleep(COMMAND_ABORT_TIMEOUT)
                }
                catch (e: Exception)
                {
                    Log.v(TAG, " TIMEOUT : ENABLE CLOSE BUTTON")
                }
                if (!isCommandFinished)
                {
                    myContext.runOnUiThread {
                        try {
                            val message =
                                "${responseArea.text}\n${activity?.getString(R.string.busy_dialog_timeout_message)}"
                            responseArea.text = message
                            alertDialog.setCancelable(true)
                            closeButton.isEnabled = true
                            closeButton.visibility = View.VISIBLE
                            myView.invalidate()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
            try
            {
                thread1.start()
            }
            catch (e: Exception)
            {
                e.printStackTrace()
            }
        }
        return (alertDialog.create())
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
            Log.v(TAG, "pushedClose()")
            vibrator?.vibrate(IVibrator.VibratePattern.SIMPLE_SHORT_SHORT)
            dismiss()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    override fun setCommandFinished(isFinished: Boolean)
    {
        this.isCommandFinished = isFinished
    }

    override fun controlNextButton(isEnabled: Boolean)
    {
        myContext.runOnUiThread {
            try
            {
                val nextButton = myView.findViewById<Button>(R.id.dialog_button_proceed)
                nextButton.isEnabled = isEnabled
                nextButton.visibility = if (isEnabled) { View.VISIBLE } else { View.INVISIBLE }
                myView.invalidate()
            }
            catch (e: Exception)
            {
                e.printStackTrace()
            }
        }
    }

    override fun controlPreviousButton(isEnabled: Boolean) {
        myContext.runOnUiThread {
            try
            {
                val previousButton = myView.findViewById<Button>(R.id.dialog_button_previous)
                previousButton.isEnabled = isEnabled
                previousButton.visibility = if (isEnabled) { View.VISIBLE } else { View.INVISIBLE }
                myView.invalidate()
            }
            catch (e: Exception)
            {
                e.printStackTrace()
            }
        }
    }

    override fun controlCloseButton(isEnabled: Boolean)
    {
        myContext.runOnUiThread {
            try
            {
                val closeButton = myView.findViewById<Button>(R.id.busy_button_close)
                alertDialog.setCancelable(isEnabled)
                closeButton.isEnabled = isEnabled
                closeButton.visibility = View.VISIBLE
                myView.invalidate()
            }
            catch (e: Exception)
            {
                e.printStackTrace()
            }
        }
    }

    override fun setMessageText(message: String, isAppend: Boolean)
    {
        myContext.runOnUiThread {
            try
            {
                val messageArea = myView.findViewById<TextView>(R.id.busy_message_message)
                val messageToDraw = if (isAppend) { "${messageArea.text}\n$message" } else { message }
                messageArea.text = messageToDraw
                myView.invalidate()
            }
            catch (e: Exception)
            {
                e.printStackTrace()
            }
        }
    }

    override fun setResponseText(message: String, isAppend: Boolean)
    {
        myContext.runOnUiThread {
            try
            {
                val messageArea = myView.findViewById<TextView>(R.id.busy_message_response)
                val messageToDraw = if (isAppend) { "${messageArea.text}\n$message" } else { message }
                messageArea.text = messageToDraw
                myView.invalidate()
            }
            catch (e: Exception)
            {
                e.printStackTrace()
            }
        }
    }

    companion object
    {
        val TAG: String = BusyProgressDialog::class.java.simpleName
        private const val COMMAND_ABORT_TIMEOUT = 45 * 1000L // [ms]

        fun newInstance(context: FragmentActivity, command: ICameraMaintenanceCommandSequence, vibrator: IVibrator?): BusyProgressDialog
        {
            val instance = BusyProgressDialog()
            instance.prepare(context, command, vibrator)
            return (instance)
        }
    }
}
