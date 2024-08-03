package jp.osdn.gokigen.aira01c.camera.interfaces

interface ICameraStatusWatcher
{
    fun startStatusWatch(indicator : IMessageDrawer?, notifier: ICameraStatusUpdateNotify?)
    fun stopStatusWatch()
}
