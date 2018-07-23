package com.intellij.tapestry.core.resource.xml;

/**
 * A XML tag attribute.
 */
public interface XmlAttribute {

    /**
     * @return the name of the attribute.
     */
    String getName();

    /**
     * @return the localname of the attribute (without the namespace)
     */
    String getLocalName();

    /**
     * @return the attribute namespace.
     */
    String getNamespace();

    /**
     * @return the value of the attribute.
     */
    String getValue();
}
