package jp.osdn.gokigen.aira01c.camera.omds.operation


import android.os.Build
import android.util.Log
import jp.osdn.gokigen.aira01c.camera.interfaces.IOmdsOperationCallback
import jp.osdn.gokigen.aira01c.camera.interfaces.IMessageDrawer
import jp.osdn.gokigen.aira01c.camera.utils.communication.SimpleHttpClient
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.Date
import java.util.HashMap
import java.util.Locale
import java.util.TimeZone

class OmdsTimeSync(private val messageDrawer: IMessageDrawer, userAgent: String = "OlympusCameraKit", private val executeUrl : String = "http://192.168.0.10")
{
    private val headerMap: MutableMap<String, String> = HashMap()
    private val http = SimpleHttpClient()

    fun setTimeSync(callback: IOmdsOperationCallback?)
    {
        try
        {
            val currentDateTime = Date(System.currentTimeMillis())
            val dateFormat1 = SimpleDateFormat("yyyyMMdd", Locale.US)
            val dateFormat2 = SimpleDateFormat("HHmmss", Locale.US)
            val timezone = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            {
                val dateFormat3 = SimpleDateFormat("XX", Locale.US)
                dateFormat3.format(currentDateTime)
            }
            else
            {
                val timeZone0 = TimeZone.getDefault()
                val offset = (timeZone0.getRawOffset().toFloat() / (60.0 * 60.0 * 1000.0))
                val hour = offset.toInt()
                val minute = 60.0 * (offset - hour)
                val plus = if (hour >= 0) { "+" } else { "-" }
                val hr = String.format(Locale.US, "%02d", Math.abs(hour))
                val mn = String.format(Locale.US, "%02d", minute.toInt())
                "${plus}${hr}${mn}"
            }
            val utcTime = "${dateFormat1.format(currentDateTime)}T${dateFormat2.format(currentDateTime)}"
            val setTimeString = "utctime=${utcTime}&utcdiff=${timezone}"
            Log.v(TAG, " setTimeSync : $setTimeString ($timezone)")
            messageDrawer.setMessageToShow("SET TIME : $utcTime $timezone")

            val thread = Thread {
                //  カメラの時刻を設定する
                val syncTimeUrl = "$executeUrl/set_utctimediff.cgi?$setTimeString"
                val response: String = http.httpGetWithHeader(syncTimeUrl, headerMap, null, TIMEOUT_MS) ?: ""
                Log.v(TAG, "RESP: (${response.length}) $response")
                callback?.operationResult(response)
                messageDrawer.appendMessageToShow("----- TIME SYNC DONE -----")
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
        headerMap["User-Agent"] = userAgent // "OlympusCameraKit" // "OI.Share"
        headerMap["X-Protocol"] = userAgent // "OlympusCameraKit" // "OI.Share"
    }

    companion object
    {
        private val TAG = OmdsTimeSync::class.java.simpleName
        private const val TIMEOUT_MS = 5000
    }
}
