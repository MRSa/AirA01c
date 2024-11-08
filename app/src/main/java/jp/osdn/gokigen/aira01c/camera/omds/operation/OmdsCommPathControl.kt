package jp.osdn.gokigen.aira01c.camera.omds.operation

import android.util.Log
import androidx.fragment.app.FragmentActivity
import jp.osdn.gokigen.aira01c.R
import jp.osdn.gokigen.aira01c.camera.interfaces.IMessageDrawer
import jp.osdn.gokigen.aira01c.camera.interfaces.IOmdsOperationCallback
import jp.osdn.gokigen.aira01c.camera.utils.communication.SimpleHttpClient
import java.lang.Exception
import java.util.HashMap

class OmdsCommPathControl(private val activity: FragmentActivity, private val messageDrawer: IMessageDrawer, userAgent: String = "OlympusCameraKit", private val executeUrl : String = "http://192.168.0.10")
{
    private val headerMap: MutableMap<String, String> = HashMap()
    private val http = SimpleHttpClient()

    fun changeCommPath(commPath: String = "wifi", callback: IOmdsOperationCallback? = null)
    {
        try
        {
            Log.v(TAG, " changeCommPath [$commPath]")
            val thread = Thread { // カメラとの接続確立を通知する
                val changeModeUrl = "$executeUrl/switch_commpath.cgi?path=$commPath"
                if (callback == null)
                {
                    messageDrawer.setMessageToShow("${activity.getString(R.string.change_path_to)} : $commPath")
                }
                val response: String = http.httpGetWithHeader(changeModeUrl, headerMap, null, TIMEOUT_MS) ?: ""
                Log.v(TAG, " $changeModeUrl $response")
                var message = ""
                var isChange = false
                try
                {
                    if (response.contains("200"))
                    {
                        isChange = true
                        message = "${activity.getString(R.string.change_path_done)} $commPath"
                    }
                    else
                    {
                        isChange = false
                        message = activity.getString(R.string.change_path_error)
                    }
                }
                catch (e: Exception)
                {
                    e.printStackTrace()
                }
                callback?.operationResult(isChange, message)
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

    init
    {
        headerMap["User-Agent"] = userAgent // "OlympusCameraKit" // "OI.Share"
        headerMap["X-Protocol"] = userAgent // "OlympusCameraKit" // "OI.Share"
    }

    companion object
    {
        private val TAG = OmdsCommPathControl::class.java.simpleName
        private const val TIMEOUT_MS = 5000
    }
}
