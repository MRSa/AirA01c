package jp.osdn.gokigen.aira01c

import android.util.Log
import androidx.multidex.MultiDexApplication
import jp.osdn.gokigen.aira01c.camera.omds.OmdsCameraControlSingleton

class AppSingleton : MultiDexApplication()
{
    override fun onCreate()
    {
        super.onCreate()
        Log.v(TAG, "AppSingleton::create()")
        cameraControl = OmdsCameraControlSingleton()
        vibrator = MyVibrator()
    }

    companion object
    {
        private val TAG = AppSingleton::class.java.simpleName
        lateinit var cameraControl: OmdsCameraControlSingleton
        lateinit var vibrator: MyVibrator
        var startBleSequence = false
    }
}
