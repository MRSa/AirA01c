package jp.osdn.gokigen.aira01c.camera.utils.communication

import android.util.Log
import java.io.*
import java.net.HttpURLConnection
import java.net.URL

class SimpleLiveViewSlicer
{
    class Payload(private val jpegData: ByteArray?, val paddingData: ByteArray?)
    {
        fun getJpegData(): ByteArray?
        {
            return jpegData
        }
    }

    private var mJpegStartMarker = intArrayOf(0x0d, 0x0a, 0x0d, 0x0a, 0xff, 0xd8)
    private var mHttpConn: HttpURLConnection? = null
    private var mInputStream : InputStream? = null

    fun setMJpegStartMarker(startMarker: IntArray)
    {
        mJpegStartMarker = startMarker
    }

    fun open(liveViewUrl: String?, postData: String?, contentType: String?)
    {
        try
        {
            if ((mInputStream != null)||(mHttpConn != null))
            {
                Log.v(TAG, "Slicer is already open.")
                return
            }
            val urlObj = URL(liveViewUrl)
            mHttpConn = urlObj.openConnection() as HttpURLConnection
            mHttpConn?.requestMethod = "POST"
            mHttpConn?.connectTimeout = CONNECTION_TIMEOUT
            if (contentType != null)
            {
                mHttpConn?.setRequestProperty("Content-Type", contentType)
            }
            run {
                try
                {
                    mHttpConn?.doInput = true
                    mHttpConn?.doOutput = true
                    val outputStream = mHttpConn?.outputStream
                    val writer = OutputStreamWriter(outputStream, "UTF-8")
                    writer.write(postData)
                    writer.flush()
                    writer.close()
                    outputStream?.close()
                }
                catch (e : Exception)
                {
                    e.printStackTrace()
                }
            }
            if (mHttpConn?.responseCode == HttpURLConnection.HTTP_OK)
            {
                mInputStream = mHttpConn?.inputStream
            }
            else
            {
                Log.v(TAG, " RESPONSE NG : " + mHttpConn?.responseCode + " " + mHttpConn?.responseMessage)
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    fun open(liveViewUrl: String?)
    {
        try
        {
            if ((mInputStream != null)||(mHttpConn != null))
            {
                Log.v(TAG, "Slicer is already open.")
                return
            }
            val urlObj = URL(liveViewUrl)
            mHttpConn = urlObj.openConnection() as HttpURLConnection
            mHttpConn?.requestMethod = "GET"
            mHttpConn?.connectTimeout = CONNECTION_TIMEOUT
            mHttpConn?.connect()
            if (mHttpConn?.responseCode == HttpURLConnection.HTTP_OK)
            {
                Log.v(TAG, " LIVEVIEW REQUEST ACCEPTED : ${mHttpConn?.responseCode}")
                mInputStream = mHttpConn?.inputStream
            }
            else
            {
                Log.v(TAG, " LIVEVIEW REQUEST RESPONSE NG : ${mHttpConn?.responseCode}")
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    fun close()
    {
        try
        {
            if (mInputStream != null)
            {
                mInputStream?.close()
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
        try
        {
            if (mHttpConn != null)
            {
                mHttpConn?.disconnect()
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    fun nextPayload(): Payload?
    {
        var payload: Payload? = null
        try
        {
            while ((mInputStream != null)&&(payload == null))
            {
                // Common Header
                var readLength = 1 + 1 + 2 + 4
                val commonHeader = readBytes(mInputStream!!, readLength)
                if ((commonHeader == null)||(commonHeader.size != readLength))
                {
                    Log.v(TAG, "Cannot read stream for common header.")
                    payload = null
                    break
                }
                if (commonHeader[0].toUByte().toByte() != 0xFF.toByte())
                {
                    Log.v(TAG, "Unexpected data format. (Start byte)")
                    payload = null
                    break
                }
                when (commonHeader[1].toUByte().toByte())
                {
                    0x12.toByte() -> {
                        // This is information header for streaming. skip this packet.
                        readLength = 4 + 3 + 1 + 2 + 118 + 4 + 4 + 24
                        readBytes(mInputStream!!, readLength)
                    }
                    0x01.toByte(), 0x11.toByte() -> payload = readPayload()
                    else -> {
                    }
                }
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
            System.gc()
        }
        return payload
    }

    private fun readPayload(): Payload?
    {
        try
        {
            if (mInputStream != null)
            {
                // Payload Header
                val readLength = 4 + 3 + 1 + 4 + 1 + 115
                val payloadHeader = readBytes(mInputStream!!, readLength)
                if (payloadHeader == null || payloadHeader.size != readLength)
                {
                    throw EOFException("Cannot read stream for payload header.")
                }
                if (payloadHeader[0].toUByte().toByte() != 0x24.toByte() || payloadHeader[1].toUByte().toByte() != 0x35.toByte() || payloadHeader[2].toUByte().toByte() != 0x68.toByte() || payloadHeader[3].toUByte().toByte() != 0x79.toByte())
                {
                    throw EOFException("Unexpected data format. (Start code)")
                }
                val jpegSize = bytesToInt(payloadHeader, 4, 3)
                val paddingSize = bytesToInt(payloadHeader, 7, 1)

                // Payload Data
                val jpegData = readBytes(mInputStream!!, jpegSize)
                val paddingData = readBytes(mInputStream!!, paddingSize)
                return Payload(jpegData, paddingData)
            }
        }
        catch (eo: EOFException)
        {
            eo.printStackTrace()
            close()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
        return (null)
    }

    /**
     * 先頭のjpegマーカーが出てくるまで読み飛ばす
     *
     */
    private fun skipJpegMarkStart(stream: InputStream?)
    {
        if (stream == null)
        {
            return
        }
        var searchIndex = 0
        while (true)
        {
            try
            {
                val data = stream.read()
                if (data == mJpegStartMarker[searchIndex])
                {
                    searchIndex++
                    if (searchIndex >= mJpegStartMarker.size)
                    {
                        break
                    }
                }
            }
            catch (e: Exception)
            {
                e.printStackTrace()
                return
            }
        }
    }

    /**
     *
     *
     */
    fun nextPayloadForMotionJpeg(): Payload?
    {
        var searchIndex = 0
        val endMarker = intArrayOf(0xff, 0xd9)
        var payload: Payload? = null
        try
        {
            while ((mInputStream != null)&&(payload == null))
            {
                skipJpegMarkStart(mInputStream)
                val tmpByteArray = ByteArrayOutputStream()
                // 先頭にJPEGのマークを詰める
                tmpByteArray.write(0xff)
                tmpByteArray.write(0xd8)
                while (true)
                {
                    try
                    {
                        // 1byteづつの読み込み... 本当は複数バイト読み出しで処理したい
                        val data = mInputStream?.read()
                        if (data != null)
                        {
                            tmpByteArray.write(data)
                            if (data == endMarker[searchIndex])
                            {
                                searchIndex++
                                if (searchIndex >= endMarker.size)
                                {
                                    break
                                }
                            }
                            else
                            {
                                searchIndex = 0
                            }
                        }
                    }
                    catch (e: Throwable)
                    {
                        Log.v(TAG, "INPUT STREAM EXCEPTION : " + e.localizedMessage)
                        return (null)
                    }
                }
                payload = Payload(tmpByteArray.toByteArray(), null)
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
        return (payload)
    }

    companion object
    {
        private val TAG = SimpleLiveViewSlicer::class.java.simpleName
        private const val CONNECTION_TIMEOUT = 2000 // [msec]
        private fun bytesToInt(byteData: ByteArray, startIndex: Int, count: Int): Int
        {
            var ret = 0
            try
            {
                for (i in startIndex until startIndex + count)
                {
                    ret = ret shl 8 or (byteData[i].toUByte().toInt() and 0xff)
                }
            }
            catch (e: Exception)
            {
                e.printStackTrace()
            }
            return ret
        }

        private fun readBytes(inputStream: InputStream, length: Int): ByteArray?
        {
            var ret: ByteArray?
            try
            {
                val tmpByteArray = ByteArrayOutputStream()
                val buffer = ByteArray(1024)
                while (true)
                {
                    val trialReadlen = Math.min(buffer.size, length - tmpByteArray.size())
                    val readlen = inputStream.read(buffer, 0, trialReadlen)
                    if (readlen < 0)
                    {
                        break
                    }
                    tmpByteArray.write(buffer, 0, readlen)
                    if (length <= tmpByteArray.size())
                    {
                        break
                    }
                }
                ret = tmpByteArray.toByteArray()
                tmpByteArray.close()
            }
            catch (e: Exception)
            {
                e.printStackTrace()
                ret = null
            }
            return ret
        }
    }
}
