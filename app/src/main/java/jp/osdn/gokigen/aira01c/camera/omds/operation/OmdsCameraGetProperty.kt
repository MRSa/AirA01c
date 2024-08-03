package jp.osdn.gokigen.aira01c.camera.omds.operation

import android.util.Log
import jp.osdn.gokigen.aira01c.camera.interfaces.IOmdsOperationCallback
import jp.osdn.gokigen.aira01c.camera.interfaces.IMessageDrawer
import jp.osdn.gokigen.aira01c.camera.utils.communication.SimpleHttpClient
import java.util.HashMap
import kotlin.Exception

class OmdsCameraGetProperty(private val messageDrawer: IMessageDrawer, userAgent: String = "OlympusCameraKit", private val executeUrl : String = "http://192.168.0.10")
{
    private val headerMap: MutableMap<String, String> = HashMap()
    private val http = SimpleHttpClient()

    fun getCameraProperty(propertyName: String, propertyLabel: String?, callback: IOmdsOperationCallback?)
    {
        try
        {
            val thread = Thread {
                //  ステータスを取得する
                val getStatusUrl = "$executeUrl/get_camprop.cgi?com=get&propname=$propertyName"
                val response: String = http.httpGetWithHeader(getStatusUrl, headerMap, null, TIMEOUT_MS) ?: ""
                Log.v(TAG, "RESP: (${response.length}) $response")
                callback?.operationResult(response)
                val value = pickupValue("value", response)
                messageDrawer.appendMessageToShow("$propertyLabel : $value")

                //val currentTime = Date(System.currentTimeMillis())
                //val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.US)
                //messageDrawer.appendMessageToShow("$propertyLabel : $value (${dateFormat.format(currentTime)})")
            }
            thread.start()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    private fun pickupValue(@Suppress("SameParameterValue") tagName: String, data: String): String
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
        private val TAG = OmdsTimeSync::class.java.simpleName
        private const val TIMEOUT_MS = 5000
    }
}
