package jp.osdn.gokigen.aira01c.camera.utils

import android.annotation.SuppressLint
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import jp.osdn.gokigen.aira01c.AppSingleton
import jp.osdn.gokigen.aira01c.R
import jp.osdn.gokigen.aira01c.camera.interfaces.IOmdsOperationCallback

class SendCommandDialog : DialogFragment(), View.OnClickListener
{
    private lateinit var myContext : FragmentActivity
    private lateinit var myView: View

    private fun prepare(context: FragmentActivity)
    {
        this.myContext = context
    }

    @SuppressLint("SetTextI18n")
    fun show()
    {
        // 表示イアログの生成
        val alertDialog = AlertDialog.Builder(myContext)
        val inflater = myContext.layoutInflater
        myView = inflater.inflate(R.layout.http_request_layout, null, false)

        myView.findViewById<Button>(R.id.change_to_standalone).setOnClickListener(this)
        myView.findViewById<Button>(R.id.change_to_rec).setOnClickListener(this)
        myView.findViewById<Button>(R.id.change_to_playback).setOnClickListener(this)
        myView.findViewById<Button>(R.id.change_to_playback_maintenance).setOnClickListener(this)
        myView.findViewById<Button>(R.id.send_message_button).setOnClickListener(this)
        myView.findViewById<TextView>(R.id.omds_command_response_value).text = ""

        alertDialog.setView(myView)
        alertDialog.setCancelable(true)
        alertDialog.setPositiveButton(myContext.getString(R.string.dialog_positive_execute)) { dialog, _ -> dialog.dismiss() }
        alertDialog.show()
    }

    override fun onClick(p0: View?)
    {
        when (p0?.id)
        {
            R.id.change_to_standalone -> { changeRunMode("standalone") }
            R.id.change_to_rec -> { changeRunMode("rec") }
            R.id.change_to_playback -> { changeRunMode("play") }
            R.id.change_to_playback_maintenance -> { changeRunMode("playmaintenance") }
            R.id.send_message_button -> { sendMessage() }
            else -> { }
        }
    }

    private fun changeRunMode(mode: String)
    {
        Log.v(TAG, "changeRunMode(): $mode")
        try
        {
            AppSingleton.cameraControl.changeRunMode(mode, object: IOmdsOperationCallback
            {
                @SuppressLint("SetTextI18n")
                override fun operationResult(responseText: String)
                {
                    try
                    {
                        activity?.runOnUiThread {
                            try
                            {
                                myView.findViewById<TextView>(R.id.omds_command_response_value).text =
                                    if (responseText[0] == '2')
                                    {
                                        Log.v(TAG, "SUCCESS>RunMode($mode)")
                                        "Run Mode: $mode"
                                    }
                                    else
                                    {
                                        Log.v(TAG, "ERR>RunMode($mode) : $responseText")
                                        responseText
                                    }
                            }
                            catch (e: Exception)
                            {
                                e.printStackTrace()
                            }
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

    private fun sendMessage()
    {
        Log.v(TAG, "sendMessage()")
        try
        {

        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    companion object
    {
        private val TAG = SendCommandDialog::class.java.simpleName

        fun newInstance(context: FragmentActivity): SendCommandDialog
        {
            val instance = SendCommandDialog()
            instance.prepare(context)
            return (instance)
        }
    }


}
