package jp.osdn.gokigen.aira01c.ui.configuration

import android.os.Bundle
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
import jp.osdn.gokigen.aira01c.R
import jp.osdn.gokigen.aira01c.camera.utils.CreditDialog

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
            view.findViewById<Button>(R.id.btnStandaloneShooting)?.setOnClickListener(onClickListener)
            view.findViewById<Button>(R.id.btnOthers)?.setOnClickListener(onClickListener)
            view.findViewById<Button>(R.id.btnStandalone01)?.setOnClickListener(onClickListener)
            view.findViewById<Button>(R.id.btnStandalone02)?.setOnClickListener(onClickListener)
            view.findViewById<Button>(R.id.btnStandalone03)?.setOnClickListener(onClickListener)
            view.findViewById<Button>(R.id.btnStandalone11)?.setOnClickListener(onClickListener)
            view.findViewById<Button>(R.id.btnStandalone12)?.setOnClickListener(onClickListener)
            view.findViewById<Button>(R.id.btnStandalone13)?.setOnClickListener(onClickListener)

            view.findViewById<TextView>(R.id.command_response_area)?.text = requireActivity().getString(R.string.blank)

            Log.v(TAG, "setupOnClickListener() done.")
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