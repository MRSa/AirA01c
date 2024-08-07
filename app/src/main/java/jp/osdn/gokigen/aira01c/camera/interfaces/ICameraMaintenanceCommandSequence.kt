package jp.osdn.gokigen.aira01c.camera.interfaces

interface ICameraMaintenanceCommandSequence
{
    fun getCommandTitle(): String
    fun executeMaintenanceCommand(parameter: String?)
    fun abortMaintenanceCommand(parameter: String?)

    fun isVisiblePrevious(): Boolean
    fun isEnabledPrevious(): Boolean

    fun isVisibleNext() : Boolean
    fun isEnableNext() : Boolean

    fun isEnableClose(): Boolean

    fun pressedPrevious()
    fun pressedNext()

    fun reset()
}