package jp.osdn.gokigen.aira01c.camera.interfaces

interface ICameraConnectionStatus
{
    enum class CameraConnectionStatus
    {
        UNKNOWN,  DISCONNECTED, CONNECTING, CONNECTED
    }

    fun getConnectionStatus(): CameraConnectionStatus
}
