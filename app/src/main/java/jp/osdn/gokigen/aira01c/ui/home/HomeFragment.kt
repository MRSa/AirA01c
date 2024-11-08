package jp.osdn.gokigen.aira01c.ui.home

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import jp.osdn.gokigen.aira01c.AppSingleton
import jp.osdn.gokigen.aira01c.AppSingleton.Companion.cameraControl
import jp.osdn.gokigen.aira01c.R
import jp.osdn.gokigen.aira01c.ble.MyBleAdapter
import jp.osdn.gokigen.aira01c.ble.PowerOnCamera
import jp.osdn.gokigen.aira01c.camera.interfaces.ICameraConnectionStatus
import jp.osdn.gokigen.aira01c.camera.interfaces.IMessageDrawer
import jp.osdn.gokigen.aira01c.camera.interfaces.IVibrator
import jp.osdn.gokigen.aira01c.camera.utils.ConfirmationDialog
import jp.osdn.gokigen.aira01c.camera.utils.ConfirmationDialog.ConfirmationCallback
import jp.osdn.gokigen.aira01c.camera.utils.CreditDialog
import jp.osdn.gokigen.aira01c.databinding.FragmentHomeBinding

class HomeFragment : Fragment(), IMessageDrawer
{
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var bleDeviceList : MyBleAdapter
    private lateinit var cameraPowerOn : PowerOnCamera

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel = ViewModelProvider(this)[HomeViewModel::class.java]

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.app_bar_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                when (menuItem.itemId) {
                    R.id.action_about_gokigen -> {
                        try
                        {
                            CreditDialog.newInstance(requireContext()).show()
                        }
                        catch (e: Exception)
                        {
                            e.printStackTrace()
                        }
                    }
                }
                return true
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun setupScreen(view: View)
    {
        try
        {
            // ----- カメラに接続する
            view.findViewById<Button>(R.id.btnConnect)?.setOnClickListener {
                AppSingleton.vibrator.vibrate(requireContext(), IVibrator.VibratePattern.SIMPLE_SHORT)
                pushedConnect()
            }

            // ----- カメラから切断する
            view.findViewById<Button>(R.id.btnDisconnect)?.setOnClickListener {
                AppSingleton.vibrator.vibrate(requireContext(), IVibrator.VibratePattern.SIMPLE_SHORT_SHORT)
                pushedDisconnect()
            }

            // ----- Wifi設定画面を開く
            view.findViewById<Button>(R.id.btnWifiSet)?.setOnClickListener {
                AppSingleton.vibrator.vibrate(requireContext(), IVibrator.VibratePattern.SIMPLE_SHORT)
                 pushedWifiSet()
            }

            // ----- モードリセット
            view.findViewById<Button>(R.id.btnModeReset)?.setOnClickListener {
                AppSingleton.vibrator.vibrate(requireContext(), IVibrator.VibratePattern.SIMPLE_SHORT)
                pushedModeReset()
            }

            // ----- 時刻設定
            view.findViewById<Button>(R.id.btnTimeSync)?.setOnClickListener {
                AppSingleton.vibrator.vibrate(requireContext(), IVibrator.VibratePattern.SIMPLE_SHORT)
                pushedTimeSync()
            }

            // ----- 情報の更新
            view.findViewById<Button>(R.id.btnRefresh)?.setOnClickListener {
                AppSingleton.vibrator.vibrate(requireContext(), IVibrator.VibratePattern.SIMPLE_SHORT)
                pushedRefresh()
            }

            // ----- Bluetooth LE の制御
            view.findViewById<Button>(R.id.btnBlePowerControl)?.setOnClickListener {
                AppSingleton.vibrator.vibrate(requireContext(), IVibrator.VibratePattern.SIMPLE_SHORT)
                pushedBlePowerControl()
            }

            // ----- アプリケーションの起動
            view.findViewById<Button>(R.id.btnLaunchApp)?.setOnClickListener {
                AppSingleton.vibrator.vibrate(requireContext(), IVibrator.VibratePattern.SIMPLE_SHORT)
                pushedLaunchApplication()
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
            cameraControl.connectToCamera()
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
            cameraControl.changeCommPath("wifi")
            try
            {
                Thread.sleep(75)
            }
            catch (ee: Exception)
            {
                ee.printStackTrace()
            }
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

    private fun pushedBlePowerControl()
    {
        Log.v(TAG, "pushedBlePowerControl()")
        try
        {
            // -------- Bluetooth LE の制御 （ダイアログを開く）
            try
            {
                // ----- BLEのデバイス一覧保持クラスを初期化する
                if (!::bleDeviceList.isInitialized)
                {
                    bleDeviceList = MyBleAdapter(requireActivity())
                }
                bleDeviceList.prepare()

                // ----- Bluetooth経由でカメラの電源をONにするクラスの生成
                if (!::cameraPowerOn.isInitialized)
                {
                    cameraPowerOn = PowerOnCamera(requireActivity(), bleDeviceList)
                }

                BleControlDialog.newInstance(requireActivity(), bleDeviceList, cameraPowerOn)
                    .show(requireActivity().supportFragmentManager, TAG)
            }
            catch (e: Exception)
            {
                e.printStackTrace()
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    private fun pushedLaunchApplication()
    {
        // -------- 選択したアプリケーションの起動 （ダイアログを開く）
        Log.v(TAG, "pushedLaunchApplication()")
        try
        {
            OpcAppLaunchDialog.newInstance(requireActivity())
                .show(requireActivity().supportFragmentManager, TAG)
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    private fun changeButtonStatus(isConnected: Boolean)
    {
        try
        {
            requireActivity().runOnUiThread {
                try {
                    val btnConnect = requireActivity().findViewById<Button>(R.id.btnConnect)
                    if (btnConnect.isEnabled == isConnected)
                    {
                        btnConnect.isEnabled = !isConnected
                    }

                    val btnDisconnect = requireActivity().findViewById<Button>(R.id.btnDisconnect)
                    if (btnDisconnect.isEnabled != isConnected)
                    {
                        btnDisconnect.isEnabled = isConnected
                    }

                    val btnModeReset = requireActivity().findViewById<Button>(R.id.btnModeReset)
                    if (btnModeReset.isEnabled != isConnected) {
                        btnModeReset.isEnabled = isConnected
                    }

                    val btnTimeSync = requireActivity().findViewById<Button>(R.id.btnTimeSync)
                    if (btnTimeSync.isEnabled != isConnected) {
                        btnTimeSync.isEnabled = isConnected
                    }

                    val btnRefresh = requireActivity().findViewById<Button>(R.id.btnRefresh)
                    if (btnRefresh.isEnabled != isConnected) {
                        btnRefresh.isEnabled = isConnected
                    }
                    // getIpAddress(isConnected)
                } catch (e: Exception) {
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
