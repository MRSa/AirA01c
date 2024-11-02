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
    // ----- https://stackoverflow.com/questions/39272712 の情報より実装
    private var bleStatus : BleConnectionStatus = BleConnectionStatus.UNKNOWN
    private var wakeupStatus = false
    private var writeResultReceived = false

    override fun onDescriptorWrite(gatt: BluetoothGatt?, descriptor: BluetoothGattDescriptor?, status: Int)
    {
        super.onDescriptorWrite(gatt, descriptor, status)
        Log.v(TAG, "onDescriptorWrite() : $status ${descriptor?.uuid} [$bleStatus]")
        if (bleStatus == BleConnectionStatus.PREPARE)
        {
            setupNotification2nd(gatt)
            bleStatus = BleConnectionStatus.PASSCODE
            return
        }
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
                if (code.isNotEmpty())
                {
                    // ----- パスコードが入力されていた場合は、パスコードを送信する
                    callback.onProgress(" ${context.getString(R.string.ble_send_passcode)}", false)
                    bleStatus = BleConnectionStatus.PREPARE
                }
                else
                {
                    // パスコードがない場合は、WAKEUPコマンドを発行する
                    bleStatus = BleConnectionStatus.WAKEUP
                }
                // ----- データ更新の通知をする設定
                setupNotification(gatt)

                // ----- データダンプ用ロジック
                dumpServices(gatt)
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    @SuppressLint("MissingPermission")
    private fun setupNotification(gatt: BluetoothGatt?)
    {
        try
        {
            val service = gatt?.getService(UUID.fromString("0391D26E-625B-4736-B4DA-3BB0910ECEC5")) ?: return
            val characteristics = service.getCharacteristic(UUID.fromString("d15464da-de00-41d4-bec8-7c2b2cc8b2ee"))
            if (gatt.setCharacteristicNotification(characteristics, true))
            {
                val descriptor = characteristics.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"))
                val writeDesc = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                {
                    gatt.writeDescriptor(descriptor, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
                }
                else
                {
                    @Suppress("DEPRECATION")
                    descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
                    @Suppress("DEPRECATION")
                    gatt.writeDescriptor(descriptor)
                }
                Log.v(TAG, "GATT WRITE[enableNotification]: $writeDesc [${BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE.toUByteArray()}]")
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    @SuppressLint("MissingPermission")
    private fun setupNotification2nd(gatt: BluetoothGatt?)
    {
        try
        {
            val service = gatt?.getService(UUID.fromString("0391D26E-625B-4736-B4DA-3BB0910ECEC5")) ?: return
            val characteristics2 = service.getCharacteristic(UUID.fromString("59168c27-b5cd-40c7-9ee0-f5ec2e927346"))
            if (gatt.setCharacteristicNotification(characteristics2, true))
            {
                val descriptor2 = characteristics2.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"))
                val writeDesc = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                {
                    gatt.writeDescriptor(descriptor2, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
                }
                else
                {
                    @Suppress("DEPRECATION")
                    descriptor2.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
                    @Suppress("DEPRECATION")
                    gatt.writeDescriptor(descriptor2)
                }
                Log.v(TAG, "GATT WRITE[enableNotification2]: $writeDesc [${BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE.toUByteArray()}]")
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    private fun dumpServices(gatt: BluetoothGatt?)
    {
        try
        {
            // ::::: Services / Characteristics / Descriptors のダンプ (フラグがONの時、ログに出力する)
            if (DEBUG_SERVICES)
            {
                val services = gatt?.services ?: return
                Log.v(TAG, "------------")
                for (serviceItem in services)
                {
                    Log.v(TAG, "SERVICE : ${serviceItem.uuid}")
                    for (characteristicItem in serviceItem.characteristics)
                    {
                        Log.v(TAG, " CHARACTERISTICS : ${characteristicItem.uuid}")
                        for (descriptorItem in characteristicItem.descriptors)
                        {
                            Log.v(TAG, "  DESCRIPTOR : ${descriptorItem.uuid}")
                        }
                    }
                }
                Log.v(TAG, "------------")
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    override fun onCharacteristicWrite(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int)
    {
        super.onCharacteristicWrite(gatt, characteristic, status)
        val statusChar = if (status == BluetoothGatt.GATT_SUCCESS) { "SUCCESS" } else { "ERROR" }
        writeResultReceived = false
        Log.v(TAG, "  onCharacteristicWrite() : $statusChar($status) ${characteristic?.uuid} $bleStatus")
        when (bleStatus)
        {
            BleConnectionStatus.PASSCODE -> {
                bleStatus = BleConnectionStatus.WAKEUP
            }
            BleConnectionStatus.WAKEUP -> {
                bleStatus = BleConnectionStatus.FINISH
                if (status == BluetoothGatt.GATT_SUCCESS)
                {
                    wakeupStatus = true
                }
            }
            else -> {
                Log.v(TAG, "  --- onCharacteristicWrite() --- NOT SUPPORT STATUS : $bleStatus")
            }
        }
        // bleConnectionProceed(gatt)  // 書き込み... 値が変わるのを onCharacteristicChanged で待つべき
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    override fun onCharacteristicChanged(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        value: ByteArray
    ) {
        super.onCharacteristicChanged(gatt, characteristic, value)
        Log.v(TAG, "  onCharacteristicChanged() ${value.toUByteArray()} ${characteristic.uuid} +++$bleStatus+++")
        writeResultReceived = true
        bleConnectionProceed(gatt)
    }

    @Deprecated("Deprecated in Java")
    @OptIn(ExperimentalUnsignedTypes::class)
    override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?)
    {
        @Suppress("DEPRECATION")
        super.onCharacteristicChanged(gatt, characteristic)
        @Suppress("DEPRECATION")
        Log.v(TAG, "  onCharacteristicChanged() ${characteristic?.value?.toUByteArray()} ${characteristic?.uuid} ***$bleStatus***")
        writeResultReceived = true
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

    private fun bleConnectionProceed(gatt: BluetoothGatt?)
    {
        try
        {
            // ---- 別スレッドで送信する...
            Thread { bleConnectionProceedImpl(gatt) }.start()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    @SuppressLint("MissingPermission")
    private fun bleConnectionProceedImpl(gatt: BluetoothGatt?)
    {
        try
        {
            Log.v(TAG, "BLE CONNECT SEQUENCE : $bleStatus")
            wait250ms()  //  送信前にちょっと止める
            when (bleStatus)
            {
                BleConnectionStatus.PASSCODE -> {
                    // ----- コードを送信するシーケンス (文字数不足予防のためにダミーの数字を入れておく）
                    val codeString = code + "000000"
                    val codeByte = codeString.toByteArray()
                    var checkSum: Byte = 15 // (0x0c + 0x01 + 0x02)
                    for (pi in 0..5)
                    {
                        checkSum = (checkSum + codeByte[pi]).toByte()
                    }
                    Log.v(TAG, " ---- DATA CHECK SUM: $checkSum")
                    val passCodeInput: ByteArray = byteArrayOf(
                        0x01.toByte(), 0x01.toByte(),
                        0x09.toByte(),
                        0x0c.toByte(), 0x01.toByte(), 0x02.toByte(),  // 送信コマンド？
                        codeByte[0], codeByte[1], codeByte[2],        // パスコード
                        codeByte[3], codeByte[4], codeByte[5],        // パスコード
                        checkSum, 0x00.toByte(),
                    )
                    val service =
                        gatt?.getService(UUID.fromString("0391D26E-625B-4736-B4DA-3BB0910ECEC5")) ?: return
                    val characteristics = service.getCharacteristic(UUID.fromString("d15464da-de00-41d4-bec8-7c2b2cc8b2ee"))
                    val writeCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
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
                    Log.v(TAG, "GATT WRITE[passCode]: $writeCode [${passCodeInput.toUByteArray()}]")
                    writeResultReceived = false
                }

                BleConnectionStatus.WAKEUP -> {
                    // ---- Wake up the Olympus Air -----
                    val service = gatt?.getService(UUID.fromString("0391D26E-625B-4736-B4DA-3BB0910ECEC5")) ?: return
                    val characteristics = service.getCharacteristic(UUID.fromString("d15464da-de00-41d4-bec8-7c2b2cc8b2ee"))
                    val wakeData: ByteArray = byteArrayOf(
                        0x01.toByte(), 0x01.toByte(),
                        0x04.toByte(),
                        0x0f.toByte(), 0x01.toByte(), 0x01.toByte(), 0x02.toByte(),
                        0x13.toByte(), 0x00.toByte(),
                    )
                    val writeValue = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
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
                    Log.v(TAG, "GATT WRITE[wakeData2]: ($writeValue) [${wakeData.toUByteArray()}]")
                    writeResultReceived = false
                }

                BleConnectionStatus.FINISH -> {
                    // 回線を切る
                    gatt?.disconnect()

                    // 起動結果を通知
                    callback.wakeupExecuted((wakeupStatus&&writeResultReceived))
                    writeResultReceived = false
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

    private fun wait250ms()
    {
        try
        {
            Thread.sleep(WAIT_250MS)
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    private enum class BleConnectionStatus { UNKNOWN, PREPARE, PASSCODE, WAKEUP, FINISH, }

    companion object
    {
        private val TAG = OlympusAirBleCallback::class.java.simpleName
        private const val WAIT_250MS = 250L
        private const val DEBUG_SERVICES = false
    }
}
