package jp.osdn.gokigen.aira01c.camera.omds.operation

import android.util.Log
import androidx.fragment.app.FragmentActivity
import jp.osdn.gokigen.aira01c.R
import jp.osdn.gokigen.aira01c.camera.interfaces.IOmdsOperationCallback
import jp.osdn.gokigen.aira01c.camera.interfaces.IMessageDrawer
import jp.osdn.gokigen.aira01c.camera.utils.communication.SimpleHttpClient
import java.lang.Exception
import java.util.HashMap

class OmdsCamIndStatus(private val activity: FragmentActivity, private val messageDrawer: IMessageDrawer, userAgent: String = "OlympusCameraKit", private val executeUrl : String = "http://192.168.0.10")
{
    private val headerMap: MutableMap<String, String> = HashMap()
    private val http = SimpleHttpClient()

    fun getCamInState(callback: IOmdsOperationCallback?)
    {
        try
        {
            Log.v(TAG, " getCamInState")
            val thread = Thread {
                val camInStateUrl = "$executeUrl/get_camindstate.cgi"
                val response: String = http.httpGetWithHeader(camInStateUrl, headerMap, null, TIMEOUT_MS) ?: ""
                Log.v(TAG, " $camInStateUrl $response")
                val message = "${activity.getString(R.string.camera_firmware_version)} ${pickupValue("fwversion", response)}\n${activity.getString(R.string.lens_firmware_version)} ${pickupValue("lensfwversion", response)}\n"
                callback?.operationResult(true, message)
                if (callback == null)
                {
                    messageDrawer.appendMessageToShow(message)
                }
                else
                {
                    Log.v(TAG, message)
                }
            }
            thread.start()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    private fun pickupValue(tagName: String, data: String): String
    {
        var value = ""
        try
        {
            val startTag = "<$tagName>"
            val endTag = "</$tagName>"

            val startPosition = data.indexOf(startTag) + startTag.length
            val endPosition = data.indexOf(endTag)
            if ((startPosition >= 0)&&(endPosition > 0))
            {
                value = data.substring(startPosition, endPosition)
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
        return (value)
    }

    init
    {
        headerMap["User-Agent"] = userAgent // "OlympusCameraKit" // "OI.Share"
        headerMap["X-Protocol"] = userAgent // "OlympusCameraKit" // "OI.Share"
    }

    companion object
    {
        private val TAG = OmdsCamIndStatus::class.java.simpleName
        private const val TIMEOUT_MS = 5000
    }
}
