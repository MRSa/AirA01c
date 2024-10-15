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
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import jp.osdn.gokigen.aira01c.R
import jp.osdn.gokigen.aira01c.camera.interfaces.IVibrator

class BleControlDialog : DialogFragment(), View.OnClickListener
{
    private lateinit var myContext: FragmentActivity
    private lateinit var myView: View
    private lateinit var alertDialog: AlertDialog.Builder

    private var container: ViewGroup? = null

    private fun prepare(context: FragmentActivity)
    {
        this.myContext = context
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog
    {
        try
        {
            return (showDialog())
        } catch (e: Exception) {
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

        return (alertDialog.create())
    }

    private fun setupDialog() {
        // -------- ダイアログの処理メイン
        try {
            if (::myView.isInitialized) {
                myView.findViewById<Button>(R.id.dialog_ble_button_power_on).setOnClickListener(this)
                myView.findViewById<Button>(R.id.ble_button_close).setOnClickListener(this)
            }
        } catch (e: Exception) {
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

    companion object {
        val TAG: String = BleControlDialog::class.java.simpleName

        fun newInstance(context: FragmentActivity): BleControlDialog {
            val instance = BleControlDialog()
            instance.prepare(context)
            return (instance)
        }
    }
}
