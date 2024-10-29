package jp.osdn.gokigen.aira01c.ui.home

import android.app.Dialog
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatSpinner
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import androidx.preference.PreferenceManager
import jp.osdn.gokigen.aira01c.AppSingleton
import jp.osdn.gokigen.aira01c.R
import jp.osdn.gokigen.aira01c.camera.interfaces.IVibrator


class OpcAppLaunchDialog: DialogFragment(), View.OnClickListener
{
    private lateinit var myContext: FragmentActivity
    private lateinit var myView: View
    private lateinit var alertDialog: AlertDialog.Builder
    private lateinit var preferences: SharedPreferences
    private var container: ViewGroup? = null

    private var selectedApplication: TargetApplicationInfo? = null
    private val installedApplicationList = ArrayList<TargetApplicationInfo>()

    private fun prepare(context: FragmentActivity)
    {
        this.myContext = context
        this.preferences = PreferenceManager.getDefaultSharedPreferences(context)
        this.installedApplicationList.clear()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog
    {
        try
        {
            return (showDialog())
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
        return (super.onCreateDialog(savedInstanceState))
    }

    private fun showDialog(): Dialog
    {
        // 表示イアログの生成
        if (!::alertDialog.isInitialized)
        {
            alertDialog = AlertDialog.Builder(myContext)
        }
        if (!::myView.isInitialized)
        {
            val inflater = myContext.layoutInflater
            myView = inflater.inflate(R.layout.dialog_launch_app_selection, container, false)
        }
        setupDialog()
        alertDialog.setView(myView)
        alertDialog.setCancelable(true)
        prepareSpinner()
        return (alertDialog.create())
    }

    private fun setupDialog()
    {
        try
        {
            if (::myView.isInitialized)
            {
                myView.findViewById<Button>(R.id.launch_app_button).setOnClickListener(this)
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    private fun prepareSpinner()
    {
        try
        {
            val adapter = ArrayAdapter<String>(this.requireContext(), android.R.layout.simple_spinner_item)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

            // ---------------------------------------------------
            if (DUMP_INSTALLED_APPLICATIONS)
            {
                val pm: PackageManager = requireActivity().packageManager
                val packageInfoList = pm.getInstalledPackages(PackageManager.GET_ACTIVITIES or PackageManager.GET_SERVICES)
                for (packageInfo in packageInfoList)
                {
                    if (pm.getLaunchIntentForPackage(packageInfo.packageName) != null)
                    {
                        val packageName = packageInfo.packageName
                        val className = pm.getLaunchIntentForPackage(packageInfo.packageName)?.component?.className + ""
                        Log.v(TAG, "  Application Package : $packageName  $className")
                    }
                    else
                    {
                        Log.v(TAG, "  Cannot Launch Package: ${packageInfo.packageName}")
                    }
                }
            }
            // ---------------------------------------------------

            // ---------------------------------------------------
            if (checkIsApplicationInstalled("jp.osdn.gokigen.aira01b")) {
                adapter.add("AirA01b")
                installedApplicationList.add(TargetApplicationInfo("AirA01a", "jp.osdn.gokigen.aira01b", "jp.osdn.gokigen.aira01b.MainActivity"))
            }

            if (checkIsApplicationInstalled("jp.osdn.gokigen.aira01a")) {
                adapter.add("AirA01a")
                installedApplicationList.add(TargetApplicationInfo("AirA01a", "jp.osdn.gokigen.aira01a", "jp.osdn.gokigen.aira01a.MainActivity"))
            }

            if (checkIsApplicationInstalled("net.osdn.gokigen.gr2control"))
            {
                adapter.add("A01GR2")
                installedApplicationList.add(TargetApplicationInfo("A01GR2", "net.osdn.gokigen.gr2control", "net.osdn.gokigen.gr2control.Gr2ControlMain"))
            }

            if (checkIsApplicationInstalled("jp.osdn.gokigen.mangle"))
            {
                adapter.add("A01e")
                installedApplicationList.add(TargetApplicationInfo("A01e", "jp.osdn.gokigen.mangle", "jp.osdn.gokigen.mangle.MainActivity"))
            }

            if (checkIsApplicationInstalled("net.osdn.gokigen.pkremote"))
            {
                adapter.add("A01DL")
                installedApplicationList.add(TargetApplicationInfo("A01DL", "net.osdn.gokigen.pkremote", "net.osdn.gokigen.pkremote.MainActivity"))
            }

            if (checkIsApplicationInstalled("net.osdn.gokigen.a01d"))
            {
                adapter.add("A01d")
                installedApplicationList.add(TargetApplicationInfo("A01d", "net.osdn.gokigen.a01d", "net.osdn.gokigen.a01d.A01dMain"))
            }

            if (checkIsApplicationInstalled("jp.olympusimaging.oamodedial"))
            {
                adapter.add("OA.ModeDial")
                installedApplicationList.add(TargetApplicationInfo("OA.ModeDial", "jp.olympusimaging.oamodedial", "jp.olympusimaging.oamodedial.SplashActivity"))
            }

            if (checkIsApplicationInstalled("jp.olympusimaging.oaviewer"))
            {
                adapter.add("OA.Viewer")
                installedApplicationList.add(TargetApplicationInfo("OA.Viewer", "jp.olympusimaging.oaviewer", "jp.olympusimaging.oaviewer.SplashActivity"))
            }

            if (checkIsApplicationInstalled("jp.olympusimaging.oaartfilter"))
            {
                adapter.add("OA.ArtFilter")
                installedApplicationList.add(TargetApplicationInfo("OA.ArtFilter", "jp.olympusimaging.oaartfilter", "jp.olympusimaging.oaartfilter.SplashActivity"))
            }

            if (checkIsApplicationInstalled("jp.olympusimaging.oacolorcreator"))
            {
                adapter.add("OA.ColorCreator")
                installedApplicationList.add(TargetApplicationInfo("OA.ColorCreator", "jp.olympusimaging.oacolorcreator", "jp.olympusimaging.oacolorcreator.SplashActivity"))
            }

            if (checkIsApplicationInstalled("jp.olympusimaging.oacentral"))
            {
                adapter.add("OA.Central")
                installedApplicationList.add(TargetApplicationInfo("OA.Central", "jp.olympusimaging.oacentral", "jp.olympusimaging.oacentral.SplashActivity"))
            }

            // ---------------------------------------------------


            if (adapter.isEmpty)
            {
                // ----- インストールされていないときには、選択肢を１つだけ表示する （でも当然起動しない）
                adapter.add("-----")
            }
            if (installedApplicationList.isNotEmpty())
            {
                selectedApplication = installedApplicationList[0]
            }

            val spinner: AppCompatSpinner = myView.findViewById(R.id.target_application_selection)
            spinner.adapter = adapter
            spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener
            {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    pos: Int,
                    id: Long
                ) {
                    Log.v(TAG, "onItemSelected(parent: $parent, view: $view, pos: $pos, id: $id)")
                    if (installedApplicationList.isNotEmpty())
                    {
                        selectedApplication = installedApplicationList[pos]
                    }
                }

                override fun onNothingSelected(p0: AdapterView<*>?)
                {
                    Log.v(TAG, "onNothingSelected()")
                    selectedApplication = null
                }
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    private fun checkIsApplicationInstalled(target: String): Boolean
    {
        Log.v(TAG, "checkIsApplicationInstalled : $target")
        return (true)
    }

    private fun launchApplication(targetApplication: TargetApplicationInfo?)
    {
        try
        {
            // ------ アプリケーションを起動する
            Log.v(TAG, "App. launch : ${targetApplication?.packageName} (${targetApplication?.className})")
            if (targetApplication != null)
            {
                val intent = Intent().setClassName(targetApplication.packageName, targetApplication.className)
                requireContext().startActivity(intent)
                return
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
        try
        {
            // ----- 起動失敗を通知
            Toast.makeText(requireContext(), requireContext().getString(R.string.launch_failure), Toast.LENGTH_SHORT).show()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    override fun onClick(view: View)
    {
        when (view.id)
        {
            R.id.launch_app_button -> {
                // ---- 選択ているアプリを起動する
                AppSingleton.vibrator.vibrate(requireContext(), IVibrator.VibratePattern.SIMPLE_MIDDLE)
                try
                {
                    Log.v(TAG, "Launch : ${selectedApplication?.packageName} (${selectedApplication?.className})")
                    if (selectedApplication != null)
                    {
                        launchApplication(selectedApplication)
                    }
                    else
                    {
                        Toast.makeText(requireContext(), requireContext().getString(R.string.launch_no_package_selection), Toast.LENGTH_SHORT).show()
                    }
                }
                catch (e: Exception)
                {
                    e.printStackTrace()
                }
            }
            else -> {
                Log.v(TAG, " onClick() : ${view.id}")
            }
        }
    }

    private data class TargetApplicationInfo(val appName: String, val packageName: String, val className: String)

    companion object
    {
        val TAG: String = OpcAppLaunchDialog::class.java.simpleName
        private const val DUMP_INSTALLED_APPLICATIONS = false
        fun newInstance(context: FragmentActivity): OpcAppLaunchDialog
        {
            val instance = OpcAppLaunchDialog()
            instance.prepare(context)
            return (instance)
        }
    }
}
