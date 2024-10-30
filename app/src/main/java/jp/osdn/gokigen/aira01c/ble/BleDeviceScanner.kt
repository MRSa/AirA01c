package jp.osdn.gokigen.aira01c.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.FragmentActivity
import jp.osdn.gokigen.aira01c.R
import jp.osdn.gokigen.aira01c.ble.ICameraPowerOn.IPowerOnCameraCallback

class BleDeviceScanner(private val context: FragmentActivity, private val bleAdapterGetter: MyBleAdapter, private val callback: IPowerOnCameraCallback)
{
    private val bleDeviceList = HashMap<String, BluetoothDevice>()
    private var foundDeviceCount = 0
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private val scanCallbackApi21 = object: ScanCallback()
    {
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult?)
        {
            super.onScanResult(callbackType, result)
            try {
                val device = result?.device
                if (device != null)
                {
                    Log.v(TAG, "Found device: ${device.name} (${device.address})")
                    callback.onProgress(".", false)
                    bleDeviceList[device.address] = device
                    foundDeviceCount++
                    if (foundDeviceCount > MAX_FOUND_BLE_DEVICES)
                    {
                        stopBleScanApi21()
                        callback.onProgress(context.getString(R.string.ble_scan_done), false)
                        callback.finishedScan(bleDeviceList)
                    }
                }
            }
            catch (e: Exception)
            {
                e.printStackTrace()
            }
        }
    }

    fun scanDevices()
    {
        try
        {
            // === Bluetooth Adapter を取得する
            val bleAdapter = bleAdapterGetter.getBleAdapter()
            if (bleAdapter == null)
            {
                context.runOnUiThread {
                    try
                    {
                        // Abort launch application because required permissions was rejected.
                        Toast.makeText(
                            context,
                            context.getString(R.string.ble_adapter_is_nothing),
                            Toast.LENGTH_SHORT
                        ).show()
                        Log.v(TAG, "----- CANNOT GET BLUETOOTH ADAPTER -----")
                    }
                    catch (e: Exception)
                    {
                        e.printStackTrace()
                    }
                }
                return
            }
            foundDeviceCount = 0
            bleDeviceList.clear()
            callback.onStart("${context.getString(R.string.ble_scan_start)} ")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            {
                scanBleDeviceApi21(bleAdapter)
            }
            else
            {
                scanBleDevice()
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    @SuppressLint("MissingPermission")
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private fun scanBleDeviceApi21(bleAdapter: BluetoothAdapter)
    {
        try
        {
            // スキャン開始
            val scanner = bleAdapter.bluetoothLeScanner
            val filter = ScanFilter.Builder().build()
            val settings = ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_BALANCED)
                .build()
            val scanFilter = ArrayList<ScanFilter>()
            scanFilter.add(filter)
            scanner.startScan(scanFilter, settings, scanCallbackApi21)
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
        Log.v(TAG, "Bluetooth LE SCAN STARTED")
    }

    @SuppressLint("MissingPermission")
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private fun stopBleScanApi21()
    {
        try
        {
            Log.v(TAG, "scanner.stopScan()")
            val bleAdapter = bleAdapterGetter.getBleAdapter()
            val scanner = bleAdapter?.bluetoothLeScanner
            scanner?.stopScan(scanCallbackApi21)
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    private fun stopBleScan() { }

    private fun scanBleDevice()
    {
        try
        {
            context.runOnUiThread {
                try
                {
                    Toast.makeText(
                        context,
                        context.getString(R.string.ble_control_is_limited_version),
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.v(TAG, "----- NOT SUPPORT BLE BEFORE LOLLIPOP -----")
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

    companion object
    {
        private val TAG = BleDeviceScanner::class.java.simpleName
        private const val MAX_FOUND_BLE_DEVICES = 50
    }
}
