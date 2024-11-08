package jp.osdn.gokigen.aira01c.ble

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.os.Build
import android.util.Log

class BleAdapterGetter(val context: Context)
{
    init
    {
        Log.v(TAG, "BleAdapterGetter()")
    }
    fun getBleAdapter(): BluetoothAdapter?
    {
        try
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            {
                try
                {
                    // ----- https://developer.android.com/develop/connectivity/bluetooth/setup
                    val bluetoothManager: BluetoothManager = context.getSystemService(BluetoothManager::class.java)
                    return (bluetoothManager.adapter)
                }
                catch (e: Exception)
                {
                    e.printStackTrace()
                }
            }
            try
            {
                // ----- OLD Version
                @Suppress("DEPRECATION")
                return (BluetoothAdapter.getDefaultAdapter())
            }
            catch (e: Exception)
            {
                e.printStackTrace()
            }
        }
        catch (ee: Exception)
        {
            ee.printStackTrace()
        }
        return (null)
    }

    companion object
    {
        private val TAG = BleAdapterGetter::class.java.simpleName
    }
}
