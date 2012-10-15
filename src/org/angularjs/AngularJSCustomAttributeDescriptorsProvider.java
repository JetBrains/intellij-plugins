package org.angularjs;

import com.intellij.psi.xml.XmlTag;
import com.intellij.xml.XmlAttributeDescriptor;
import com.intellij.xml.XmlAttributeDescriptorsProvider;
import com.intellij.xml.impl.schema.AnyXmlAttributeDescriptor;
import com.intellij.xml.util.HtmlUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author John Lindquist
 */
public class AngularJSCustomAttributeDescriptorsProvider implements XmlAttributeDescriptorsProvider {
    @Override
    public XmlAttributeDescriptor[] getAttributeDescriptors(XmlTag tag) {
        if (tag == null) {
            return XmlAttributeDescriptor.EMPTY;
        }
        final List<XmlAttributeDescriptor> result = new ArrayList<XmlAttributeDescriptor>();
        //todo: refactor to loading a file
        //todo: explore making the loaded file a DTD/schema that would also make these "valid" attributes for inspections
        String[] directiveNames = {
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
        };

        for (String directiveName : directiveNames) {
            directiveName = "ng-" + directiveName;
            result.add(new AnyXmlAttributeDescriptor(directiveName));
        }
        result.add(new AnyXmlAttributeDescriptor("required"));

        return result.toArray(new XmlAttributeDescriptor[result.size()]);
    }

    @Override
    public XmlAttributeDescriptor getAttributeDescriptor(String attributeName, XmlTag context) {
        if (context != null) {
            return new AnyXmlAttributeDescriptor(attributeName);
        }
        return null;
    }

}
