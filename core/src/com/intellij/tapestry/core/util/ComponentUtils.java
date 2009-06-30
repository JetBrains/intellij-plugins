package com.intellij.tapestry.core.util;

import com.intellij.tapestry.core.TapestryConstants;
import com.intellij.tapestry.core.resource.xml.XmlAttribute;
import com.intellij.tapestry.core.resource.xml.XmlTag;

/**
 * Utility methods related to Tapestry components.
 */  // todo remove it
public class ComponentUtils {

  /**
   * Checks if a tag in a HTML document is a component tag.
   *
   * @param tag the tag to check.
   * @return <code>true</code> if the given tag is a opening or closing tag of a Tapestry component, <code>false</code> otherwise.
   */
  public static boolean _isComponentTag(XmlTag tag) {
    return tag.getNamespace().equals(TapestryConstants.TEMPLATE_NAMESPACE)
           || hasTapestryNamespaceAttribute(tag.getAttributes());
  }

  private static boolean hasTapestryNamespaceAttribute(XmlAttribute[] attributes) {
    for (XmlAttribute attribute : attributes) {
      if (attribute.getLocalName().length() > 0
          && attribute.getNamespace().equals(TapestryConstants.TEMPLATE_NAMESPACE)) return true;
    }
    return false;
  }
}
