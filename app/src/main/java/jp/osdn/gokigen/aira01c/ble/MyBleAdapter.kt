package jp.osdn.gokigen.aira01c.ble

import android.Manifest.permission
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import jp.osdn.gokigen.aira01c.R

class MyBleAdapter(val context: FragmentActivity)
{
    private val deviceList : MutableList<MyBleDevice> = ArrayList()
    private var isReadyDeviceList = false
    private val bleAdapterGetter = BleAdapterGetter(context)

    fun prepare()
    {
        context.runOnUiThread {
            try
            {
                // ----- UIスレッドで PERMISSION の取得と、デバイスリストを作成する
                Log.v(TAG, " ----- SET PERMISSIONS(BLUETOOTH) -----")
                if (!allPermissionsGranted())
                {
                    val requestPermission = context.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
                        ActivityCompat.requestPermissions(context, REQUIRED_BLUETOOTH_PERMISSIONS, REQUEST_NEED_BLUETOOTH_PERMISSIONS)
                        if(!allPermissionsGranted())
                        {
                            // Abort launch application because required permissions was rejected.
                            Toast.makeText(context, context.getString(R.string.permission_not_granted), Toast.LENGTH_SHORT).show()
                            Log.v(TAG, "----- APPLICATION LAUNCH ABORTED -----")
                        }
                        else
                        {
                            // ----- デバイスリストを取得
                            getDeviceList()
                            isReadyDeviceList = true
                        }
                    }
                    requestPermission.launch(REQUIRED_BLUETOOTH_PERMISSIONS)
                }
                else
                {
                    // ----- デバイスリストを取得
                    getDeviceList()
                    isReadyDeviceList = true
                }
            }
            catch (e: Exception)
            {
                e.printStackTrace()
                isReadyDeviceList = false
            }
        }
    }

    fun getBondedDeviceList(): List<MyBleDevice>
    {
        return (deviceList)
    }

    fun getBleAdapter(): BluetoothAdapter?
    {
        return (bleAdapterGetter.getBleAdapter())
    }

    private fun getDeviceList()
    {
        try
        {
            deviceList.clear()
            val bluetoothAdapter = bleAdapterGetter.getBleAdapter()
            if (bluetoothAdapter != null)
            {
                val bondedDevices =  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (ActivityCompat.checkSelfPermission(context, permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED)
                    {
                        bluetoothAdapter.bondedDevices
                    }
                    else
                    {
                        // ----- 基本、ここは通らないはず (ここを通る前にPermissionを取得しているはずなので)
                        Log.v(TAG, "getDeviceList() : Permission not grant... but, try to get bonded devices")
                        bluetoothAdapter.bondedDevices
                    }
                }
                else
                {
                    bluetoothAdapter.bondedDevices
                }
                for (bt in bondedDevices)
                {
                    deviceList.add(MyBleDevice(bt.name, bt.address))
                }

                if (!bluetoothAdapter.isEnabled)
                {
                    // Bluetoothの設定がOFFだった
                    Log.v(TAG, "Bluetooth is currently off.")
                    context.runOnUiThread {
                        Toast.makeText(
                            context,
                            context.getString(R.string.ble_setting_is_off),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    private fun allPermissionsGranted() : Boolean
    {
        var result = true
        for (param in REQUIRED_BLUETOOTH_PERMISSIONS)
        {
            if (ContextCompat.checkSelfPermission(context, param) != PackageManager.PERMISSION_GRANTED)
            {
                if ((param == permission.BLUETOOTH)&&(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S))
                {
                    //　この場合は権限付与の判断を除外 (デバイスが (12/SDK31) 以上で、BLUETOOTH がない場合）
                }
                else if ((param == permission.BLUETOOTH_ADMIN)&&(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S))
                {
                    //　この場合は権限付与の判断を除外 (デバイスが (12/SDK31) 以上で、BLUETOOTH_ADMIN がない場合）
                }
                else if ((param == permission.BLUETOOTH_SCAN)&&(Build.VERSION.SDK_INT < Build.VERSION_CODES.S))
                {
                    //　この場合は権限付与の判断を除外 (デバイスが (12/SDK31) よりも古く、BLUETOOTH_SCAN がない場合）
                }
                else if ((param == permission.BLUETOOTH_CONNECT)&&(Build.VERSION.SDK_INT < Build.VERSION_CODES.S))
                {
                    //　この場合は権限付与の判断を除外 (デバイスが (12/SDK31) よりも古く、BLUETOOTH_CONNECT がない場合）
                }
                else
                {
                    // ----- 権限が得られなかった場合...
                    Log.v(TAG, " Permission: $param : ${Build.VERSION.SDK_INT}")
                    result = false
                }
            }
        }
        return (result)
    }

    companion object
    {
        private val TAG = MyBleAdapter::class.java.simpleName
        private const val REQUEST_NEED_BLUETOOTH_PERMISSIONS = 1110

        @SuppressLint("InlinedApi")
        private val REQUIRED_BLUETOOTH_PERMISSIONS = arrayOf(
            permission.ACCESS_FINE_LOCATION,
            permission.ACCESS_COARSE_LOCATION,
            permission.BLUETOOTH,
            permission.BLUETOOTH_ADMIN,
            permission.BLUETOOTH_SCAN,
            permission.BLUETOOTH_CONNECT,
        )
    }
}
