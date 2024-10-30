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
import androidx.fragment.app.FragmentActivity
import jp.osdn.gokigen.aira01c.R
import jp.osdn.gokigen.aira01c.ble.ICameraPowerOn.IPowerOnCameraCallback
import java.util.UUID

@RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
class OlympusAirBleCallback(private val context: FragmentActivity, private val code: String, private val callback: IPowerOnCameraCallback): BluetoothGattCallback()
{
    private var bleStatus : BleConnectionStatus = BleConnectionStatus.UNKNOWN
    private var wakeupStatus = false

    override fun onDescriptorWrite(gatt: BluetoothGatt?, descriptor: BluetoothGattDescriptor?, status: Int)
    {
        super.onDescriptorWrite(gatt, descriptor, status)
        Log.v(TAG, "onDescriptorWrite() : $status ${descriptor?.uuid}")
        bleConnectionProceed(gatt)
    }

    override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int)
    {
        super.onServicesDiscovered(gatt, status)
        try
        {
            wakeupStatus = false
            if (status == BluetoothGatt.GATT_SUCCESS)
            {
                // ----- 接続成功
                Log.v(TAG, "***---*** GATT CONNECT SUCCESS. ***---*** ($bleStatus)")
                if (code.isEmpty())
                {
                    // ----- パスコードが入力されていない場合は、すぐに接続コマンドを送る
                    bleStatus = BleConnectionStatus.CONNECT
                }
                bleConnectionProceed(gatt)
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
        Log.v(TAG, "  onCharacteristicWrite() : $status ${characteristic?.uuid} $bleStatus")
        if (bleStatus == BleConnectionStatus.WAKEUP)
        {
            bleStatus = BleConnectionStatus.FINISH
            if (status == BluetoothGatt.GATT_SUCCESS)
            {
                wakeupStatus = true
            }
        }
        bleConnectionProceed(gatt)
    }

    override fun onCharacteristicChanged(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        value: ByteArray
    ) {
        super.onCharacteristicChanged(gatt, characteristic, value)
        Log.v(TAG, "  onCharacteristicChanged() $value ${characteristic.uuid}")
        bleConnectionProceed(gatt)
    }

    @SuppressLint("MissingPermission")
    override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int)
    {
        super.onConnectionStateChange(gatt, status, newState)
        when (newState)
        {
            BluetoothProfile.STATE_CONNECTED -> {
                // ----- 接続した
                Log.v(TAG, "onConnectionStateChange(): STATE_CONNECTED")
                callback.onProgress(context.getString(R.string.ble_connect_connected), false)
                bleStatus = BleConnectionStatus.START
                gatt?.discoverServices()
            }
            BluetoothProfile.STATE_DISCONNECTED -> {
                Log.v(TAG, "onConnectionStateChange(): STATE_DISCONNECTED")
                // ---- 切断した
                callback.onProgress(context.getString(R.string.ble_connect_disconnected), false)
                bleStatus = BleConnectionStatus.UNKNOWN
            }
            else -> {
                Log.v(TAG, "onConnectionStateChange(): $status -> $newState")
            }
        }
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    @SuppressLint("MissingPermission")
    private fun bleConnectionProceed(gatt: BluetoothGatt?)
    {
        try
        {
            Log.v(TAG, "BLE CONNECT SEQUENCE : $bleStatus")
            when (bleStatus)
            {
                BleConnectionStatus.CONNECT -> {
                    // ---- Wake up the Olympus Air -----
                    val service =
                        gatt?.getService(UUID.fromString("0391D26E-625B-4736-B4DA-3BB0910ECEC5"))
                            ?: return
                    val characteristics =
                        service.getCharacteristic(UUID.fromString("d15464da-de00-41d4-bec8-7c2b2cc8b2ee"))
                    val wakeData: ByteArray = byteArrayOf(
                        0x01.toByte(), 0x02.toByte(), 0x04.toByte(), 0x0f.toByte(),
                        0x01.toByte(), 0x01.toByte(), 0x02.toByte(), 0x13.toByte(),
                        0x00.toByte(),
                    )
                    if (gatt.setCharacteristicNotification(characteristics, true)) {
                        val writeOne = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            gatt.writeCharacteristic(
                                characteristics,
                                wakeData,
                                BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
                            )
                        } else {
                            @Suppress("DEPRECATION")
                            characteristics.setValue(wakeData)
                            @Suppress("DEPRECATION")
                            gatt.writeCharacteristic(characteristics)
                        }
                        Log.v(TAG, "GATT WRITE[wakeData]: $writeOne [${wakeData.toUByteArray()}]")
                        bleStatus = BleConnectionStatus.WAKEUP
                    }
                }

                BleConnectionStatus.START -> {
                    // ----- コードを送信するシーケンス (文字数不足予防のためにダミーの数字を入れておく）
                    val codeString = code + "000000"
                    val codeByte = codeString.toByteArray()
                    val passCodeInput: ByteArray = byteArrayOf(
                        0x01.toByte(), 0x01.toByte(), 0x09.toByte(), 0x0c.toByte(),
                        0x01.toByte(), 0x02.toByte(),
                        codeByte[0], codeByte[1], codeByte[2],
                        codeByte[3], codeByte[4], codeByte[5],
                        0x00.toByte(),
                    )
                    val service =
                        gatt?.getService(UUID.fromString("0391D26E-625B-4736-B4DA-3BB0910ECEC5"))
                            ?: return
                    val characteristics =
                        service.getCharacteristic(UUID.fromString("d15464da-de00-41d4-bec8-7c2b2cc8b2ee"))
                    if (gatt.setCharacteristicNotification(characteristics, true)) {
                        val writeOne = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            gatt.writeCharacteristic(
                                characteristics,
                                passCodeInput,
                                BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
                            )
                        } else {
                            @Suppress("DEPRECATION")
                            characteristics.setValue(passCodeInput)
                            @Suppress("DEPRECATION")
                            gatt.writeCharacteristic(characteristics)
                        }
                        Log.v(TAG, "GATT WRITE[wakeData]: $writeOne [${passCodeInput.toUByteArray()}]")
                        bleStatus = BleConnectionStatus.CONNECT
                    }
                }
                BleConnectionStatus.FINISH -> {
                    // 回線を切る
                    gatt?.disconnect()

                    // 起動結果を通知
                    callback.wakeupExecuted(wakeupStatus)
                }
                else -> {
                    Log.v(TAG, "Called Unknown timing : $bleStatus")
                    bleStatus = BleConnectionStatus.FINISH
                }
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
            //// ----- エラー発生時にはエラーとして終了する
            //bleStatus = BleConnectionStatus.FINISH
            //bleConnectionProceed(gatt)
        }
    }

    private enum class BleConnectionStatus { UNKNOWN, START, CONNECT, WAKEUP, FINISH, }

    companion object
    {
        private val TAG = OlympusAirBleCallback::class.java.simpleName
    }
}
