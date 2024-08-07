package jp.osdn.gokigen.aira01c.ui.configuration

import android.util.Log
import jp.osdn.gokigen.aira01c.camera.interfaces.ICameraMaintenanceCommandSequence

class CameraMaintenanceDummy: ICameraMaintenanceCommandSequence
{
    override fun getCommandTitle(): String {
        return ("DUMMY COMMAND")
    }

    override fun executeMaintenanceCommand(parameter: String?)
    {
        Log.v(TAG, "EXECUTE COMMAND : $parameter")
    }

    override fun abortMaintenanceCommand(parameter: String?) {
        Log.v(TAG, "ABORT COMMAND : $parameter")
    }

    override fun isVisiblePrevious(): Boolean {
        return (false)
    }

    override fun isEnabledPrevious(): Boolean {
        return (false)
    }

    override fun isVisibleNext(): Boolean {
        return (false)
    }

    override fun isEnableNext(): Boolean {
        return (false)
    }

    override fun isEnableClose(): Boolean {
        return (false)
    }

    override fun pressedPrevious() {
        Log.v(TAG, " PRESSED PREVIOUS")
    }

    override fun pressedNext() {
        Log.v(TAG, " PRESSED PREVIOUS")
    }

    override fun reset() {
        Log.v(TAG, "  ----- RESET -----")
    }


    companion object
    {
        private val TAG = ICameraMaintenanceCommandSequence::class.java.simpleName
    }
}