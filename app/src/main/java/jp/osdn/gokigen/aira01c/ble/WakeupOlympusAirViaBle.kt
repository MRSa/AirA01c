package jp.osdn.gokigen.aira01c.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.os.Build
import android.util.Log
import androidx.fragment.app.FragmentActivity
import jp.osdn.gokigen.aira01c.ble.ICameraPowerOn.IPowerOnCameraCallback

class WakeupOlympusAirViaBle(private val context: FragmentActivity, private val device: BluetoothDevice, private val callback: IPowerOnCameraCallback)
{
    @SuppressLint("MissingPermission")
    fun wake() : Boolean
    {
        try
        {
            Log.v(TAG, "wake()")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            {
                // https://stackoverflow.com/questions/39272712/ の情報に基づき実装
                val bleCallback = OlympusAirBleCallback(context, callback)
                device.connectGatt(context, false, bleCallback)
            }
            else
            {
                // LOLLIPOP より前のOSバージョンでは、起動しない（未サポート）
                Log.v(TAG, "Prior to api 21 (before Lollipop device) devices do not support a wake up function via BLUETOOTH LE.")
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
