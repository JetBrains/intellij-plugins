package com.intellij.tapestry.intellij.core.resource.xml;

import com.intellij.tapestry.core.resource.xml.XmlAttribute;
import com.intellij.tapestry.core.resource.xml.XmlTag;

import java.util.ArrayList;
import java.util.List;

public class IntellijXmlTag implements XmlTag {

    private com.intellij.psi.xml.XmlTag _xmlTag;

    public IntellijXmlTag(com.intellij.psi.xml.XmlTag xmlTag) {
        _xmlTag = xmlTag;
    }

    /**
     * {@inheritDoc}
     */
    public String getName() {
        return _xmlTag.getName();
    }

    /**
     * {@inheritDoc}
     */
    public String getNamespace() {
        return _xmlTag.getNamespace();
    }

    /**
     * {@inheritDoc}
     */
    public String getLocalName() {
        return _xmlTag.getLocalName();
    }

    /**
     * {@inheritDoc}
     */
    public String getText() {
        return _xmlTag.getText();
    }

    /**
     * {@inheritDoc}
     */
    public XmlAttribute[] getAttributes() {
        List<XmlAttribute> attributes = new ArrayList<>();

        for (com.intellij.psi.xml.XmlAttribute attribute : _xmlTag.getAttributes())
            attributes.add(new IntellijXmlAttribute(attribute));

        return attributes.toArray(new XmlAttribute[attributes.size()]);
    }

    public XmlAttribute getAttribute(String name, String namespace) {
        com.intellij.psi.xml.XmlAttribute attribute = _xmlTag.getAttribute(name, namespace);

        if (attribute != null)
            return new IntellijXmlAttribute(attribute);
        else
            return null;
    }
}
