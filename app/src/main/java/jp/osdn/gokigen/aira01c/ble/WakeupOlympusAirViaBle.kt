package jp.osdn.gokigen.aira01c.ble

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentActivity
import jp.osdn.gokigen.aira01c.R
import jp.osdn.gokigen.aira01c.ble.ICameraPowerOn.IPowerOnCameraCallback

class WakeupOlympusAirViaBle(private val context: FragmentActivity, private val device: BluetoothDevice, private val callback: IPowerOnCameraCallback)
{
    fun wake() : Boolean
    {
        try
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            {
                // https://stackoverflow.com/questions/39272712/ の情報に基づき実装
                val bleCallback = OlympusAirBleCallback(callback)
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED)
                {
                    Toast.makeText(context, context.getString(R.string.ble_no_connect_permission), Toast.LENGTH_SHORT).show()
                    return (false)
                }
                device.connectGatt(context, false, bleCallback)
            }
            else
            {
                // LOLLIPOP より前のOSバージョンでは、起動しない（未サポート）
                Log.v(TAG, "Prior to api 21 devices do not support a wake up function via BLUETOOTH LE.")
                return (false)
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
        return (true)
    }

    companion object
    {
        private val TAG = WakeupOlympusAirViaBle::class.java.simpleName
    }
}
