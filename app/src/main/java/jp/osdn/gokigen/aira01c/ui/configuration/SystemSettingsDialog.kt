package jp.osdn.gokigen.aira01c.ui.configuration

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatSpinner
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import jp.osdn.gokigen.aira01c.AppSingleton
import jp.osdn.gokigen.aira01c.R
import jp.osdn.gokigen.aira01c.camera.interfaces.ICameraMaintenanceCommandSequence
import jp.osdn.gokigen.aira01c.camera.interfaces.IOmdsOperationCallback
import jp.osdn.gokigen.aira01c.camera.interfaces.IVibrator
import java.util.HashMap

class SystemSettingsDialog: DialogFragment(), View.OnClickListener, IBusyProgressDrawer
{
    private val headerMap: MutableMap<String, String> = HashMap()

    private lateinit var myContext : FragmentActivity
    private lateinit var myView: View
    private lateinit var myCommand: ICameraMaintenanceCommandSequence
    private lateinit var alertDialog: AlertDialog.Builder

    private var vibrator: IVibrator? = null
    private var container: ViewGroup? = null
    
    private fun prepare(context: FragmentActivity, command: ICameraMaintenanceCommandSequence, vibrator: IVibrator?, userAgent: String = "OlympusCameraKit")
    {
        this.myContext = context
        this.myCommand = command
        this.vibrator = vibrator

        headerMap["User-Agent"] = userAgent // "OlympusCameraKit" // "OI.Share"
        headerMap["X-Protocol"] = userAgent // "OlympusCameraKit" // "OI.Share"
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
            myView = inflater.inflate(R.layout.dialog_system_settings, container, false)
        }
        setupSystemSettingDialog()
        queryCurrentSystemSettings()

        alertDialog.setView(myView)
        alertDialog.setCancelable(true)

