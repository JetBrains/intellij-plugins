package org.angularjs

import com.intellij.psi.xml.XmlTag
import com.intellij.xml.XmlAttributeDescriptor
import com.intellij.xml.XmlAttributeDescriptorsProvider
import com.intellij.xml.impl.schema.AnyXmlAttributeDescriptor
import java.util.ArrayList
import com.intellij.xml.impl.dom.DomAttributeXmlDescriptor
import com.intellij.util.xml.DomElement
import com.intellij.util.xml.DomManager
import com.intellij.util.xml.reflect.DomAttributeChildDescription
import java.util.HashMap
import kotlin.properties.Delegates

public open class AngularJSCustomAttributeDescriptorsProvider(): XmlAttributeDescriptorsProvider {
    var angularjs:AngularJS? = null

    public override fun getAttributeDescriptors(tag: XmlTag?): Array<XmlAttributeDescriptor>? {
        if(angularjs == null) angularjs = AngularJS.getInstance(tag?.getProject())

        if (tag == null)
        {
            return XmlAttributeDescriptor.EMPTY
        }

        return angularjs?.attrArray
    }
    public override fun getAttributeDescriptor(attributeName: String?, context: XmlTag?): XmlAttributeDescriptor? {
        if (context != null)
        {
            val descriptor = angularjs?.attrLookup?.get(attributeName)
            if(descriptor == null) return null
            return descriptor
        }

        return null
    }


}
