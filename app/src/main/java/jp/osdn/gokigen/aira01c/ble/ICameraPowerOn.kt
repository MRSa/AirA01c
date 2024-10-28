package jp.osdn.gokigen.aira01c.ble

import android.bluetooth.BluetoothDevice

interface ICameraPowerOn
{
    // カメラ起動指示
    fun wakeup(target: MyBleDevice, code: String, callback: IPowerOnCameraCallback)
    fun cancelWakeup()

    // 実行終了時のコールバックのインタフェース
    interface IPowerOnCameraCallback
    {
        fun onStart(message: String)
        fun onProgress(message: String, isLineFeed: Boolean = true)
        fun wakeupExecuted(isExecute: Boolean)
        fun finishedScan(deviceList: Map<String, BluetoothDevice>)
    }
}
