package jp.osdn.gokigen.aira01c.camera.utils

import android.content.Context
import android.view.LayoutInflater
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import jp.osdn.gokigen.aira01c.R

class CreditDialog(private val myContext: Context) : DialogFragment()
{
    fun show()
    {
        val alertDialog = AlertDialog.Builder(myContext)
        try
        {
            val inflater = myContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val layout = inflater.inflate(R.layout.creditdialog, null)
            val messageArea = layout.findViewById<TextView>(R.id.creditmessage)
            messageArea.text = myContext.getString(R.string.app_credit)
            alertDialog.setView(layout)
            alertDialog.setIcon(R.drawable.aira01c_icon)
            alertDialog.setTitle(myContext.getString(R.string.app_name))
            alertDialog.setCancelable(true)
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
        val alert = alertDialog.create()
        alert.show()
    }

    companion object
    {
        fun newInstance(context: Context): CreditDialog
        {
            return (CreditDialog(context))
        }
    }
}
