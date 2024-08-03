package jp.osdn.gokigen.aira01c.ui.home

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import jp.osdn.gokigen.aira01c.R
import jp.osdn.gokigen.aira01c.databinding.FragmentHomeBinding
import jp.osdn.gokigen.aira01c.ui.tips.TipsFragment

class HomeFragment : Fragment()
{
    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
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
            if (isCameraConnected())
            {
                binding.textHome.text = getString(R.string.connected_message)
            }
            else
            {
                binding.textHome.text = getString(R.string.initial_message)
            }
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
            // activity?.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))

            changeButtonStatus(false)
            binding.textHome.text = getString(R.string.initial_message)

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
            // activity?.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))

            changeButtonStatus(true)
            binding.textHome.text = getString(R.string.connected_message)

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
            // activity?.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
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
            // activity?.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    private fun pushedRefresh()
    {
        Log.v(TAG, "pushedRefresh()")
        try
        {
            // activity?.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))

            binding.textHome.text = getString(R.string.connection_error_message)
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
                try
                {
                    requireActivity().findViewById<Button>(R.id.btnConnect)?.isEnabled = !isConnected
                    requireActivity().findViewById<Button>(R.id.btnDisconnect)?.isEnabled = isConnected
                    requireActivity().findViewById<Button>(R.id.btnModeReset)?.isEnabled = isConnected
                    requireActivity().findViewById<Button>(R.id.btnTimeSync)?.isEnabled = isConnected
                    requireActivity().findViewById<Button>(R.id.btnRefresh)?.isEnabled = isConnected
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
        return (false)
    }

    companion object
    {
        private val TAG = HomeFragment::class.java.simpleName
    }

}
