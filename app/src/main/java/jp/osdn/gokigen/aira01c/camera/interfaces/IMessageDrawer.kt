package jp.osdn.gokigen.aira01c.camera.interfaces

interface IMessageDrawer
{
    fun setMessageToShow(message: String)
    fun appendMessageToShow(message: String)
    fun clear()
    fun invalidate()
}
