package jp.osdn.gokigen.aira01c.ui.tips

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class TipsViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is Tips Fragment"
    }
    val text: LiveData<String> = _text
}