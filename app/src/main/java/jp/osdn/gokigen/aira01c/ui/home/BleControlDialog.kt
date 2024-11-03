package jp.osdn.gokigen.aira01c.ui.home

import android.annotation.SuppressLint
import android.app.Dialog
import android.bluetooth.BluetoothDevice
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
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatSpinner
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import androidx.preference.PreferenceManager
import jp.osdn.gokigen.aira01c.AppSingleton
import jp.osdn.gokigen.aira01c.R
import jp.osdn.gokigen.aira01c.ble.BleDeviceScanner
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
    private lateinit var deviceScanner: BleDeviceScanner

    private var selectedBleDevice : MyBleDevice? = null
    private var container: ViewGroup? = null
    private val myDeviceList = ArrayList<MyBleDevice>()

    private fun prepare(context: FragmentActivity, bleDeviceList: MyBleAdapter, cameraPowerOn: ICameraPowerOn)
    {
        this.myContext = context
        this.bleDeviceList = bleDeviceList
        this.cameraPowerOn = cameraPowerOn
        this.preferences = PreferenceManager.getDefaultSharedPreferences(context)
        this.deviceScanner = BleDeviceScanner(context, bleDeviceList, this)
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

    private fun showDialog(): Dialog
    {
        // 表示するダイアログの生成
        if (!::alertDialog.isInitialized)
        {
            alertDialog = AlertDialog.Builder(myContext)
        }
        if (!::myView.isInitialized)
        {
            val inflater = myContext.layoutInflater
            myView = inflater.inflate(R.layout.dialog_ble_control, container, false)
        }
        setupDialog()

        alertDialog.setView(myView)
        alertDialog.setCancelable(true)

        try
        {
            // Bluetooth LE 端末（Olympus Air）を探す
            scanNeighbourDevices()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
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
                myView.findViewById<ImageButton>(R.id.btn_scan_device).setOnClickListener(this)
                myView.findViewById<EditText>(R.id.ble_passcode).setText(preferences.getString(PREFERENCE_BLE_PASSCODE, ""))
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

                        val spinner = myView.findViewById<AppCompatSpinner>(R.id.paired_devices_selection)
                        spinner.isEnabled = false

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
            R.id.btn_scan_device -> {
                // ----- Bluetooth のスキャン
                AppSingleton.vibrator.vibrate(requireContext(), IVibrator.VibratePattern.SIMPLE_MIDDLE)
                scanNeighbourDevices()
            }
            else -> {
                Log.v(TAG, " onClick() : ${view.id}")
            }
        }
    }

    private fun scanNeighbourDevices()
    {
        try
        {
            deviceScanner.scanDevices()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
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

    private fun getLastUsedBleDevice() : MyBleDevice?
    {
        var myBleDevice : MyBleDevice? = null
        try
        {
            if (::preferences.isInitialized)
            {
                val deviceName = preferences.getString(PREFERENCE_LAST_BLE_DEVICE_NAME, "") ?: ""
                val deviceAddress = preferences.getString(PREFERENCE_LAST_BLE_DEVICE_ADDRESS, "") ?: ""
                if ((deviceName.isNotEmpty())&&(deviceAddress.isNotEmpty()))
                {
                    myBleDevice = MyBleDevice(deviceName, deviceAddress)
                }
                Log.v(TAG, " getLastUsedBleDevice() name: $deviceName  address: $deviceAddress")
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
        return (myBleDevice)
    }

    private fun storeLastUsedBleDevice(name: String, address: String)
    {
        myContext.runOnUiThread {
            try
            {
                val editor = preferences.edit()
                editor.putString(PREFERENCE_LAST_BLE_DEVICE_NAME, name)
                editor.putString(PREFERENCE_LAST_BLE_DEVICE_ADDRESS, address)
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
    override fun onProgress(message: String, isLineFeed: Boolean)
    {
        try
        {
            val field = myView.findViewById<TextView>(R.id.ble_message_response)
            myContext.runOnUiThread {
                if (isLineFeed)
                {
                    field.text = "${field.text}\r\n$message"
                }
                else
                {
                    field.text = "${field.text}$message"
                }
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
            val message = if (isExecute) {
                // ----- 起動成功時...
                if (selectedBleDevice != null)
                {
                    val name: String = selectedBleDevice?.name ?: ""
                    val address: String = selectedBleDevice?.id ?: ""
                    storeLastUsedBleDevice(name, address)
                }
                requireContext().getString(R.string.ble_wake_success)
            }
            else
            {
                // ----- 起動失敗時
                requireContext().getString(R.string.ble_wake_failure)
            }
            val field = myView.findViewById<TextView>(R.id.ble_message_response)
            val spinner = myView.findViewById<AppCompatSpinner>(R.id.paired_devices_selection)
            val button = myView.findViewById<Button>(R.id.dialog_ble_button_power_on)
            myContext.runOnUiThread {
                field.text = "${field.text}\r\n$message\r\n"
                spinner.isEnabled = true
                button.isEnabled = true
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    @SuppressLint("MissingPermission")
    override fun finishedScan(deviceList: Map<String, BluetoothDevice>)
    {
        try
        {
            requireActivity().runOnUiThread {
                try
                {
                    myDeviceList.clear()
                    val adapter = ArrayAdapter<String>(this.requireContext(), android.R.layout.simple_spinner_item)
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    var isFirst = true
                    val lastUsedBleDevice = getLastUsedBleDevice()
                    if (lastUsedBleDevice != null)
                    {
                        selectedBleDevice = lastUsedBleDevice
                        isFirst = false
                        myDeviceList.add(lastUsedBleDevice)
                        adapter.add("${lastUsedBleDevice.name}(${lastUsedBleDevice.id}) ${requireContext().getString(R.string.ble_last_use)}")
                    }
                    for (device in deviceList)
                    {
                        if (device.value.name != null)
                        {
                            val myDevice = MyBleDevice(device.value.name, device.value.address)
                            myDeviceList.add(myDevice)
                            if (isFirst)
                            {
                                selectedBleDevice = myDevice
                                isFirst = false
                            }
                            adapter.add("${device.value.name}(${device.value.address})")
                        }
                    }
                    if (myDeviceList.isEmpty())
                    {
                        adapter.add("- - - - -")
                    }
                    val spinner: AppCompatSpinner = myView.findViewById(R.id.paired_devices_selection)
                    spinner.adapter = adapter
                    spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long)
                        {
                            Log.v(TAG, "onItemSelected(parent: $parent, view: $view, pos: $pos, id: $id)")
                            try
                            {
                                selectedBleDevice = myDeviceList[pos]
                            }
                            catch (e: Exception)
                            {
                                e.printStackTrace()
                            }
                        }

                        override fun onNothingSelected(p0: AdapterView<*>?)
                        {
                            Log.v(TAG, "onNothingSelected()")
                        }
                    }
                }
                catch (ee: Exception)
                {
                    ee.printStackTrace()
                }
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
        private const val PREFERENCE_LAST_BLE_DEVICE_NAME = "last_ble_name"
        private const val PREFERENCE_LAST_BLE_DEVICE_ADDRESS = "last_ble_address"

        fun newInstance(context: FragmentActivity, bleDeviceList: MyBleAdapter, cameraPowerOn: ICameraPowerOn): BleControlDialog {
            val instance = BleControlDialog()
            instance.prepare(context, bleDeviceList, cameraPowerOn)
            return (instance)
        }
    }
}
