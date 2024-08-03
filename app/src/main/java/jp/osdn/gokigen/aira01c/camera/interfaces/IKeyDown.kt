package jp.osdn.gokigen.aira01c.camera.interfaces

import android.view.KeyEvent

interface IKeyDown
{
    fun handleKeyDown(keyCode: Int, event: KeyEvent): Boolean
}
