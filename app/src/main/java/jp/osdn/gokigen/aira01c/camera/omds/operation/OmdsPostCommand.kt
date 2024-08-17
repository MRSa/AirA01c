package jp.osdn.gokigen.aira01c.camera.omds.operation

import android.util.Log
import androidx.fragment.app.FragmentActivity
import jp.osdn.gokigen.aira01c.R
import jp.osdn.gokigen.aira01c.camera.interfaces.IMessageDrawer
import jp.osdn.gokigen.aira01c.camera.interfaces.IOmdsOperationCallback
import jp.osdn.gokigen.aira01c.camera.utils.communication.SimpleHttpClient
import java.lang.Exception
import java.util.HashMap

class OmdsPostCommand(private val activity: FragmentActivity, private val messageDrawer: IMessageDrawer, userAgent: String = "OlympusCameraKit", private val executeUrl : String = "http://192.168.0.10")
{
    private val headerMap: MutableMap<String, String> = HashMap()
    private val http = SimpleHttpClient()

    fun sendCommand(commandCgi: String, parameter: String, body: String, callback: IOmdsOperationCallback?)
    {
        try
        {
            Log.v(TAG, " OmdsCommands [$commandCgi] [$parameter] [$body]")
            val thread = Thread {
                val commandUrl = if (parameter.length > 1) {
                    "$executeUrl/$commandCgi?$parameter"
                } else {
                    "$executeUrl/$commandCgi"
                }
                if (callback == null)
                {
                    messageDrawer.setMessageToShow("${activity.getString(R.string.lbl_send_command)} : $commandCgi")
                }
                val response: String = http.httpPostWithHeader(commandUrl, body, headerMap, null, TIMEOUT_MS) ?: ""
                Log.v(TAG, " $commandUrl $response")
                var isChange = false
                try
                {
                    isChange = (response.contains("OK"))||(response.contains("ok"))
                }
                catch (e: Exception)
                {
                    e.printStackTrace()
                }
                callback?.operationResult(isChange, response)
            }
            thread.start()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    init
    {
        headerMap["User-Agent"] = userAgent // "OlympusCameraKit" or "OI.Share"
        headerMap["X-Protocol"] = userAgent // "OlympusCameraKit" or "OI.Share"
    }

    companion object
    {
        private val TAG = OmdsPostCommand::class.java.simpleName
        private const val TIMEOUT_MS = 5000
    }
}
