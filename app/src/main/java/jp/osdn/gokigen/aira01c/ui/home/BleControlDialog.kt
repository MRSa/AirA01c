package jp.osdn.gokigen.aira01c.ui.home

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.DialogInterface
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatSpinner
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import androidx.preference.PreferenceManager
import jp.osdn.gokigen.aira01c.AppSingleton
import jp.osdn.gokigen.aira01c.R
import jp.osdn.gokigen.aira01c.ble.ICameraPowerOn
import jp.osdn.gokigen.aira01c.ble.ICameraPowerOn.IPowerOnCameraCallback
import jp.osdn.gokigen.aira01c.ble.MyBleAdapter
import jp.osdn.gokigen.aira01c.ble.MyBleDevice
import jp.osdn.gokigen.aira01c.camera.interfaces.IVibrator

class BleControlDialog : DialogFragment(), View.OnClickListener, IPowerOnCameraCallback
{
    private lateinit var myContext: FragmentActivity
    private lateinit var bleDeviceList: MyBleAdapter
    private lateinit var cameraPowerOn : ICameraPowerOn
    private lateinit var myView: View
    private lateinit var alertDialog: AlertDialog.Builder
    private lateinit var preferences: SharedPreferences


    private var selectedBleDevice : MyBleDevice? = null
    private var container: ViewGroup? = null

    private fun prepare(context: FragmentActivity, bleDeviceList: MyBleAdapter, cameraPowerOn: ICameraPowerOn)
    {
        this.myContext = context
        this.bleDeviceList = bleDeviceList
        this.cameraPowerOn = cameraPowerOn
        this.preferences = PreferenceManager.getDefaultSharedPreferences(context)
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

    override fun onDismiss(dialog: DialogInterface)
    {
        super.onDismiss(dialog)
        try
        {
            if (::cameraPowerOn.isInitialized)
            {
                cameraPowerOn.cancelWakeup()
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    private fun showDialog(): Dialog {
        // 表示イアログの生成
        if (!::alertDialog.isInitialized) {
            alertDialog = AlertDialog.Builder(myContext)
        }
        if (!::myView.isInitialized) {
            val inflater = myContext.layoutInflater
            myView = inflater.inflate(R.layout.dialog_ble_control, container, false)
        }
        setupDialog()

        alertDialog.setView(myView)
        alertDialog.setCancelable(true)

        prepareSpinner()

        return (alertDialog.create())
    }

    private fun setupDialog()
    {
        // -------- ダイアログの処理メイン
        try
        {
            if (::myView.isInitialized)
            {
                myView.findViewById<Button>(R.id.dialog_ble_button_power_on).setOnClickListener(this)
                myView.findViewById<Button>(R.id.ble_button_close).setOnClickListener(this)
                myView.findViewById<EditText>(R.id.ble_passcode).setText(preferences.getString(PREFERENCE_BLE_PASSCODE, ""))
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    private fun prepareSpinner()
    {
        try
        {
            // -----
            if(::bleDeviceList.isInitialized)
            {
                this.bleDeviceList.prepare()
                val deviceList = bleDeviceList.getBondedDeviceList()
                val adapter = ArrayAdapter<String>(this.requireContext(), android.R.layout.simple_spinner_item)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

                for (device in deviceList)
                {
                    adapter.add("${device.name}(${device.id})")
                }
                if (deviceList.isNotEmpty())
                {
                    selectedBleDevice = deviceList[0]
                }
                val spinner: AppCompatSpinner = myView.findViewById(R.id.paired_devices_selection)
                spinner.adapter = adapter
                spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long)
                    {
                        Log.v(TAG, "onItemSelected(parent: $parent, view: $view, pos: $pos, id: $id)")
                        selectedBleDevice = deviceList[pos]
                    }

                    override fun onNothingSelected(p0: AdapterView<*>?)
                    {
                        Log.v(TAG, "onNothingSelected()")
                    }
                }
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }


    override fun onClick(view: View)
    {
        when (view.id) {
            R.id.dialog_ble_button_power_on -> {
                // ---- Bluetooth LE 経由で電源を投入する
                AppSingleton.vibrator.vibrate(requireContext(), IVibrator.VibratePattern.SIMPLE_MIDDLE)
                try
                {
                    val passCode = myView.findViewById<EditText>(R.id.ble_passcode).text.toString()
                    storePassCode(passCode)
                    Log.v(TAG, "Pushed Wake up : ${selectedBleDevice?.name} (${selectedBleDevice?.id})")
                    if (selectedBleDevice != null)
                    {
                        val button = myView.findViewById<Button>(R.id.dialog_ble_button_power_on)
                        button.isEnabled = false
                        cameraPowerOn.wakeup(selectedBleDevice!!, passCode, this)
                    }
                }
                catch (e: Exception)
                {
                    e.printStackTrace()
                }
            }
            R.id.ble_button_close -> {
                // ----- ダイアログを閉じる
                AppSingleton.vibrator.vibrate(requireContext(), IVibrator.VibratePattern.SIMPLE_SHORT)
                dismiss()
            }
            else -> {
                Log.v(TAG, " onClick() : ${view.id}")
            }
        }
    }



    private fun storePassCode(code: String)
    {
        myContext.runOnUiThread {
            try
            {
                val editor = preferences.edit()
                editor.putString(PREFERENCE_BLE_PASSCODE, code)
                editor.apply()
            }
            catch (e: Exception)
            {
                e.printStackTrace()
            }
        }
    }

    override fun onStart(message: String)
    {
        try
        {
            val field = myView.findViewById<TextView>(R.id.ble_message_response)
            myContext.runOnUiThread {
                field.text = ""
                field.text = message
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onProgress(message: String)
    {
        try
        {
            val field = myView.findViewById<TextView>(R.id.ble_message_response)
            myContext.runOnUiThread {
                field.text = "${field.text}\r\n$message"
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    @SuppressLint("SetTextI18n")
    override fun wakeupExecuted(isExecute: Boolean)
    {
        try
        {
            val message = if (isExecute) { requireContext().getString(R.string.ble_wake_success) } else { requireContext().getString(R.string.ble_wake_failure) }
            val field = myView.findViewById<TextView>(R.id.ble_message_response)
            val button = myView.findViewById<Button>(R.id.dialog_ble_button_power_on)
            myContext.runOnUiThread {
                field.text = "${field.text}\r\n$message\r\n"
                button.isEnabled = true
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    companion object
    {
        val TAG: String = BleControlDialog::class.java.simpleName

        private const val PREFERENCE_BLE_PASSCODE = "ble_passcode"

        fun newInstance(context: FragmentActivity, bleDeviceList: MyBleAdapter, cameraPowerOn: ICameraPowerOn): BleControlDialog {
            val instance = BleControlDialog()
            instance.prepare(context, bleDeviceList, cameraPowerOn)
            return (instance)
        }
    }
}
