package jp.osdn.gokigen.aira01c.camera.utils

import android.content.Context
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment

class ItemSelectionDialog: DialogFragment()
{
    private lateinit var myContext : Context

    private fun prepare(context: Context)
    {
        this.myContext = context
    }

    fun show(iconId: Int, title: String?, key: String, itemArray: List<String?>, callback: ItemSelectedCallback)
    {
        // コンテナ積み替え...
        val itemList = itemArray.toList()
        val items: Array<CharSequence?> = Array(itemList.size) { i -> itemList[i] }

        // 表示イアログの生成
        //val alertDialog = AlertDialog.Builder(myContext, android.R.style.Theme_Material_Dialog)
        //val alertDialog = AlertDialog.Builder(myContext, android.R.style.Theme_Material_Light_Dialog_Alert)
        val alertDialog = AlertDialog.Builder(myContext)
        if (iconId != 0)
        {
            alertDialog.setIcon(iconId)
        }
        alertDialog.setTitle(title)
        alertDialog.setCancelable(true)
        alertDialog.setItems(items) { dialog, which ->
            try
            {
                val selected = itemList[which] ?: ""
                callback.itemSelected(key, selected)
            }
            catch (e: Exception)
            {
                e.printStackTrace()
            }
            dialog.dismiss()
        }

        // アイテム選択ダイアログを表示する
        alertDialog.show()
    }

    // コールバックインタフェース
    interface ItemSelectedCallback
    {
        fun itemSelected(key: String, selectedItem: String)
    }

    companion object
    {
        fun newInstance(context: Context): ItemSelectionDialog
        {
            val instance = ItemSelectionDialog()
            instance.prepare(context)
            return (instance)
        }
    }

}