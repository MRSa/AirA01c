package jp.osdn.gokigen.aira01c.camera.interfaces

interface ICameraStatusReceiver
{
    fun onStatusNotify(message: String?)
    fun onCameraConnected()
    fun onCameraDisconnected()
    fun onCameraConnectError(msg: String?)
}
