package org.angularjs.codeInsight;

import com.intellij.psi.xml.XmlTag;
import com.intellij.xml.XmlAttributeDescriptor;
import com.intellij.xml.XmlAttributeDescriptorsProvider;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by denofevil on 26/11/13.
 */
public class AngularJSAttributeDescriptorsProvider implements XmlAttributeDescriptorsProvider {
    private static final String[] DIRECTIVE_NAMES = new String[] {
            "animate",
            "app",
            "bind",
            "bind-html-unsafe",
            "bind-template",
            "change",
            "checked",
            "class",
            "class-even",
            "class-odd",
            "click",
            "cloak",
            "controller",
            "csp",
            "dblclick",
            "disabled",
            "false-value",
            "form",
            "hide",
            "href",
            "if",
            "include",
            "init",
            "keypress",
            "list",
            "minlength",
            "maxlength",
            "model",
            "mousedown",
            "mouseup",
            "mouseover",
            "mouseout",
            "mousemove",
            "mouseenter",
            "mouseleave",
            "multiple",
            "non-bindable",
            "options",
            "pattern",
            "pluralize",
            "readonly",
            "repeat",
            "required",
            "selected",
            "show",
            "src",
            "srcset",
            "submit",
            "style",
            "swipe",
            "switch",
            "switch-when",
            "switch-default",
            "transclude",
            "true-value",
            "value",
            "view"
    };
    private static final XmlAttributeDescriptor[] DESCRIPTORS = new XmlAttributeDescriptor[DIRECTIVE_NAMES.length];
    private static final Map<String, XmlAttributeDescriptor> ATTRIBUTE_BY_NAME = new HashMap<String, XmlAttributeDescriptor>();


    static {
        for (int i = 0; i < DIRECTIVE_NAMES.length; i++) {
            final String directiveName = DIRECTIVE_NAMES[i];
            AngularAttributeDescriptor desc = new AngularAttributeDescriptor("ng-" + directiveName);
            DESCRIPTORS[i] = desc;
            ATTRIBUTE_BY_NAME.put(desc.getName(), desc);
        }
    }

    @Override
    public XmlAttributeDescriptor[] getAttributeDescriptors(XmlTag xmlTag) {
        return DESCRIPTORS;
    }

    @Nullable
    @Override
    public XmlAttributeDescriptor getAttributeDescriptor(String attrName, XmlTag xmlTag) {
        return ATTRIBUTE_BY_NAME.get(attrName);
    }
}
