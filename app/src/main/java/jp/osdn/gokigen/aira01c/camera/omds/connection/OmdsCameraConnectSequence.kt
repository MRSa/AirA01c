package jp.osdn.gokigen.aira01c.camera.omds.connection

import android.util.Log
import androidx.fragment.app.FragmentActivity
import jp.osdn.gokigen.aira01c.R
import jp.osdn.gokigen.aira01c.camera.interfaces.ICameraConnection
import jp.osdn.gokigen.aira01c.camera.interfaces.ICameraConnectionStatus
import jp.osdn.gokigen.aira01c.camera.interfaces.ICameraStatusReceiver
import jp.osdn.gokigen.aira01c.camera.omds.IOmdsProtocolNotify
import jp.osdn.gokigen.aira01c.camera.omds.status.IOmdsCommunicationInfo
import jp.osdn.gokigen.aira01c.camera.utils.communication.SimpleHttpClient
import java.lang.Exception
import java.util.HashMap

class OmdsCameraConnectSequence(private val context: FragmentActivity, private val cameraStatusReceiver: ICameraStatusReceiver, private val cameraConnection : ICameraConnection, private val communicationInfo: IOmdsCommunicationInfo, private val useOpcProtocolNotify: IOmdsProtocolNotify, private val liveViewQuality : String, userAgent : String = "OlympusCameraKit", private val executeUrl : String = "http://192.168.0.10") : Runnable
{
    private val headerMap: MutableMap<String, String> = HashMap()
    private val http = SimpleHttpClient()

    override fun run()
    {
        try
        {
            val getCommandListUrl = "$executeUrl/get_commandlist.cgi"
            val getConnectModeUrl = "$executeUrl/get_connectmode.cgi"
            val switchOpcCameraModeUrl = "$executeUrl/switch_cameramode.cgi"
            val switchCommPathUrl = "$executeUrl/switch_commpath.cgi"

            val response: String = http.httpGetWithHeader(getConnectModeUrl, headerMap, null, TIMEOUT_MS) ?: ""
            Log.v(TAG, " $getConnectModeUrl $response")
            if (response.isNotEmpty())
            {
                // コマンドリストを取得する
                val response2: String = http.httpGetWithHeader(getCommandListUrl, headerMap, null, TIMEOUT_MS) ?: ""
                Log.v(TAG, " $getCommandListUrl (${response2.length})")
                communicationInfo.setOmdsCommandList(response2)

                // --------- 通信経路をWiFiに(強制)変更する
                val response6: String = http.httpGetWithHeader("$switchCommPathUrl?path=wifi", headerMap, null, TIMEOUT_MS) ?: ""
                Log.v(TAG, " $switchCommPathUrl?path=wifi $response6")

                // OPCのコマンドを発行する
                val response5: String = http.httpGetWithHeader("$switchOpcCameraModeUrl?mode=standalone", headerMap, null, TIMEOUT_MS) ?: ""
                Log.v(TAG, " $switchOpcCameraModeUrl?mode=standalone $response5")
                if (response5.length > 5)
                {
                    Log.v(TAG, " -=-=-=-=-=- DETECTED OPC CAMERA -=-=-=-=-=-")
                    useOpcProtocolNotify.detectedOpcProtocol(true)
                    communicationInfo.startReceiveOpcEvent()
                }

                ////////////////  for TEST   ////////////////
                if (checkStatusDump)
                {
                    val testUrl = "$executeUrl/get_proplist.cgi"  // プロパティ一覧 (OPC)
                    //val testUrl = "$executeUrl/get_camprop.cgi?com=desc&propname=desclist"  // コマンド一覧
                    val testResponse: String = http.httpGetWithHeader(testUrl, headerMap, null, TIMEOUT_MS) ?: ""
                    Log.v(TAG, "     ------------------------------------------ ")
                    for (pos in 0..testResponse.length step 768)
                    {
                        val lastIndex = if ((pos + 768) > testResponse.length) { testResponse.length } else { pos + 768 }
                        Log.v(TAG, " $testUrl ($pos/${testResponse.length}) ${testResponse.substring(pos, lastIndex)}")
                    }
                    Log.v(TAG, "     ------------------------------------------ ")
                }
                ////////////////  for TEST   ////////////////

                onConnectNotify()
            }
            else
            {
                cameraConnection.alertConnectingFailed(context.getString(R.string.camera_not_found))
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
            cameraConnection.alertConnectingFailed(e.localizedMessage)
        }
    }

    private fun onConnectNotify()
    {
        try
        {
            val thread = Thread { // カメラとの接続確立を通知する
                cameraStatusReceiver.onStatusNotify(context.getString(R.string.connect_connected))
                cameraStatusReceiver.onCameraConnected()
                Log.v(TAG, "onConnectNotify()")
                cameraConnection.forceUpdateConnectionStatus(ICameraConnectionStatus.CameraConnectionStatus.CONNECTED)
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
        private val TAG = OmdsCameraConnectSequence::class.java.simpleName
        private const val TIMEOUT_MS = 5000
        private const val checkStatusDump = false
    }
}
