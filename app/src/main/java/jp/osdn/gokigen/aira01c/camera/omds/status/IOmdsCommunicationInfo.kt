package jp.osdn.gokigen.aira01c.camera.omds.status

interface IOmdsCommunicationInfo
{
    fun setOmdsCommandList(commandList: String)
    fun startReceiveOpcEvent()
}
