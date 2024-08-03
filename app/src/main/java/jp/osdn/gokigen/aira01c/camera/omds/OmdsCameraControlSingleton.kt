package jp.osdn.gokigen.aira01c.camera.omds

import android.util.Log
import androidx.fragment.app.FragmentActivity
import jp.osdn.gokigen.aira01c.R
import jp.osdn.gokigen.aira01c.camera.interfaces.ICameraConnectionStatus
import jp.osdn.gokigen.aira01c.camera.interfaces.ICameraStatusReceiver
import jp.osdn.gokigen.aira01c.camera.interfaces.IMessageDrawer
import jp.osdn.gokigen.aira01c.camera.omds.connection.OmdsCameraConnection
import jp.osdn.gokigen.aira01c.camera.omds.operation.OmdsCameraGetProperty
import jp.osdn.gokigen.aira01c.camera.omds.operation.OmdsCameraStatus
import jp.osdn.gokigen.aira01c.camera.omds.operation.OmdsRunModeControl
import jp.osdn.gokigen.aira01c.camera.omds.operation.OmdsTimeSync
import jp.osdn.gokigen.aira01c.camera.omds.status.OmdsCameraStatusWatcher

class OmdsCameraControlSingleton : IOmdsProtocolNotify, ICameraStatusReceiver, ICameraConnectionStatus
{
    private val statusChecker = OmdsCameraStatusWatcher()
    private var connectionStatus: ICameraConnectionStatus.CameraConnectionStatus = ICameraConnectionStatus.CameraConnectionStatus.UNKNOWN

    private lateinit var activity: FragmentActivity
    private lateinit var cameraConnection: OmdsCameraConnection
    private lateinit var messageDrawer : IMessageDrawer
    private lateinit var runModeControl : OmdsRunModeControl
    private lateinit var timeSync: OmdsTimeSync
    private lateinit var cameraStatus: OmdsCameraStatus
    private lateinit var getCameraProperty: OmdsCameraGetProperty

    private var isInitialized  = false

    fun initialize(activity: FragmentActivity, messageDrawer: IMessageDrawer)
    {
        try
        {
            this.activity = activity
            this.messageDrawer = messageDrawer
            this.cameraConnection = OmdsCameraConnection(activity, statusChecker, this, this)
            this.runModeControl = OmdsRunModeControl(messageDrawer)
            this.timeSync = OmdsTimeSync(messageDrawer)
            this.cameraStatus = OmdsCameraStatus(activity, messageDrawer)
            this.getCameraProperty = OmdsCameraGetProperty(messageDrawer)

            isInitialized = true
        }
        catch (e: Exception)
        {
            e.printStackTrace()
            isInitialized = false
        }
    }

/*
    fun connectToCamera()
    {
        Log.v(TAG, " connectToCamera() : OMDS ")
        try
        {
            cameraConnection.connect()
        }
        catch (e : Exception)
        {
            e.printStackTrace()
        }
    }
 */
    fun startCamera()
    {
        try
        {
            Log.v(TAG, " startCamera() : OMDS ")
            if (connectionStatus != ICameraConnectionStatus.CameraConnectionStatus.CONNECTED)
            {
                cameraConnection.startWatchWifiStatus(activity)
            }
            else
            {
                cameraConnection.connect()
            }
        }
        catch (e : Exception)
        {
            e.printStackTrace()
        }
    }

    fun finishCamera(isPowerOff: Boolean)
    {
        try
        {
            Log.v(TAG, " finishCamera() : $isPowerOff ")
            statusChecker.stopStatusWatch()
            cameraConnection.disconnect(isPowerOff)
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    fun changeRunMode(runMode: String) { runModeControl.changeRunMode(runMode, null) }
    fun synchronizeTime() { timeSync.setTimeSync(null) }
    fun getCameraStatus()
    {
        cameraStatus.getCameraStatus(null)
        getCameraProperty.getCameraProperty("BATTERY_LEVEL", activity.getString(R.string.label_battery_level),null)
    }

    override fun detectedOpcProtocol(opcProtocol: Boolean)
    {
        statusChecker.detectedOpcProtocol(opcProtocol)
    }

    override fun getConnectionStatus(): ICameraConnectionStatus.CameraConnectionStatus
    {
        Log.v(TAG, "getConnectionStatus()")
        return (connectionStatus)
    }

    override fun onStatusNotify(message: String?)
    {
        if (message == null)
        {
            messageDrawer.clear()
        }
        else
        {
            messageDrawer.appendMessageToShow("$message")
        }
    }

    override fun onCameraConnected()
    {
        // ICameraStatusReceiver の実装
        try
        {
            connectionStatus = ICameraConnectionStatus.CameraConnectionStatus.CONNECTED
            messageDrawer.invalidate()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    override fun onCameraDisconnected()
    {
        // ICameraStatusReceiver の実装
        try
        {
            connectionStatus = ICameraConnectionStatus.CameraConnectionStatus.DISCONNECTED
            messageDrawer.invalidate()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    override fun onCameraConnectError(msg: String?) {
        // ICameraStatusReceiver の実装
        try
        {
            connectionStatus = ICameraConnectionStatus.CameraConnectionStatus.UNKNOWN
            messageDrawer.invalidate()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    companion object
    {
        private val TAG = OmdsCameraControlSingleton::class.java.simpleName
    }
}