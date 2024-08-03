package jp.osdn.gokigen.aira01c.ui.home

import android.annotation.SuppressLint
import android.content.Context.CONNECTIVITY_SERVICE
import android.content.Context.WIFI_SERVICE
import android.content.Intent
import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.Network
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import jp.osdn.gokigen.aira01c.AppSingleton.Companion.cameraControl
import jp.osdn.gokigen.aira01c.R
import jp.osdn.gokigen.aira01c.camera.interfaces.ICameraConnectionStatus
import jp.osdn.gokigen.aira01c.camera.interfaces.IMessageDrawer
import jp.osdn.gokigen.aira01c.camera.utils.ConfirmationDialog
import jp.osdn.gokigen.aira01c.camera.utils.ConfirmationDialog.ConfirmationCallback
import jp.osdn.gokigen.aira01c.databinding.FragmentHomeBinding
import java.net.Inet4Address

class HomeFragment : Fragment(), IMessageDrawer
{
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val textView: TextView = binding.textHome
        homeViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        textView.text = getString(R.string.initial_message)

        val myStatus: TextView = binding.myTextStatus
        homeViewModel.myStatusText.observe(viewLifecycleOwner) {
            myStatus.text = it
        }

        setupScreen(root)

        return root
    }

    private fun setupScreen(view: View)
    {
        try
        {
            // ----- カメラに接続する
            view.findViewById<Button>(R.id.btnConnect)?.setOnClickListener {
                pushedConnect()
            }

            // ----- カメラから切断する
            view.findViewById<Button>(R.id.btnDisconnect)?.setOnClickListener {
                pushedDisconnect()
            }

            // ----- Wifi設定画面を開く
            view.findViewById<Button>(R.id.btnWifiSet)?.setOnClickListener {
                 pushedWifiSet()
            }

            // ----- モードリセット
            view.findViewById<Button>(R.id.btnModeReset)?.setOnClickListener {
                pushedModeReset()
            }

            // ----- 時刻設定
            view.findViewById<Button>(R.id.btnTimeSync)?.setOnClickListener {
                pushedTimeSync()
            }

            // ----- 情報の更新
            view.findViewById<Button>(R.id.btnRefresh)?.setOnClickListener {
                pushedRefresh()
            }

            // ----- ステータス領域をスクロールできるようにする
            view.findViewById<TextView>(R.id.myTextStatus)?.movementMethod = ScrollingMovementMethod()

            cameraControl.initialize(requireActivity(), this)

        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    override fun onResume()
    {
        super.onResume()

        try
        {
            isCameraConnected()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun pushedWifiSet()
    {
        Log.v(TAG, "pushedWifiSet()")
        try
        {
            isCameraConnected()
            activity?.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    private fun pushedDisconnect()
    {
        Log.v(TAG, "pushedDisconnect()")
        try
        {
            // ----- 確認ダイアログを表示し、OKの場合はアプリ終了＆カメラ電源OFFにする
            val confirmation = ConfirmationDialog.newInstance(requireContext())
            confirmation.show(
                R.string.dialog_title_exit_application,
                R.string.dialog_message_power_off,
                object : ConfirmationCallback {
                    override fun confirm() {
                        cameraControl.finishCamera(true)
                        requireActivity().finish()
                    }
                }
            )
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    private fun pushedConnect()
    {
        Log.v(TAG, "pushedConnect()")
        try
        {
            cameraControl.startCamera()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    private fun pushedModeReset()
    {
        Log.v(TAG, "pushedModeReset()")
        try
        {
            cameraControl.changeRunMode("standalone")
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    private fun pushedTimeSync()
    {
        Log.v(TAG, "pushedTimeSync()")
        try
        {
            cameraControl.synchronizeTime()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun pushedRefresh()
    {
        Log.v(TAG, "pushedRefresh()")
        try
        {
            cameraControl.getCameraStatus()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    @Suppress("DEPRECATION")
    private fun getIpAddressBefore21()
    {
        try
        {
            val manager: WifiManager = activity?.applicationContext?.getSystemService(WIFI_SERVICE) as WifiManager
            val ip = manager.connectionInfo.ipAddress
            val ipAddress = arrayOf(ip,ip shr 8,ip shr 16,ip shr 24).map{it and 0xff}.joinToString(".")
            updateIpAddress(ipAddress)
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun getIpAddressAfter21()
    {
        try
        {
            val manager: ConnectivityManager = activity?.applicationContext?.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkCallback = object: ConnectivityManager.NetworkCallback()
            {
                override fun onLinkPropertiesChanged(network: Network, linkProperties: LinkProperties) {
                    super.onLinkPropertiesChanged(network, linkProperties)
                    val ipAddress = linkProperties.linkAddresses.filter{ it.address is Inet4Address }[0].toString()
                    updateIpAddress(ipAddress)
                }
            }
            manager.registerDefaultNetworkCallback(networkCallback)
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
        return
    }

    private fun getIpAddress(isConnected: Boolean)
    {
        if (isConnected)
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            {
                getIpAddressAfter21()
            }
            else
            {
                getIpAddressBefore21()
            }
        }
        else
        {
            updateIpAddress("")
        }
    }

    private fun updateIpAddress(address: String)
    {
        try
        {
            requireActivity().runOnUiThread {
                try
                {
                    requireActivity().findViewById<TextView>(R.id.ip_address).text = address
                }
                catch (e: Exception)
                {
                    e.printStackTrace()
                }
            }
        }
        catch (e:Exception)
        {
            e.printStackTrace()
        }
    }

    private fun changeButtonStatus(isConnected: Boolean)
    {
        try
        {
            requireActivity().runOnUiThread {
                try
                {
                    requireActivity().findViewById<Button>(R.id.btnConnect)?.isEnabled = !isConnected
                    requireActivity().findViewById<Button>(R.id.btnDisconnect)?.isEnabled = isConnected
                    requireActivity().findViewById<Button>(R.id.btnModeReset)?.isEnabled = isConnected
                    requireActivity().findViewById<Button>(R.id.btnTimeSync)?.isEnabled = isConnected
                    requireActivity().findViewById<Button>(R.id.btnRefresh)?.isEnabled = isConnected

                    getIpAddress(isConnected)
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

    private fun isCameraConnected() : Boolean
    {
        val status = cameraControl.getConnectionStatus()
        requireActivity().runOnUiThread {
            when (status) {
                ICameraConnectionStatus.CameraConnectionStatus.CONNECTED -> {
                    binding.textHome.text = getString(R.string.connected_message)
                    changeButtonStatus(true)
                }
                ICameraConnectionStatus.CameraConnectionStatus.DISCONNECTED -> {
                    binding.textHome.text = getString(R.string.initial_message)
                    changeButtonStatus(false)
                }
                ICameraConnectionStatus.CameraConnectionStatus.CONNECTING -> {
                    binding.textHome.text = getString(R.string.connecting_message)
                    changeButtonStatus(false)
                }
                else -> {
                    binding.textHome.text = getString(R.string.initial_message)
                    changeButtonStatus(false)
                }
            }
        }
        return (status  == ICameraConnectionStatus.CameraConnectionStatus.CONNECTED)
    }

    @SuppressLint("SetTextI18n")
    override fun setMessageToShow(message: String)
    {
        try
        {
            requireActivity().runOnUiThread {
                binding.myTextStatus.text = message
                isCameraConnected()

            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    @SuppressLint("SetTextI18n")
    override fun appendMessageToShow(message: String)
    {
        try
        {
            requireActivity().runOnUiThread {
                binding.myTextStatus.text = "${binding.myTextStatus.text}\n$message"
                isCameraConnected()
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    @SuppressLint("SetTextI18n")
    override fun clear()
    {
        try
        {
            requireActivity().runOnUiThread {
                binding.myTextStatus.text = ""
                isCameraConnected()
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    override fun invalidate()
    {
        try
        {
            isCameraConnected()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    companion object
    {
        private val TAG = HomeFragment::class.java.simpleName
    }
}
