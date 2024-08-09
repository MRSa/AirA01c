package jp.osdn.gokigen.aira01c.ui.configuration

interface IBusyProgressDrawer
{
    fun setCommandFinished(isFinished: Boolean)
    fun controlNextButton(isEnabled: Boolean)
    fun controlPreviousButton(isEnabled: Boolean)
    fun controlCloseButton(isEnabled: Boolean)
    fun setMessageText(message: String, isAppend: Boolean = false)
    fun setResponseText(message: String, isAppend: Boolean = true)
}
