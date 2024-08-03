package jp.osdn.gokigen.aira01c.camera.utils.communication

import android.util.Log
import android.util.Xml
import org.xmlpull.v1.XmlPullParser
import java.io.StringReader
import java.util.*

class XmlElement private constructor()
{
    //Log.v(TAG, "XmlElement Tag [" + tagName + "]");
    var tagName = ""
        get() =//Log.v(TAG, "XmlElement Tag [" + tagName + "]");
            field
        private set
    private var tagValue: String
    private val childElements: LinkedList<XmlElement> = LinkedList()
    private val attributes: MutableMap<String, String>
    private var parentElement: XmlElement? = null

    val parent: XmlElement?
        get() = parentElement

    //Log.v(TAG, "XmlElement Value [" + tagValue + "]");
    var value: String
        get() =//Log.v(TAG, "XmlElement Value [" + tagValue + "]");
            tagValue
        private set(value)
        {
            tagValue = value
        }

    private fun putChild(childItem: XmlElement)
    {
        childElements.add(childItem)
        childItem.setParent(this)
    }

    fun findChild(name: String): XmlElement
    {
        for (child in childElements)
        {
            if (child.tagName == name)
                return child
        }
        return XmlElement()
    }

    fun findChildren(name: String): List<XmlElement>
    {
        val tagItemList: MutableList<XmlElement> = ArrayList()
        for (child in childElements)
        {
            if (child.tagName == name)
            {
                tagItemList.add(child)
            }
        }
        return tagItemList
    }

    private fun setParent(parent: XmlElement)
    {
        parentElement = parent
    }

    private fun putAttribute(name: String, value: String)
    {
        attributes[name] = value
    }

    fun getAttribute(name: String, defaultValue: String?): String?
    {
        var ret = attributes[name]
        if (ret == null)
        {
            ret = defaultValue
        }
        return ret
    }

    companion object
    {
        private val TAG = XmlElement::class.java.simpleName
        private val NULL_ELEMENT = XmlElement()
        private fun parse(xmlPullParser: XmlPullParser): XmlElement
        {
            var rootElement = NULL_ELEMENT
            try
            {
                var parsingElement: XmlElement? = NULL_ELEMENT
                MAINLOOP@ while (true) {
                    when (xmlPullParser.next())
                    {
                        XmlPullParser.START_DOCUMENT -> Log.v(
                            TAG, "------- START DOCUMENT -----"
                        )
                        XmlPullParser.START_TAG -> {
                            val childItem = XmlElement()
                            childItem.tagName = xmlPullParser.name
                            if (parsingElement === NULL_ELEMENT) {
                                rootElement = childItem
                            } else {
                                parsingElement!!.putChild(childItem)
                            }
                            parsingElement = childItem

                            // Set Attribute
                            var i = 0
                            while (i < xmlPullParser.attributeCount) {
                                parsingElement.putAttribute(
                                    xmlPullParser.getAttributeName(i),
                                    xmlPullParser.getAttributeValue(i)
                                )
                                i++
                            }
                        }
                        XmlPullParser.TEXT -> parsingElement!!.value = xmlPullParser.text
                        XmlPullParser.END_TAG -> parsingElement = parsingElement!!.parent
                        XmlPullParser.END_DOCUMENT -> {
                            Log.v(TAG, "------- END DOCUMENT -------")
                            break@MAINLOOP
                        }
                        else -> break@MAINLOOP
                    }
                }
            }
            catch (e: Exception)
            {
                e.printStackTrace()
                rootElement = NULL_ELEMENT
            }
            return rootElement
        }

        fun parse(xmlStr: String): XmlElement
        {
            try
            {
                val xmlPullParser = Xml.newPullParser()
                xmlPullParser.setInput(StringReader(xmlStr))
                return parse(xmlPullParser)
            }
            catch (e: Exception)
            {
                e.printStackTrace()
            }
            return XmlElement()
        }
    }

    init
    {
        //Log.v(TAG, "XmlElement()");
        attributes = HashMap()
        tagValue = ""
    }
}
