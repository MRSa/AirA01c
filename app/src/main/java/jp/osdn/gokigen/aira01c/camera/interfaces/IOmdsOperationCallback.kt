package jp.osdn.gokigen.aira01c.camera.interfaces

interface IOmdsOperationCallback
{
    fun operationResult(isChange: Boolean, responseText: String)
}