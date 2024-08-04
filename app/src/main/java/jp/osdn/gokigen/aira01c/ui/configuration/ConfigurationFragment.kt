package jp.osdn.gokigen.aira01c.ui.configuration

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import jp.osdn.gokigen.aira01c.R

class ConfigurationFragment : Fragment()
{
    private lateinit var onClickListener: ConfigurationOnClickListener
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View
    {
        val root: View = inflater.inflate(R.layout.fragment_configuration, container, false)
        setupOnClickListener(root)
        return root
    }

    private fun setupOnClickListener(view: View)
    {
        try
        {
            onClickListener = ConfigurationOnClickListener(requireActivity())
            view.findViewById<Button>(R.id.btnFormatSd)?.setOnClickListener(onClickListener)
            view.findViewById<Button>(R.id.btnDeleteAllContent)?.setOnClickListener(onClickListener)
            view.findViewById<Button>(R.id.btnSdCardReserve)?.setOnClickListener(onClickListener)
            view.findViewById<Button>(R.id.btnLevelReset)?.setOnClickListener(onClickListener)
            view.findViewById<Button>(R.id.btnLevelAdjust)?.setOnClickListener(onClickListener)
            view.findViewById<Button>(R.id.btnLevelReserve00)?.setOnClickListener(onClickListener)
            view.findViewById<Button>(R.id.btnLevelReserve01)?.setOnClickListener(onClickListener)
            view.findViewById<Button>(R.id.btnPixelMapping)?.setOnClickListener(onClickListener)
            view.findViewById<Button>(R.id.btnResetHardware)?.setOnClickListener(onClickListener)
            view.findViewById<Button>(R.id.btnHardwareReserve)?.setOnClickListener(onClickListener)
            view.findViewById<Button>(R.id.btnSendCommand)?.setOnClickListener(onClickListener)
            view.findViewById<Button>(R.id.btnNetworkSettings)?.setOnClickListener(onClickListener)
            view.findViewById<Button>(R.id.btnOthers)?.setOnClickListener(onClickListener)
            view.findViewById<Button>(R.id.btnStandalone01)?.setOnClickListener(onClickListener)
            view.findViewById<Button>(R.id.btnStandalone02)?.setOnClickListener(onClickListener)
            view.findViewById<Button>(R.id.btnStandalone03)?.setOnClickListener(onClickListener)
            view.findViewById<Button>(R.id.btnStandalone11)?.setOnClickListener(onClickListener)
            view.findViewById<Button>(R.id.btnStandalone12)?.setOnClickListener(onClickListener)
            view.findViewById<Button>(R.id.btnStandalone13)?.setOnClickListener(onClickListener)

        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }


    companion object
    {
        private val TAG = ConfigurationFragment::class.java.simpleName
    }

}