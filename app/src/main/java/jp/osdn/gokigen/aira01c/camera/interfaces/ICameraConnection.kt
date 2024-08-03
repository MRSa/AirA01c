package jp.osdn.gokigen.aira01c.camera.interfaces

interface ICameraConnection
{
    fun alertConnectingFailed(message: String?)
    fun forceUpdateConnectionStatus(status: ICameraConnectionStatus.CameraConnectionStatus)
}
