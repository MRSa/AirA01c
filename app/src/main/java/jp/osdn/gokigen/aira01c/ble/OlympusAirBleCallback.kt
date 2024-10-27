package jp.osdn.gokigen.aira01c.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothProfile
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import jp.osdn.gokigen.aira01c.ble.ICameraPowerOn.IPowerOnCameraCallback
import java.util.UUID

@RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
class OlympusAirBleCallback(private val callback: IPowerOnCameraCallback): BluetoothGattCallback()
{

    override fun onDescriptorWrite(
        gatt: BluetoothGatt?,
        descriptor: BluetoothGattDescriptor?,
        status: Int
    )
    {
        super.onDescriptorWrite(gatt, descriptor, status)
        Log.v(TAG, "onDescriptorWrite() : $status ${descriptor?.uuid}")
    }

    @SuppressLint("MissingPermission")
    override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int)
    {
        super.onServicesDiscovered(gatt, status)
        try
        {
            if (status == BluetoothGatt.GATT_SUCCESS)
            {
                // ----- 接続成功
                Log.v(TAG, "***---*** GATT CONNECT SUCCESS. ***---***")

                // ----- 使用可能なサービスの一覧をダンプする
                val services = gatt?.services
                if (services != null)
                {
                    for (service in services)
                    {
                        Log.v(TAG, " SERVICE [" + service.uuid + "] " + service.type)
                        val characteristics = service.characteristics
                        for (characteristic in characteristics)
                        {
                            Log.v(TAG, "    BluetoothGattCharacteristic() [" + characteristic.uuid + "] " + characteristic.permissions + " " + characteristic.properties)
                            val descriptors = characteristic.descriptors
                            for (descriptor in descriptors)
                            {
                                Log.v(TAG, "        BluetoothGattDescriptor() [" + descriptor.uuid + "] " + descriptor.permissions + " ")
                            }
                        }
                    }
                }

                // ---- Wake up camera -----
                val service = gatt?.getService(UUID.fromString("0391D26E-625B-4736-B4DA-3BB0910ECEC5")) ?: return
                val characteristics = service.getCharacteristic(UUID.fromString("d15464da-de00-41d4-bec8-7c2b2cc8b2ee"))
                val wakeData: ByteArray = byteArrayOf(
                    0x01.toByte(), 0x02.toByte(), 0x04.toByte(), 0x0f.toByte(),
                    0x01.toByte(), 0x01.toByte(), 0x02.toByte(), 0x13.toByte(),
                    0x00.toByte(),
                )
                if (gatt.setCharacteristicNotification(characteristics, true))
                {
                    val writeOne = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                    {
                        gatt.writeCharacteristic(
                            characteristics,
                            wakeData,
                            BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
                        )
                    }
                    else
                    {
                        characteristics.setValue(wakeData)
                        gatt.writeCharacteristic(characteristics)
                    }
                    Log.v(TAG, "GATT WRITE[wakeData]: $writeOne [${wakeData.toUByteArray()}]")
                }
                pause()
/*
                val changeControlLine: ByteArray = byteArrayOf(
                    0x02.toByte(), 0x02.toByte(), 0x00.toByte(), 0x00.toByte(),
                    0x00.toByte(),
                )
                if (gatt.setCharacteristicNotification(characteristicsTwo, true))
                {
                    val write2nd =
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            gatt.writeCharacteristic(
                                characteristicsTwo,
                                changeControlLine,
                                BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
                            )
                        } else {
                            characteristicsTwo.setValue(changeControlLine)
                            gatt.writeCharacteristic(characteristicsTwo)
                        }
                    Log.v(TAG, "GATT WRITE[9]: $write2nd [${changeControlLine.toUByteArray()}]")
                }
                pause()
*/
                // 回線を切る
                gatt.disconnect()

                // 起動成功を通知
                callback.wakeupExecuted(true)
                return
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    override fun onCharacteristicWrite(
        gatt: BluetoothGatt?,
        characteristic: BluetoothGattCharacteristic?,
        status: Int
    ) {
        super.onCharacteristicWrite(gatt, characteristic, status)
        Log.v(TAG, "onCharacteristicWrite() : $status ${characteristic?.uuid}")
    }

    override fun onCharacteristicChanged(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        value: ByteArray
    ) {
        super.onCharacteristicChanged(gatt, characteristic, value)
        Log.v(TAG, " >> >> >> >> >> onCharacteristicChanged() $value ${characteristic.uuid}")
    }

    @SuppressLint("MissingPermission")
    override fun onConnectionStateChange(
        gatt: BluetoothGatt?,
        status: Int,
        newState: Int
    ) {
        super.onConnectionStateChange(gatt, status, newState)
        when (newState)
        {
            BluetoothProfile.STATE_CONNECTED -> {
                // ----- 接続した
                Log.v(TAG, "onConnectionStateChange(): STATE_CONNECTED")
                gatt?.discoverServices()
            }
            BluetoothProfile.STATE_DISCONNECTED -> {
                Log.v(TAG, "onConnectionStateChange(): STATE_DISCONNECTED")
            }
            else -> {
                Log.v(TAG, "onConnectionStateChange(): $status -> $newState")
            }
        }
    }

    private fun pause()
    {
        try
        {
            Log.v(TAG, " - - - - -  PAUSE  - - - - -")
            Thread.sleep(100)
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    companion object
    {
        private val TAG = OlympusAirBleCallback::class.java.simpleName
    }
}
