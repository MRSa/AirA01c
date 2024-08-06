package jp.osdn.gokigen.aira01c.camera.utils

import android.annotation.SuppressLint
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import jp.osdn.gokigen.aira01c.AppSingleton
import jp.osdn.gokigen.aira01c.R
import jp.osdn.gokigen.aira01c.camera.interfaces.IOmdsOperationCallback
import jp.osdn.gokigen.aira01c.camera.interfaces.IVibrator
import jp.osdn.gokigen.aira01c.camera.utils.communication.SimpleHttpClient
import java.util.HashMap

class SendCommandDialog : DialogFragment(), View.OnClickListener
{
    private val headerMap: MutableMap<String, String> = HashMap()

    private lateinit var myContext : FragmentActivity
    private lateinit var myView: View
    private var vibrator: IVibrator? = null

    private fun prepare(context: FragmentActivity, vibrator: IVibrator?, userAgent: String = "OlympusCameraKit")
    {
        this.myContext = context
        this.vibrator = vibrator
        headerMap["User-Agent"] = userAgent // "OlympusCameraKit" // "OI.Share"
        headerMap["X-Protocol"] = userAgent // "OlympusCameraKit" // "OI.Share"
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
        //alertDialog.setPositiveButton(myContext.getString(R.string.dialog_positive_execute)) { dialog, _ -> dialog.dismiss() }
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
                        myContext.runOnUiThread {
                            try
                            {
                                val textView = myView.findViewById<TextView>(R.id.omds_command_response_value)
                                textView.text = responseText
                                vibrator?.vibrate(IVibrator.VibratePattern.SIMPLE_SHORT)
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
            val target = myView.findViewById<EditText>(R.id.edit_http_header).text.toString()
            val method = myView.findViewById<EditText>(R.id.edit_method).text.toString()
            val command = myView.findViewById<EditText>(R.id.edit_command).text.toString()
            val option = myView.findViewById<EditText>(R.id.edit_option).text.toString()
            val parameter = myView.findViewById<EditText>(R.id.edit_parameter).text.toString()
            val body = myView.findViewById<EditText>(R.id.edit_body).text.toString()

            val thread = Thread {
                try
                {
                    val http = SimpleHttpClient()
                    var targetUrl = "$target$command"
                    if (option.isNotEmpty())
                    {
                        targetUrl = "$targetUrl?$option"
                    }
                    if (parameter.isNotEmpty())
                    {
                        targetUrl = "$targetUrl&$parameter"
                    }
                    Log.v(TAG, "SEND COMMAND: $targetUrl  BODY: $body")
                    val responseText: String = when (method) {
                        "GET" -> {
                            http.httpGetWithHeader(targetUrl, headerMap, null, DEFAULT_TIMEOUT) ?: ""
                        }
                        "POST" -> {
                            http.httpPostWithHeader(targetUrl, body, headerMap, null, DEFAULT_TIMEOUT) ?: ""
                        }
                        "PUT" -> {
                            http.httpPutWithHeader(targetUrl, body, headerMap, null, DEFAULT_TIMEOUT) ?: ""
                        }
                        else -> {
                            http.httpGetWithHeader(targetUrl, headerMap, null, DEFAULT_TIMEOUT) ?: ""
                        }
                    }
                    myContext.runOnUiThread {
                        try
                        {
                            val textView = myView.findViewById<TextView>(R.id.omds_command_response_value)
                            textView.text = responseText
                            vibrator?.vibrate(IVibrator.VibratePattern.SIMPLE_MIDDLE)
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
            thread.start()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    companion object
    {
        private val TAG = SendCommandDialog::class.java.simpleName
        private const val DEFAULT_TIMEOUT = 10 * 1000 // [ms]

        fun newInstance(context: FragmentActivity, vibrator: IVibrator?): SendCommandDialog
        {
            val instance = SendCommandDialog()
            instance.prepare(context, vibrator)
            return (instance)
        }
    }
}
