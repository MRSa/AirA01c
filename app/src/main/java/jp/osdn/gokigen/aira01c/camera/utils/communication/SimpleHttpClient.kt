package jp.osdn.gokigen.aira01c.camera.utils.communication

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import java.io.*
import java.net.HttpURLConnection
import java.net.URL

class SimpleHttpClient
{
    /**
     *
     *
     *
     */
    fun httpGet(url: String, timeoutMs: Int): String
    {
        var inputStream : InputStream? = null
        var replyString = ""
        var timeout = timeoutMs
        if (timeoutMs < 0)
        {
            timeout = DEFAULT_TIMEOUT
        }

        //  HTTP GETメソッドで要求を投げる
        try
        {
            val httpConn = URL(url).openConnection() as HttpURLConnection
            try
            {
                httpConn.requestMethod = "GET"
                httpConn.connectTimeout = timeout
                httpConn.readTimeout = timeout
                httpConn.connect()
                val responseCode = httpConn.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK)
                {
                    inputStream = httpConn.inputStream
                }
                if (inputStream == null)
                {
                    Log.w(TAG, "httpGet: Response Code Error: $responseCode: $url")
                    return ("")
                }
            }
            catch (ee : Exception)
            {
                Log.w(TAG, "httpGet: " + url + "  " + ee.message)
                ee.printStackTrace()
                httpConn.disconnect()
                return ("")
            }
        }
        catch (e: Exception)
        {
            Log.w(TAG, "httpGet(2): " + url + "  " + e.message)
            e.printStackTrace()
            return ("")
        }

