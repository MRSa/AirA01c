package jp.osdn.gokigen.aira01c.ble

import android.bluetooth.BluetoothDevice
import android.util.Log
import androidx.fragment.app.FragmentActivity
import jp.osdn.gokigen.aira01c.R
import jp.osdn.gokigen.aira01c.ble.ICameraPowerOn.IPowerOnCameraCallback

/**
 * BLE経由でカメラの電源を入れてしまうクラス
 *
 */
class PowerOnCamera(private val context: FragmentActivity) : ICameraPowerOn
{
    //private var myCameraList: MutableList<CameraBleSetArrayItem>? = null
    private var myBluetoothDevice: BluetoothDevice? = null
    private var myBtDevicePassCode = ""
    private val bleAdapterGetter = BleAdapterGetter(context)

    init
    {
        Log.v(TAG, "PowerOnCamera()")
        //setupCameraList()
    }

    override fun wakeup(target: MyBleDevice, code: String, callback: IPowerOnCameraCallback?)
    {
        Log.v(TAG, "PowerOnCamera::wakeup() : ${target.name} (${target.id}) [$code]")
        callback?.onStart("${context.getString(R.string.ble_wake_start)}: ${target.name}")








/*
        try
        {
            var btAdapter = BluetoothAdapter.getDefaultAdapter()
            if (!btAdapter.isEnabled)
            {
                // Bluetoothの設定がOFFだった
                Log.v(TAG, "Bluetooth is currently off.")
                context.runOnUiThread { // Toastで カメラ起動エラーがあったことを通知する
                    Toast.makeText(
                        context,
                        context.getString(R.string.ble_setting_is_off),
                        Toast.LENGTH_LONG
                    ).show()
                }
                callback.wakeupExecuted(false)
                return
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
            callback.wakeupExecuted(false)
            return
        }

        val btMgr: BluetoothManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
        {
            // BLE のサービスを取得
            btMgr = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            if (btMgr == null) {
                // Bluetooth LEのサポートがない場合は、何もしない
                Log.v(TAG, "PowerOnCamera::wakeup() NOT SUPPORT BLE...")

                // BLEの起動はしなかった...
                callback.wakeupExecuted(false)
                return
            }
            val deviceList: List<CameraBleSetArrayItem>? = myCameraList

            //  BLE_SCAN_TIMEOUT_MILLIS の間だけBLEのスキャンを実施する
            var thread = Thread(Runnable {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    class bleScanCallback : LeScanCallback {
                        override fun onLeScan(
                            bluetoothDevice: BluetoothDevice,
                            i: Int,
                            bytes: ByteArray
                        ) {
                            try {
                                val btDeviceName = bluetoothDevice.name
                                // Log.v(TAG, "onLeScan() " + btDeviceName);   // BluetoothDevice::getName() でログ出力してくれるので
                                for (device in deviceList) {
                                    val btName: String = device.getBtName()
                                    // Log.v(TAG, "onLeScan() [" + btName + "]");
                                    if (btName == btDeviceName) {
                                        // マイカメラ発見！
                                        // 別スレッドで起動する
                                        myBluetoothDevice = bluetoothDevice
                                        myBtDevicePassCode = device.getBtPassCode()
                                        break
                                    }
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }

                        fun reset() {
                            try {
                                myBluetoothDevice = null
                                myBtDevicePassCode = ""
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }

                    var scanCallback = bleScanCallback()
                    try {
                        // スキャン開始
                        scanCallback.reset()
                        var adapter = btMgr.adapter
                        if (!adapter.startLeScan(scanCallback)) {
                            // Bluetooth LEのスキャンが開始できなかった場合...
                            Log.v(TAG, "Bluetooth LE SCAN START fail...")
                            callback.wakeupExecuted(false)
                            return@Runnable
                        }
                        Log.v(TAG, "BT SCAN STARTED")
                        var passed = 0
                        while (passed < BLE_SCAN_TIMEOUT_MILLIS) {
                            // BLEデバイスが見つかったときは抜ける...
                            if (myBluetoothDevice != null) {
                                break
                            }

                            // BLEのスキャンが終わるまで待つ
                            Thread.sleep(BLE_WAIT_DURATION.toLong())
                            passed = passed + BLE_WAIT_DURATION
                        }
                        // スキャンを止める
                        adapter.stopLeScan(scanCallback)
                        Log.v(TAG, "BT SCAN STOPPED")

                        // カメラの起動
                        callback.wakeupExecuted(
                            wakeupViaBle(
                                adapter,
                                myBluetoothDevice,
                                myBtDevicePassCode
                            )
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Log.v(TAG, "Bluetooth LE SCAN EXCEPTION...")
                        callback.wakeupExecuted(false)

                        try {
                            val btName =
                                if ((myBluetoothDevice != null)) myBluetoothDevice!!.name else ""
                            context.runOnUiThread { // Toastで カメラ起動エラーがあったことを通知する
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.launch_fail_via_ble) + btName,
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        } catch (ee: Exception) {
                            ee.printStackTrace()
                        }
                    }
                    Log.v(TAG, "Bluetooth LE SCAN STOPPED")
                } // if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            })
            thread.start()
        }
*/
    }

    /*
    private fun wakeupViaBle(
        adapter: BluetoothAdapter?,
        myBluetoothDevice: BluetoothDevice?,
        passCode: String
    ): Boolean {
        if (adapter == null) {
            Log.v(TAG, " BluetoothAdapter is UNKNOWN(null).")
            return (false)
        }

        if (myBluetoothDevice == null) {
            Log.v(TAG, " Bt Device is UNKNOWN(null).")
            return (false)
        }

        Log.v(
            TAG,
            "WAKE UP CAMERA : " + myBluetoothDevice.name + " [" + myBluetoothDevice.address + "]"
        )
        try {
            Log.v(TAG, "PASSCODE : $passCode")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                // デバイスに接続する
                myBluetoothDevice.connectGatt(context, false, BleConnectionApi18())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return (true)
    }

    private fun setupCameraList() {
        myCameraList = ArrayList<CameraBleSetArrayItem>()

        var preferences = PreferenceManager.getDefaultSharedPreferences(context)
        for (index in 1..ICameraBleProperty.MAX_STORE_PROPERTIES) {
            var idHeader = String.format(Locale.ENGLISH, "%03d", index)
            var prefDate =
                preferences.getString(idHeader + ICameraBleProperty.DATE_KEY, "")!!
            if (prefDate.length <= 0) {
                // 登録が途中までだったとき
                break
            }
            var btName =
                preferences.getString(idHeader + ICameraBleProperty.NAME_KEY, "")!!
            var btCode =
                preferences.getString(idHeader + ICameraBleProperty.CODE_KEY, "")!!
            myCameraList!!.add(CameraBleSetArrayItem(idHeader, btName, btCode, prefDate))
        }
        Log.v(TAG, "setupCameraList() : " + myCameraList!!.size)
    }
*/
    companion object
    {
        private val TAG = PowerOnCamera::class.java.simpleName
        private const val BLE_SCAN_TIMEOUT_MILLIS = 5 * 1000 // 5秒間
        private const val BLE_WAIT_DURATION = 100 // 100ms間隔
    }
}
