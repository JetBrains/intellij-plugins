package org.angularjs

import com.intellij.psi.xml.XmlTag
import com.intellij.xml.XmlAttributeDescriptor
import com.intellij.xml.XmlAttributeDescriptorsProvider
import com.intellij.xml.impl.schema.AnyXmlAttributeDescriptor
import com.intellij.xml.util.HtmlUtil
import java.util.ArrayList

public open class AngularJSCustomAttributeDescriptorsProvider(): XmlAttributeDescriptorsProvider {
    public override fun getAttributeDescriptors(tag: XmlTag?): Array<XmlAttributeDescriptor>? {
        if (tag == null)
        {
            return XmlAttributeDescriptor.EMPTY
        }

        val descriptors = ArrayList<XmlAttributeDescriptor>()
        var directiveNames = arrayListOf<String?>(
                "click",
                "dblclick",
                "mousedown",
                "mouseup",
                "mouseover",
                "mouseout",
                "mousemove",
                "mouseenter",
                "mouseleave",
                "app",
                "bind",
                "bind-html-unsafe",
                "bind-template",
                "class",
                "class-even",
                "class-odd",
                "cloak",
                "controller",
                "disabled",
                "form",
                "hide",
                "href",
                "include",
                "init",
                "non-bindable",
                "pluralize",
                "repeat",
                "show",
                "submit",
                "style",
                "switch",
                "switch-when",
                "switch-default",
                "options",
                "view",
                "transclude",
                "model",
                "list",
                "change",
                "value",
                "required",
                "checked",
                "csp",
                "multiple",
                "readonly",
                "src"
        )

        directiveNames.forEach {
            val a = AnyXmlAttributeDescriptor("ng-" + it)
            descriptors.add(a)
        }
        val anyXmlAttributeDescriptor = AnyXmlAttributeDescriptor("required")
        descriptors.add(anyXmlAttributeDescriptor)

        return descriptors.toArray(array())
    }
    public override fun getAttributeDescriptor(attributeName: String?, context: XmlTag?): XmlAttributeDescriptor? {
        if (context != null)
        {
            return AnyXmlAttributeDescriptor(attributeName)
        }

        return null
    }
}
