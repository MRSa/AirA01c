package jp.osdn.gokigen.aira01c.camera.interfaces

interface IVibrator
{
    enum class VibratePattern
    {
        NONE, SIMPLE_SHORT_SHORT, SIMPLE_SHORT, SIMPLE_MIDDLE, SIMPLE_LONG
    }

    fun vibrate(vibratePattern: VibratePattern)

}