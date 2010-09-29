package com.intellij.tapestry.core.mocks;

import com.intellij.tapestry.core.resource.xml.XmlAttribute;

/**
 * Description Class
 *
 * @author <a href="mailto:monica.carvalho@logical-software.com">Monica Carvalho</a>
 */

public class XmlAttributeMock implements XmlAttribute {

    private String _name;
    private String _localName;
    private String _namespace;
    private String _value;

    public XmlAttributeMock() {
    }

    public XmlAttributeMock(String localName, String value) {
        _localName = localName;
        _value = value;
    }

    @Override
    public String getName() {
        return _name;
    }

    public XmlAttributeMock setName(String name) {
        _name = name;

         return this;
    }

    @Override
    public String getLocalName() {
        return _localName;
    }

    public XmlAttributeMock setLocalName(String localName) {
        _localName = localName;

         return this;
    }

    @Override
    public String getNamespace() {
        return _namespace;
    }

    public XmlAttributeMock setNamespace(String namespace) {
        _namespace = namespace;

         return this;
    }

    @Override
    public String getValue() {
        return _value;
    }

    public XmlAttributeMock setValue(String value) {
        _value = value;

         return this;
    }
}
