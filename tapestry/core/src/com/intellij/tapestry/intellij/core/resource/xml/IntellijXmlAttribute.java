package com.intellij.tapestry.intellij.core.resource.xml;

import com.intellij.tapestry.core.resource.xml.XmlAttribute;

public class IntellijXmlAttribute implements XmlAttribute {

    private final com.intellij.psi.xml.XmlAttribute _xmlAttribute;

    public IntellijXmlAttribute(com.intellij.psi.xml.XmlAttribute xmlAttribute) {
        _xmlAttribute = xmlAttribute;
    }

    /**
     * {@inheritDoc}
     */
    public String getName() {
        return _xmlAttribute.getName();
    }

    /**
     * {@inheritDoc}
     */
    public String getLocalName() {
        return _xmlAttribute.getLocalName();
    }

    /**
     * {@inheritDoc}
     */
    public String getNamespace() {
        return _xmlAttribute.getNamespace();
    }

    /**
     * {@inheritDoc}
     */
    public String getValue() {
        return _xmlAttribute.getValue();
    }
}
