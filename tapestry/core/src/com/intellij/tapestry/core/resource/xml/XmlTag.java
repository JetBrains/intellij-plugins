package com.intellij.tapestry.core.resource.xml;

/**
 * Represents a XML tag.
 */
public interface XmlTag {

    /**
     * @return the name of the tag.
     */
    String getName();

    /**
     * @return the namespace of the tag.
     */
    String getNamespace();

    /**
     * @return the local name of tag (without the namespace).
     */
    String getLocalName();

    /**
     * @return the tag text.
     */
    String getText();

    /**
     * @return all the attributes of the tag.
     */
    XmlAttribute[] getAttributes();

    /**
     * @param name      the local name of the attribute to find.
     * @param namespace the namespace of the attribute to find.
     * @return the attribute with the given name in the given namespace, {@code null} if none is found.
     */
    XmlAttribute getAttribute(String name, String namespace);
}