        return (alertDialog.create())        
    }

    private fun queryCurrentSystemSettings()
    {
        try
        {
            //  現在のシステム設定を取得する
            val thread = Thread {
                try
                {
                    Thread.sleep(100) // 送信前にちょっと止めてみる
                    val command = "get_camprop.cgi"
                    val parameter = "com=desc&propname=desclist"
                    val body = "<?xml version=\"1.0\"?><desclist><prop name=\"WIFI_CH\"/><prop name=\"SLEEP\"/><prop name=\"SOUND_VOLUME_LEVEL\"/><prop name=\"LENS_RESET\"/><prop name=\"IMAGESIZE\"/><prop name=\"COMPRESSIBILITY_RATIO\"/><prop name=\"RAW\"/></desclist>"

                    AppSingleton.cameraControl.sendPostCommand(command, parameter, body, object: IOmdsOperationCallback
                    {
                        override fun operationResult(isChange: Boolean, responseText: String)
                        {
                            try
                            {
                                activity?.runOnUiThread {
                                    // 取得したシステム設定を画面に反映させる
                                    extractSystemSettings(responseText)
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
            controlButtons(false, requireActivity().getString(R.string.message_get_current_settings))
            thread.start()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    private fun extractSystemSettings(responseText: String)
    {
        try
        {
            Log.v(TAG, "Response: $responseText")

            var parseCount = 0
            val desclist = responseText.split("</desc>")
            for (desc in desclist)
            {
                if (desc.contains("<desc>"))
                {
                    try
                    {
                        if (::myView.isInitialized)
                        {
                            parseReceivedSetting(desc)
                            parseCount++
                        }
                    }
                    catch (ee: Exception)
                    {
                        ee.printStackTrace()
                    }
                }
            }
            val messageId = if (parseCount > 0) { R.string.message_parameter_selection_ready } else { R.string.message_parameter_get_failure }
            controlButtons(true, requireActivity().getString(messageId))
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    private fun parseReceivedSetting(description: String)
    {
        try
        {
            val propname = pickupValue("propname", description)
            //val attribute = pickupValue("attribute", description)
            val value = pickupValue("value", description)
            val enumList = pickupValue("enum", description).split(" ")
            when (propname) {
                "WIFI_CH" -> { setupSpinner(R.id.select_wifi_ch, enumList, value)}
                "SLEEP" -> { setupSpinner(R.id.select_sleep, enumList, value) }
                "SOUND_VOLUME_LEVEL" -> { setupSpinner(R.id.select_sound_volume, enumList, value)}
                "LENS_RESET" -> { setupSpinner(R.id.select_lens_reset, enumList, value) }
                "IMAGESIZE" -> { setupSpinner(R.id.select_image_quality, enumList, value) }
                "COMPRESSIBILITY_RATIO" -> { setupSpinner(R.id.select_compression_ratio, enumList, value) }
                "RAW" -> { setupSpinner(R.id.select_record_raw, enumList, value) }
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    private fun setupSpinner(id: Int, selectionList: List<String>, currentSelection: String)
    {
        val spinner = myView.findViewById<AppCompatSpinner>(id)
        try
        {
            val adapter = ArrayAdapter<String>(this.requireContext(), android.R.layout.simple_spinner_item)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            adapter.addAll(selectionList)
            spinner.adapter = adapter
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
        try
        {
            val index = selectionList.indexOf(currentSelection)
            spinner.setSelection(index)
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    private fun pickupValue(tagName: String, data: String): String
    {
        var value = ""
        try
        {
            val startTag = "<$tagName>"
            val endTag = "</$tagName>"

            val startPosition = data.indexOf(startTag) + startTag.length
            val endPosition = data.indexOf(endTag)
            if ((startPosition >= 0)&&(endPosition > 0))
            {
                value = data.substring(startPosition, endPosition)
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
        return (value)
    }

    private fun controlButtons(isEnabled: Boolean, message: String)
    {
        try
        {
            if (::myView.isInitialized)
            {
                myView.findViewById<TextView>(R.id.text_information).text = message
                myView.findViewById<ImageButton>(R.id.button_data_reload).isEnabled = isEnabled
                myView.findViewById<Button>(R.id.btn_apply_change).isEnabled = isEnabled
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    private fun setupSystemSettingDialog()
    {
        // -------- ダイアログの処理メイン
        try
        {
            if (::myView.isInitialized)
            {
                myView.findViewById<ImageButton>(R.id.button_data_reload).setOnClickListener(this)
                myView.findViewById<Button>(R.id.btn_apply_change).setOnClickListener(this)
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    override fun onClick(p0: View?)
    {
        try
        {
            vibrator?.vibrate(IVibrator.VibratePattern.SIMPLE_SHORT_SHORT)
            when (p0?.id) {
                R.id.button_data_reload -> { queryCurrentSystemSettings() }
                R.id.btn_apply_change -> { applySystemSettings() }
                else -> { }
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    private fun sendSystemSetting(body: String)
    {
        try
        {
            //  ----- 現在の設定を反映する
            val thread = Thread {
                try {
                    Thread.sleep(150) // 送信前にちょっと止めてみる

                    AppSingleton.cameraControl.changeRunMode(
                        "standalone",
                        object : IOmdsOperationCallback {
                            override fun operationResult(isChange: Boolean, responseText: String)
                            {
                                try
                                {
                                    Thread.sleep(200) // 送信前にちょっと止めてみる
                                    AppSingleton.cameraControl.changeRunMode(
                                        "rec",
                                        object : IOmdsOperationCallback {
                                            override fun operationResult(isChange: Boolean, responseText: String)
                                            {
                                                try
                                                {
                                                    sendSystemSettingImpl(body)
                                                }
                                                catch (e: Exception)
                                                {
                                                    e.printStackTrace()
                                                }
                                            }
                                        }
                                    )
                                }
                                catch (e: Exception)
                                {
                                    e.printStackTrace()
                                }
                            }
                        }
                    )
                }
                catch (e: Exception)
                {
                    e.printStackTrace()
                }
            }
            controlButtons(false, requireActivity().getString(R.string.message_apply_settings))
            thread.start()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    private fun sendSystemSettingImpl(body: String)
    {
        try
        {
            Thread.sleep(150) // 送信前にちょっと止める

            // カメラプロパティ変更コマンドを発行
            val command = "set_camprop.cgi"
            val parameter = "com=setlist"
            AppSingleton.cameraControl.sendPostCommand(command, parameter, body, object : IOmdsOperationCallback {
                override fun operationResult(isChange: Boolean, responseText: String) {
                    try
                    {
                        registerMySet()  // もう一発コマンド発行
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

    private fun registerMySet()
    {
        try
        {
            val thread = Thread {
                try
                {
                    Thread.sleep(150) // 送信前にちょっと止めてみる
                    val command = "register_myset.cgi"
                    val parameter = ""
                    AppSingleton.cameraControl.sendGetCommand(command, parameter, object: IOmdsOperationCallback
                    {
                        override fun operationResult(isChange: Boolean, responseText: String)
                        {
                            try
                            {
                                Thread.sleep(150) // 送信前にちょっと止めてみる
                                AppSingleton.cameraControl.changeRunMode(
                                    "standalone",
                                    object : IOmdsOperationCallback {
                                        override fun operationResult(
                                            isChange: Boolean,
                                            responseText: String
                                        ) {
                                            // パラメータ設定の完了
                                            activity?.runOnUiThread {
                                                controlButtons(
                                                    true,
                                                    requireActivity().getString(R.string.message_apply_settings_done)
                                                )
                                            }
                                        }
                                    }
                                )
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
            thread.start()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    private fun applySystemSettings()
    {
        try
        {
            if (::myView.isInitialized)
            {
                // 現在の画面設定を取得し、システムに反映させる
                //val wifiCh = myView.findViewById<AppCompatSpinner>(R.id.select_wifi_ch).selectedItem.toString()
                val sleep = myView.findViewById<AppCompatSpinner>(R.id.select_sleep).selectedItem.toString()
                val soundLevel = myView.findViewById<AppCompatSpinner>(R.id.select_sound_volume).selectedItem.toString()
                val lensReset = myView.findViewById<AppCompatSpinner>(R.id.select_lens_reset).selectedItem.toString()
                val imageSize = myView.findViewById<AppCompatSpinner>(R.id.select_image_quality).selectedItem.toString()
                val compressionRatio = myView.findViewById<AppCompatSpinner>(R.id.select_compression_ratio).selectedItem.toString()
                val raw = myView.findViewById<AppCompatSpinner>(R.id.select_record_raw).selectedItem.toString()

                // ---- 送信用のコマンドを作成する
                var body = "<?xml version=\"1.0\"?><set>"
                body += "<prop name=\"RAW\"><value>$raw</value></prop>"
                body += "<prop name=\"SLEEP\"><value>$sleep</value></prop>"
                body += "<prop name=\"SOUND_VOLUME_LEVEL\"><value>$soundLevel</value></prop>"
                body += "<prop name=\"LENS_RESET\"><value>$lensReset</value></prop>"
                body += "<prop name=\"IMAGESIZE\"><value>$imageSize</value></prop>"
                body += "<prop name=\"COMPRESSIBILITY_RATIO\"><value>$compressionRatio</value></prop>"
                body += "</set>"

                sendSystemSetting(body)
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    override fun setCommandFinished(isFinished: Boolean)
    {
        // IBusyProgressDrawer
        Log.v(TAG, "setCommandFinished($isFinished)")
    }

    override fun controlNextButton(isEnabled: Boolean)
    {
        // IBusyProgressDrawer
        Log.v(TAG, "controlNextButton($isEnabled)")
    }

    override fun controlPreviousButton(isEnabled: Boolean)
    {
        // IBusyProgressDrawer
        Log.v(TAG, "controlPreviousButton($isEnabled)")
    }

    override fun controlCloseButton(isEnabled: Boolean)
    {
        // IBusyProgressDrawer
        Log.v(TAG, "controlCloseButton($isEnabled)")
    }

    override fun setMessageText(message: String, isAppend: Boolean)
    {
        // IBusyProgressDrawer
        Log.v(TAG, "setMessageText($isAppend) $message")
    }

    override fun setResponseText(message: String, isAppend: Boolean)
    {
        // IBusyProgressDrawer
        Log.v(TAG, "setResponseText($isAppend) $message")
    }
    
    companion object
    {
        val TAG: String = SystemSettingsDialog::class.java.simpleName

        fun newInstance(context: FragmentActivity, command: ICameraMaintenanceCommandSequence, vibrator: IVibrator?): SystemSettingsDialog
        {
            val instance = SystemSettingsDialog()
            instance.prepare(context, command, vibrator)
            return (instance)
        }
    }
}