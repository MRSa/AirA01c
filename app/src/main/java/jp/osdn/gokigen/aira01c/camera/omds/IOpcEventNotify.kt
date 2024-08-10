package jp.osdn.gokigen.aira01c.camera.omds

interface IOpcEventNotify
{
    fun getSubscribeId(): String
    fun receivedOpcEvent(eventMessage: String)
}