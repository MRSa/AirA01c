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

class StandaloneShootingSetDialog: DialogFragment(), View.OnClickListener, IBusyProgressDrawer
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
            myView = inflater.inflate(R.layout.dialog_standalone_shooting, container, false)
        }
        setupStandaloneShootingSettingDialog()
        queryStandaloneShootingSettings()

        alertDialog.setView(myView)
        alertDialog.setCancelable(true)

        return (alertDialog.create())
    }

    private fun queryStandaloneShootingSettings()
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
                    val body = "<?xml version=\"1.0\"?><desclist><prop name=\"TAKEMODE\"/><prop name=\"COLORTONE\"/><prop name=\"WB\"/><prop name=\"ISO\"/><prop name=\"ASPECT_RATIO\"/><prop name=\"QUALITY_MOVIE\"/><prop name=\"APERTURE\"/><prop name=\"SHUTTER\"/></desclist>"

                    AppSingleton.cameraControl.sendPostCommand(command, parameter, body, object: IOmdsOperationCallback
                    {
                        override fun operationResult(isChange: Boolean, responseText: String)
                        {
                            try
                            {
                                activity?.runOnUiThread {
                                    // 取得したシステム設定を画面に反映させる
                                    extractStandaloneShootingSettings(responseText)
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
            controlButtons(false, requireActivity().getString(R.string.message_standalone_get_current_settings))
            thread.start()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    private fun extractStandaloneShootingSettings(responseText: String)
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
            val messageId = if (parseCount > 0) { R.string.message_standalone_parameter_selection_ready } else { R.string.message_standalone_parameter_get_failure }
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
                "TAKEMODE" -> { setupSpinner(R.id.select_takemode, enumList, value)}
                "COLORTONE" -> { setupSpinner(R.id.select_colortone, enumList, value) }
                "WB" -> { setupSpinner(R.id.select_wb, enumList, value)}
                "ISO" -> { setupSpinner(R.id.select_iso, enumList, value) }
                "ASPECT_RATIO" -> { setupSpinner(R.id.select_aspect_ratio, enumList, value) }
                "QUALITY_MOVIE" -> { setupSpinner(R.id.select_quality_movie, enumList, value) }
                "APERTURE" -> { setupSpinner(R.id.select_aperture, enumList, value) }
                "SHUTTER" -> { setupSpinner(R.id.select_shutter, enumList, value) }
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
                myView.findViewById<TextView>(R.id.text_standalone_information).text = message
                myView.findViewById<ImageButton>(R.id.button_standalone_reload).isEnabled = isEnabled
                myView.findViewById<Button>(R.id.btn_apply_standalone).isEnabled = isEnabled
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    private fun setupStandaloneShootingSettingDialog()
    {
        // -------- ダイアログの処理メイン
        try
        {
            if (::myView.isInitialized)
            {
                myView.findViewById<ImageButton>(R.id.button_standalone_reload).setOnClickListener(this)
                myView.findViewById<Button>(R.id.btn_apply_standalone).setOnClickListener(this)
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
                R.id.button_standalone_reload -> { queryStandaloneShootingSettings() }
                R.id.btn_apply_standalone -> { applyStandaloneShootingSettings() }
                else -> { }
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    private fun sendCameraPropertis(body: String)
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
                                                    sendCameraPropertiesImpl(body)
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
            controlButtons(false, requireActivity().getString(R.string.message_standalone_apply_settings))
            thread.start()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    private fun sendCameraPropertiesImpl(body: String)
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
                                                    requireActivity().getString(R.string.message_standalone_apply_settings_done)
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

    private fun applyStandaloneShootingSettings()
    {
        try
        {
            if (::myView.isInitialized)
            {
                // 現在の画面設定を取得し、システムに反映させる
                val takeMode = myView.findViewById<AppCompatSpinner>(R.id.select_takemode).selectedItem.toString()
                val colorTone = myView.findViewById<AppCompatSpinner>(R.id.select_colortone).selectedItem.toString()
                val whiteBalance = myView.findViewById<AppCompatSpinner>(R.id.select_wb).selectedItem.toString()
                val iso = myView.findViewById<AppCompatSpinner>(R.id.select_iso).selectedItem.toString()
                val aspectRatio = myView.findViewById<AppCompatSpinner>(R.id.select_aspect_ratio).selectedItem.toString()
                val movieQuality = myView.findViewById<AppCompatSpinner>(R.id.select_quality_movie).selectedItem.toString()
                val aperture = myView.findViewById<AppCompatSpinner>(R.id.select_aperture).selectedItem.toString()
                val shutterSpeed = myView.findViewById<AppCompatSpinner>(R.id.select_shutter).selectedItem.toString()

                // ---- 送信用のコマンドを作成する
                var body = "<?xml version=\"1.0\"?><set>"
                body += "<prop name=\"COLORTONE\"><value>$colorTone</value></prop>"
                body += "<prop name=\"TAKEMODE\"><value>$takeMode</value></prop>"
                body += "<prop name=\"WB\"><value>$whiteBalance</value></prop>"
                body += "<prop name=\"ISO\"><value>$iso</value></prop>"
                body += "<prop name=\"ASPECT_RATIO\"><value>$aspectRatio</value></prop>"
                body += "<prop name=\"QUALITY_MOVIE\"><value>$movieQuality</value></prop>"
                body += "<prop name=\"APERTURE\"><value>$aperture</value></prop>"
                body += "<prop name=\"SHUTTER\"><value>$shutterSpeed</value></prop>"
                body += "</set>"

                sendCameraPropertis(body)
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
        val TAG: String = StandaloneShootingSetDialog::class.java.simpleName

        fun newInstance(context: FragmentActivity, command: ICameraMaintenanceCommandSequence, vibrator: IVibrator?): StandaloneShootingSetDialog
        {
            val instance = StandaloneShootingSetDialog()
            instance.prepare(context, command, vibrator)
            return (instance)
        }
    }
}