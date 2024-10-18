package jp.osdn.gokigen.aira01c.ble

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.os.Build

class BleAdapterGetter(val context: Context)
{
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
            else
            {
                // ----- OLD Version
                try
                {
                    @Suppress("DEPRECATION")
                    return (BluetoothAdapter.getDefaultAdapter())
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
        return (null)
    }

    companion object
    {
        private val TAG = BleAdapterGetter::class.java.simpleName
    }
}
