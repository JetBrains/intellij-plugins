package org.angularjs

import com.intellij.psi.xml.XmlTag
import com.intellij.xml.XmlAttributeDescriptor
import com.intellij.xml.XmlAttributeDescriptorsProvider
import com.intellij.xml.impl.schema.AnyXmlAttributeDescriptor
import java.util.ArrayList

public open class AngularJSCustomAttributeDescriptorsProvider(): XmlAttributeDescriptorsProvider {
    public override fun getAttributeDescriptors(tag: XmlTag?): Array<XmlAttributeDescriptor>? {
        if (tag == null)
        {
            return XmlAttributeDescriptor.EMPTY
        }

        var directiveNames: Array<String?> = array<String?>("click", "dblclick", "mousedown", "mouseup", "mouseover", "mouseout", "mousemove", "mouseenter", "mouseleave", "app", "bind", "bind-html-unsafe", "bind-template", "class", "class-even", "class-odd", "cloak", "controller", "disabled", "form", "hide", "href", "include", "init", "non-bindable", "pluralize", "repeat", "show", "submit", "style", "switch", "switch-when", "switch-default", "options", "view", "transclude", "model", "list", "change", "value", "required", "checked", "csp", "multiple", "readonly", "src")
        val attrs = directiveNames.map { name -> AnyXmlAttributeDescriptor("ng-" + name):XmlAttributeDescriptor } as ArrayList<XmlAttributeDescriptor>
        attrs.add(AnyXmlAttributeDescriptor("required"))
        return attrs.toArray(array<XmlAttributeDescriptor>())
    }
    public override fun getAttributeDescriptor(attributeName: String?, context: XmlTag?): XmlAttributeDescriptor? {
        if (context != null)
        {
            return AnyXmlAttributeDescriptor(attributeName)
        }

        return null
    }


}
