package jp.osdn.gokigen.aira01c.camera.interfaces

/**
 *
 *
 */
interface ICameraStatusUpdateNotify
{
    fun updatedTakeMode(mode: String?)
    fun updatedShutterSpeed(tv: String?)
    fun updatedAperture(av: String?)
    fun updatedExposureCompensation(xv: String?)
    fun updatedMeteringMode(meteringMode: String?)
    fun updatedWBMode(wbMode: String?)
    fun updateRemainBattery(percentage: Int)
    fun updateFocusedStatus(focused: Boolean, focusLocked: Boolean)
    fun updateIsoSensitivity(sv: String?)
    fun updateWarning(warning: String?)
    fun updateStorageStatus(status: String?)
    fun updateShootMode(shootMode: String?)
}
