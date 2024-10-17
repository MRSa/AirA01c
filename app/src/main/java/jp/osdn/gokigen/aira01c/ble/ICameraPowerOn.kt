package jp.osdn.gokigen.aira01c.ble

interface ICameraPowerOn
{
    // カメラ起動指示
    fun wakeup(target: MyBleDevice, code: String, callback: IPowerOnCameraCallback?)

    // 実行終了時のコールバックのインタフェース
    interface IPowerOnCameraCallback
    {
        fun onStart(message: String)
        fun onProgress(message: String)
        fun wakeupExecuted(isExecute: Boolean)
    }
}
