package jp.osdn.gokigen.aira01c.ui.configuration

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import jp.osdn.gokigen.aira01c.R

class ConfigurationFragment : Fragment()
{
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val root: View = inflater.inflate(R.layout.fragment_configuration, container, false)
        return root
    }



    companion object
    {
        private val TAG = ConfigurationFragment::class.java.simpleName
    }

}