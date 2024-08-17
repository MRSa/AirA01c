package jp.osdn.gokigen.aira01c.camera.omds

import android.util.Log
import androidx.fragment.app.FragmentActivity
import jp.osdn.gokigen.aira01c.R
import jp.osdn.gokigen.aira01c.camera.interfaces.ICameraConnectionStatus
import jp.osdn.gokigen.aira01c.camera.interfaces.ICameraStatusReceiver
import jp.osdn.gokigen.aira01c.camera.interfaces.IMessageDrawer
import jp.osdn.gokigen.aira01c.camera.interfaces.IOmdsOperationCallback
import jp.osdn.gokigen.aira01c.camera.omds.connection.OmdsCameraConnection
import jp.osdn.gokigen.aira01c.camera.omds.operation.OmdsCamIndStatus
import jp.osdn.gokigen.aira01c.camera.omds.operation.OmdsCameraGetProperty
import jp.osdn.gokigen.aira01c.camera.omds.operation.OmdsCameraStatus
import jp.osdn.gokigen.aira01c.camera.omds.operation.OmdsGetCommand
import jp.osdn.gokigen.aira01c.camera.omds.operation.OmdsPostCommand
import jp.osdn.gokigen.aira01c.camera.omds.operation.OmdsRunModeControl
import jp.osdn.gokigen.aira01c.camera.omds.operation.OmdsTimeSync
import jp.osdn.gokigen.aira01c.camera.omds.status.OmdsCameraStatusWatcher

class OmdsCameraControlSingleton : IOmdsProtocolNotify, ICameraStatusReceiver, ICameraConnectionStatus, OmdsCameraStatusWatcher.IOpcEventReceive
{
    private val statusChecker = OmdsCameraStatusWatcher(this)
    private var connectionStatus: ICameraConnectionStatus.CameraConnectionStatus = ICameraConnectionStatus.CameraConnectionStatus.UNKNOWN
    private val subscriberList = ArrayList<IOpcEventNotify>()

    private lateinit var activity: FragmentActivity
    private lateinit var cameraConnection: OmdsCameraConnection
    private lateinit var messageDrawer : IMessageDrawer
    private lateinit var runModeControl : OmdsRunModeControl
    private lateinit var timeSync: OmdsTimeSync
    private lateinit var cameraStatus: OmdsCameraStatus
    private lateinit var getCameraProperty: OmdsCameraGetProperty
    private lateinit var getCommand: OmdsGetCommand
    private lateinit var postCommand: OmdsPostCommand
    private lateinit var camInState: OmdsCamIndStatus

    private var isInitialized  = false

    fun initialize(activity: FragmentActivity, messageDrawer: IMessageDrawer)
    {
        try
        {
            this.activity = activity
            this.messageDrawer = messageDrawer
            this.cameraConnection = OmdsCameraConnection(activity, statusChecker, this, this)
            this.runModeControl = OmdsRunModeControl(activity, messageDrawer)
            this.timeSync = OmdsTimeSync(activity, messageDrawer)
            this.cameraStatus = OmdsCameraStatus(activity, messageDrawer)
            this.getCameraProperty = OmdsCameraGetProperty(activity, messageDrawer)
            this.getCommand = OmdsGetCommand(activity, messageDrawer)
            this.postCommand = OmdsPostCommand(activity, messageDrawer)
            this.camInState = OmdsCamIndStatus(activity, messageDrawer)
            this.subscriberList.clear()

            isInitialized = true
        }
        catch (e: Exception)
        {
            e.printStackTrace()
            isInitialized = false
        }
    }

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

/*
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
*/

    fun finishCamera(isPowerOff: Boolean)
    {
        try
        {
            Log.v(TAG, " finishCamera() : $isPowerOff ")
            val thread = Thread {
                try
                {
                    statusChecker.stopStatusWatch()
                    cameraConnection.disconnect(isPowerOff)
                }
                catch (e: Exception)
                {
                    e.printStackTrace()
                }
            }
            thread.start()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    fun sendGetCommand(command: String, parameter: String, callback: IOmdsOperationCallback? = null) { getCommand.sendCommand(command, parameter, callback) }
    fun sendPostCommand(command: String, parameter: String, body: String, callback: IOmdsOperationCallback? = null) { postCommand.sendCommand(command, parameter, body, callback) }
    fun changeRunMode(runMode: String, callback: IOmdsOperationCallback? = null) { runModeControl.changeRunMode(runMode, callback) }
    fun synchronizeTime() { timeSync.setTimeSync(null) }
    fun getCameraStatus()
    {
        cameraStatus.getCameraStatus(null)
        sendWait100ms()
        getCameraProperty.getCameraProperty("BATTERY_LEVEL", activity.getString(R.string.label_battery_level),null)
        sendWait100ms()
        camInState.getCamInState(null)
    }

    private fun sendWait100ms()
    {
        try
        {
            Thread.sleep(100)
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
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

    fun subscribeOpcEvent(subscriber: IOpcEventNotify)
    {
        try
        {
            Log.v(TAG, "subscribeOpcEvent() : ${subscriber.getSubscribeId()}")
            subscriberList.add(subscriber)
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    fun unsubscribeOpcEvent(subscriber: IOpcEventNotify)
    {
        try
        {
            Log.v(TAG, "unsubscribeOpcEvent() : ${subscriber.getSubscribeId()}")
            subscriberList.remove(subscriber)
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    override fun receivedOpcEvent(value: String)
    {
        try
        {
            Log.v(TAG, "receivedOpcEvent() [subscriber: ${subscriberList.size}]")
            subscriberList.forEach { subscriber ->
                try
                {
                    subscriber.receivedOpcEvent(value)
                }
                catch (e: Exception)
                {
                    e.printStackTrace()
                }
            }
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
