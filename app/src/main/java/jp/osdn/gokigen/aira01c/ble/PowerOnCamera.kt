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

/**
 * BLE経由でカメラの電源を入れてしまうクラス
 *
 */
class PowerOnCamera(private val context: FragmentActivity, private val bleAdapterGetter: MyBleAdapter) : ICameraPowerOn, IPowerOnCameraCallback
{
    private var targetBleDeviceName = ""
    private var targetBleDeviceAddress = ""
    private var foundBleDevice = false
    private lateinit var callback: IPowerOnCameraCallback

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
                    if (checkDevice(device.name, device.address))
                    {
                        if (::callback.isInitialized)
                        {
                            callback.onProgress("${context.getString(R.string.ble_device_found)} ${device.name} (${device.address})")
                        }
                        stopBleScanApi21()
                        wakeUpImpl(device)
                    }
                }
            }
            catch (e: Exception)
            {
                e.printStackTrace()
            }
        }
    }

    override fun wakeup(target: MyBleDevice, code: String, callback: IPowerOnCameraCallback)
    {
        this.callback = callback
        targetBleDeviceName = target.name
        targetBleDeviceAddress = target.id
        Log.v(TAG, "PowerOnCamera::wakeup() : $targetBleDeviceName ($targetBleDeviceAddress) [$code]")

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
            // === ターゲットの名称（と、アドレス）が取得できるかチェック
            if ((targetBleDeviceName.isEmpty())||(targetBleDeviceAddress.isEmpty()))
            {
                context.runOnUiThread {
                    try
                    {
                        Toast.makeText(
                            context,
                            context.getString(R.string.ble_target_is_empty),
                            Toast.LENGTH_SHORT
                        ).show()
                        Log.v(TAG, "----- BLUETOOTH DEVICE NAME IS EMPTY -----")
                    }
                    catch (e: Exception)
                    {
                        e.printStackTrace()
                    }
                }
                return
            }
            callback.onStart("${context.getString(R.string.ble_wake_start)}: ${target.name}")
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

    override fun cancelWakeup()
    {
        try
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            {
                stopBleScanApi21()
            }
            else
            {
                stopBleScan()
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    private fun checkDevice(deviceName: String, deviceAddress: String) : Boolean
    {
        try
        {
            Log.v(TAG, "checkDevice() : $deviceName ($deviceAddress)")
            if ((deviceName == targetBleDeviceName)&&(deviceAddress == targetBleDeviceAddress))
            {
                // デバイスを見つけた
                Log.v(TAG, " =-=-=-=-= FOUND BLE DEVICE : $targetBleDeviceName ($targetBleDeviceAddress) =-=-=-=-=")
                return (true)
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
        return (false)
    }

    @SuppressLint("MissingPermission")
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private fun scanBleDeviceApi21(bleAdapter: BluetoothAdapter)
    {
        try
        {
            // スキャン開始
            foundBleDevice = false
            val scanner = bleAdapter.bluetoothLeScanner
            val filter = ScanFilter.Builder()
                .setDeviceName(targetBleDeviceName)
                .setDeviceAddress(targetBleDeviceAddress)
                .build()
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

    private fun wakeUpImpl(device: BluetoothDevice)
    {
        try
        {
            // ここでカメラの起動 実処理 (別カメラの起動にも流用できるよう、クラスを分割
            Log.v(TAG, "wakeUpImpl()")
            WakeupOlympusAirViaBle(context, device, this).wake()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    override fun onStart(message: String)
    {
        if (::callback.isInitialized)
        {
            try
            {
                // --- カメラの起動開始を通知する
                callback.onStart(message)
            }
            catch (e: Exception)
            {
                e.printStackTrace()
            }
        }
    }

    override fun onProgress(message: String, isLineFeed: Boolean)
    {
        if (::callback.isInitialized)
        {
            try
            {
                // --- カメラの起動状況を通知する
                callback.onProgress(message, isLineFeed)
            }
            catch (e: Exception)
            {
                e.printStackTrace()
            }
        }
    }

    override fun wakeupExecuted(isExecute: Boolean)
    {
        if (::callback.isInitialized)
        {
            try
            {
                // --- カメラの起動結果を通知する
                callback.wakeupExecuted(isExecute)
            }
            catch (e: Exception)
            {
                e.printStackTrace()
            }
        }
    }

    override fun finishedScan(deviceList: Map<String, BluetoothDevice>)
    {
        if (::callback.isInitialized)
        {
            try
            {
                // --- BLEデバイスリストを応答する
                callback.finishedScan(deviceList)
            }
            catch (e: Exception)
            {
                e.printStackTrace()
            }
        }    }

    companion object
    {
        private val TAG = PowerOnCamera::class.java.simpleName
    }
}
