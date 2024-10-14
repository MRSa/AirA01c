package jp.osdn.gokigen.aira01c.ble

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context

class MyBleAdapter(val context: Context)
{
    fun getBondedDevices(): List<String>
    {
        val s: MutableList<String> = ArrayList()
        try
        {
            val bluetoothManager: BluetoothManager = context.getSystemService(BluetoothManager::class.java)
            val bluetoothAdapter: BluetoothAdapter = bluetoothManager.adapter ?: return (s)
            val bondedDevices = bluetoothAdapter.bondedDevices
            for (bt in bondedDevices)
            {
                s.add(bt.name)
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
        return (s)
    }
}