        // 応答を確認する
        try
        {
            val responseBuf = StringBuilder()
            val reader = BufferedReader(InputStreamReader(inputStream))
            var c: Int
            while (reader.read().also { c = it } != -1)
            {
                responseBuf.append(c.toChar())
            }
            replyString = responseBuf.toString()
            reader.close()
        }
        catch (e: Exception)
        {
            Log.w(TAG, "httpGet: exception: " + e.message)
            e.printStackTrace()
        }
        finally
        {
            try
            {
                inputStream.close()
            }
            catch (e: Exception)
            {
                e.printStackTrace()
            }
        }
        return (replyString)
    }

    /**
     *
     *
     *
     */
    fun httpGetBytes(url: String, setProperty: Map<String, String>?, timeoutMs: Int, callback: IReceivedMessageCallback)
    {
        httpCommandBytes(url, "GET", null, setProperty, null, timeoutMs, callback)
    }

    /**
     *
     *
     *
     */
    fun httpPostBytes(url: String, postData: String?, setProperty: Map<String, String>?, timeoutMs: Int, callback: IReceivedMessageCallback)
    {
        httpCommandBytes(url, "POST", postData, setProperty, null, timeoutMs, callback)
    }

    private fun httpCommandBytes(url: String, requestMethod: String, postData: String?, setProperty: Map<String, String>?, contentType: String?, timeoutMs: Int, callback: IReceivedMessageCallback)
    {
        var inputStream: InputStream? = null
        var timeout = timeoutMs
        if (timeoutMs < 0)
        {
            timeout = DEFAULT_TIMEOUT
        }

        //  HTTP メソッドで要求を送出
        try
        {
            val httpConn = URL(url).openConnection() as HttpURLConnection
            httpConn.requestMethod = requestMethod
            if (setProperty != null)
            {
                for (key in setProperty.keys)
                {
                    val value = setProperty[key]
                    httpConn.setRequestProperty(key, value)
                }
            }
            if (contentType != null)
            {
                httpConn.setRequestProperty("Content-Type", contentType)
            }
            httpConn.connectTimeout = timeout
            httpConn.readTimeout = timeout
            if (postData == null)
            {
                httpConn.connect()
            }
            else
            {
                httpConn.doInput = true
                httpConn.doOutput = true
                val outputStream = httpConn.outputStream
                val writer = OutputStreamWriter(outputStream, "UTF-8")
                writer.write(postData)
                writer.flush()
                writer.close()
                outputStream.close()
            }
            val responseCode = httpConn.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK)
            {
                inputStream = httpConn.inputStream
            }
            if (inputStream == null)
            {
                Log.w(TAG, " http $requestMethod Response Code Error: $responseCode: $url")
                callback.onErrorOccurred(NullPointerException())
                callback.onCompleted()
                return
            }

            // 応答を確認する
            try
            {
                var contentLength = httpConn.contentLength
                if (contentLength < 0)
                {
                    // コンテンツ長が取れない場合の処理...
                    try
                    {
                        val headers = httpConn.headerFields
                        // コンテンツ長さが取れない場合は、HTTP応答ヘッダから取得する
                        val valueList = headers["X-FILE_SIZE"]
                        try
                        {
                            if (valueList != null)
                            {
                                contentLength = getValue(valueList).toInt()
                            }
                        }
                        catch (ee: Exception)
                        {
                            ee.printStackTrace()
                        }
                    }
                    catch (e: Exception)
                    {
                        e.printStackTrace()
                    }
                }
                val buffer = ByteArray(BUFFER_SIZE)
                var readBytes = 0
                var readSize = inputStream.read(buffer, 0, BUFFER_SIZE)
                while (readSize != -1)
                {
                    callback.onReceive(readBytes, contentLength, readSize, buffer)
                    readBytes += readSize
                    readSize = inputStream.read(buffer, 0, BUFFER_SIZE)
                }
                Log.v(TAG, "RECEIVED $readBytes BYTES. (contentLength : $contentLength)")
                inputStream.close()
            }
            catch (e: Exception)
            {
                Log.w(TAG, "httpGet: exception: " + e.message)
                e.printStackTrace()
                callback.onErrorOccurred(e)
            }
            finally
            {
                try
                {
                    inputStream.close()
                }
                catch (e: Exception)
                {
                    e.printStackTrace()
                }
            }
        }
        catch (e: Exception)
        {
            Log.w(TAG, "http " + requestMethod + " " + url + "  " + e.message)
            e.printStackTrace()
            callback.onErrorOccurred(e)
            callback.onCompleted()
            return
        }
        callback.onCompleted()
    }


    private fun getValue(valueList: List<String>): String
    {
        // 応答ヘッダの値切り出し用...
        var isFirst = true
        val values = StringBuilder()
        for (value in valueList)
        {
            values.append(value)
            if (isFirst)
            {
                isFirst = false
            }
            else
            {
                values.append(" ")
            }
        }
        return values.toString()
    }

    fun httpGetBitmap(url: String, setProperty: Map<String, String>?, timeoutMs: Int): Bitmap?
    {
        return (httpCommandBitmap(url, "GET", null, setProperty, null, timeoutMs))
    }

    /**
     *
     *
     *
     */
    fun httpPostBitmap(url: String, postData: String?, timeoutMs: Int): Bitmap?
    {
        return (httpCommandBitmap(url, "POST", postData, null, null, timeoutMs))
    }

    /**
     *
     *
     *
     */
    private fun httpCommandBitmap(url: String, requestMethod: String, postData: String?, setProperty: Map<String, String>?, contentType: String?, timeoutMs: Int): Bitmap?
    {
        var inputStream: InputStream? = null
        var bmp: Bitmap? = null
        var timeout = timeoutMs
        if (timeoutMs < 0)
        {
            timeout = DEFAULT_TIMEOUT
        }

        //  HTTP メソッドで要求を送出
        try
        {
            val httpConn = URL(url).openConnection() as HttpURLConnection
            httpConn.requestMethod = requestMethod
            if (setProperty != null)
            {
                for (key in setProperty.keys)
                {
                    val value = setProperty[key]
                    httpConn.setRequestProperty(key, value)
                }
            }
            if (contentType != null)
            {
                httpConn.setRequestProperty("Content-Type", contentType)
            }
            httpConn.connectTimeout = timeout
            httpConn.readTimeout = timeout
            if (postData == null)
            {
                httpConn.connect()
            }
            else
            {
                httpConn.doInput = true
                httpConn.doOutput = true
                val outputStream = httpConn.outputStream
                val writer = OutputStreamWriter(outputStream, "UTF-8")
                writer.write(postData)
                writer.flush()
                writer.close()
                outputStream.close()
            }
            val responseCode = httpConn.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK)
            {
                inputStream = httpConn.inputStream
                if (inputStream != null)
                {
                    bmp = BitmapFactory.decodeStream(inputStream)
                }
            }
            if (inputStream == null)
            {
                Log.w(TAG, "http: ($requestMethod) Response Code Error: $responseCode: $url")
                return (null)
            }
            inputStream.close()
        }
        catch (e: Exception)
        {
            Log.w(TAG, "http: (" + requestMethod + ") " + url + "  " + e.message)
            e.printStackTrace()
            return (null)
        }
        return (bmp)
    }

    /**
     *
     *
     *
     */
    fun httpPost(url: String, postData: String?, timeoutMs: Int): String?
    {
        return (httpCommand(url, "POST", postData, null, null, timeoutMs))
    }

    /**
     *
     *
     *
     */
    fun httpGetWithHeader(url: String, headerMap: Map<String, String>?, contentType: String?, timeoutMs: Int): String?
    {
        return (httpCommand(url, "GET", null, headerMap, contentType, timeoutMs))
    }

    /**
     *
     *
     *
     */
    fun httpPostWithHeader(url: String, postData: String?, headerMap: Map<String, String>?, contentType: String?, timeoutMs: Int): String?
    {
        return (httpCommand(url, "POST", postData, headerMap, contentType, timeoutMs))
    }

    /**
     *
     *
     *
     */
    fun httpPutWithHeader(url: String, putData: String?, headerMap: Map<String, String>?, contentType: String?, timeoutMs: Int): String?
    {
        return (httpCommand(url, "PUT", putData, headerMap, contentType, timeoutMs))
    }

    /**
     *
     *
     *
     */
    fun httpPut(url: String, postData: String?, timeoutMs: Int): String?
    {
        return (httpCommand(url, "PUT", postData, null, null, timeoutMs))
    }

    /**
     *
     *
     *
     */
    fun httpOptions(url: String, optionsData: String?, timeoutMs: Int): String?
    {
        return (httpCommand(url, "OPTIONS", optionsData, null, null, timeoutMs))
    }

    /**
     *
     *
     *
     */
    private fun httpCommand(url: String, requestMethod: String, postData: String?, setProperty: Map<String, String>?, contentType: String?, timeoutMs: Int): String?
    {
        var inputStream: InputStream? = null
        var timeout = timeoutMs
        if (timeoutMs < 0)
        {
            timeout = DEFAULT_TIMEOUT
        }

        //  HTTP メソッドで要求を送出
        val responseCode : Int
        try
        {
            val httpConn = URL(url).openConnection() as HttpURLConnection
            httpConn.requestMethod = requestMethod
            if (setProperty != null)
            {
                for (key in setProperty.keys)
                {
                    val value = setProperty[key]
                    httpConn.setRequestProperty(key, value)
                }
            }
            if (contentType != null)
            {
                httpConn.setRequestProperty("Content-Type", contentType)
            }
            httpConn.connectTimeout = timeout
            httpConn.readTimeout = timeout
            if (postData == null)
            {
                httpConn.connect()
            }
            else
            {
                httpConn.doInput = true
                httpConn.doOutput = true
                val outputStream = httpConn.outputStream
                val writer = OutputStreamWriter(outputStream, "UTF-8")
                writer.write(postData)
                writer.flush()
                writer.close()
                outputStream.close()
            }
            responseCode = httpConn.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK)
            {
                inputStream = httpConn.inputStream
            }
            if (inputStream == null)
            {
                Log.w(TAG, "http $requestMethod : Response Code Error: $responseCode: $url")
                return "$responseCode "
            }
        }
        catch (e: Exception)
        {
            Log.w(TAG, "::http $requestMethod  $url ")
            e.printStackTrace()
            return ("")
        }

        // 応答の読み出し
        return "$responseCode ${readFromInputStream(inputStream)}"
    }

    private fun readFromInputStream(inputStream: InputStream?): String
    {
        //var reader: BufferedReader? = null
        var replyString = ""
        if (inputStream == null)
        {
            return ""
        }
        try
        {
            val responseBuf = StringBuilder()
            val reader = BufferedReader(InputStreamReader(inputStream))
            var c: Int
            while (reader.read().also { c = it } != -1)
            {
                responseBuf.append(c.toChar())
            }
            replyString = responseBuf.toString()
            reader.close()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
        return replyString
    }

    interface IReceivedMessageCallback
    {
        fun onCompleted()
        fun onErrorOccurred(e: Exception?)
        fun onReceive(readBytes: Int, length: Int, size: Int, data: ByteArray?)
    }

    companion object
    {
        private val TAG = SimpleHttpClient::class.java.simpleName
        private const val DEFAULT_TIMEOUT = 10 * 1000 // [ms]
        private const val BUFFER_SIZE = 131072 * 2 // 256kB
    }
}
