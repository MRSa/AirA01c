package jp.osdn.gokigen.aira01c.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HomeViewModel : ViewModel()
{

    private val _myStatusText = MutableLiveData<String>().apply {
        value = ""
    }
    val myStatusText: LiveData<String> = _myStatusText

    private val _text = MutableLiveData<String>().apply {
        value = ""
    }
    val text: LiveData<String> = _text

}