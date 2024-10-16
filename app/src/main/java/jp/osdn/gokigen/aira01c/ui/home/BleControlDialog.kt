package jp.osdn.gokigen.aira01c.ui.home

import android.app.Dialog
import android.content.Context.VIBRATOR_MANAGER_SERVICE
import android.content.Context.VIBRATOR_SERVICE
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatSpinner
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import jp.osdn.gokigen.aira01c.R
import jp.osdn.gokigen.aira01c.ble.MyBleAdapter
import jp.osdn.gokigen.aira01c.camera.interfaces.IVibrator

class BleControlDialog : DialogFragment(), View.OnClickListener
{
    private lateinit var myContext: FragmentActivity
    private lateinit var bleDeviceList: MyBleAdapter
    private lateinit var myView: View
    private lateinit var alertDialog: AlertDialog.Builder

    private var container: ViewGroup? = null

    private fun prepare(context: FragmentActivity, bleDeviceList: MyBleAdapter)
    {
        this.myContext = context
        this.bleDeviceList = bleDeviceList
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
        return super.onCreateDialog(savedInstanceState)
    }

    private fun showDialog(): Dialog {
        // 表示イアログの生成
        if (!::alertDialog.isInitialized) {
            alertDialog = AlertDialog.Builder(myContext)
        }
        if (!::myView.isInitialized) {
            val inflater = myContext.layoutInflater
            myView = inflater.inflate(R.layout.dialog_ble_control, container, false)
        }
        setupDialog()

        alertDialog.setView(myView)
        alertDialog.setCancelable(true)

        prepareSpinner()

        return (alertDialog.create())
    }

    private fun setupDialog()
    {
        // -------- ダイアログの処理メイン
        try
        {
            if (::myView.isInitialized)
            {
                myView.findViewById<Button>(R.id.dialog_ble_button_power_on).setOnClickListener(this)
                myView.findViewById<Button>(R.id.ble_button_close).setOnClickListener(this)
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
            // -----
            if(::bleDeviceList.isInitialized)
            {
                this.bleDeviceList.prepare()
                val deviceList = bleDeviceList.getBondedDeviceList()
                val adapter = ArrayAdapter<String>(this.requireContext(), android.R.layout.simple_spinner_item)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

                for (device in deviceList)
                {
                    adapter.add("${device.name}(${device.id})")
                }
                val spinner: AppCompatSpinner = myView.findViewById(R.id.paired_devices_selection)
                spinner.adapter = adapter
                spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long)
                    {
                        Log.v(TAG, "onItemSelected(parent: $parent, view: $view, pos: $pos, id: $id)")
                    }

                    override fun onNothingSelected(p0: AdapterView<*>?)
                    {
                        Log.v(TAG, "onNothingSelected()")
                    }
                }
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }


    override fun onClick(view: View) {
        when (view.id) {
            R.id.dialog_ble_button_power_on -> {
                vibrate(IVibrator.VibratePattern.SIMPLE_MIDDLE)
            }
            R.id.ble_button_close -> {
                vibrate(IVibrator.VibratePattern.SIMPLE_SHORT)
                dismiss()
            }

            else -> {
                Log.v(TAG, " onClick() : ${view.id}")
            }
        }
    }

    private fun vibrate(vibratePattern: IVibrator.VibratePattern)
    {
        try
        {
            // バイブレータをつかまえる
            val vibrator  = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            {
                val vibratorManager =  myContext.getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibratorManager.defaultVibrator
            }
            else
            {
                @Suppress("DEPRECATION")
                myContext.getSystemService(VIBRATOR_SERVICE) as Vibrator
            }
            if (!vibrator.hasVibrator())
            {
                Log.v(TAG, " not have Vibrator...")
                return
            }
            @Suppress("DEPRECATION") val thread = Thread {
                try
                {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    {
                        vibrator.vibrate(VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE))
                    }
                    else
                    {
                        when (vibratePattern)
                        {
                            IVibrator.VibratePattern.SIMPLE_SHORT_SHORT -> vibrator.vibrate(30)
                            IVibrator.VibratePattern.SIMPLE_SHORT ->  vibrator.vibrate(50)
                            IVibrator.VibratePattern.SIMPLE_MIDDLE -> vibrator.vibrate(100)
                            IVibrator.VibratePattern.SIMPLE_LONG ->  vibrator.vibrate(150)
                            else -> { }
                        }
                    }
                }
                catch (e : Exception)
                {
                    e.printStackTrace()
                }
            }
            thread.start()
        }
        catch (e: java.lang.Exception)
        {
            e.printStackTrace()
        }
    }

    companion object
    {
        val TAG: String = BleControlDialog::class.java.simpleName

        fun newInstance(context: FragmentActivity, bleDeviceList: MyBleAdapter): BleControlDialog {
            val instance = BleControlDialog()
            instance.prepare(context, bleDeviceList)
            return (instance)
        }
    }
}
