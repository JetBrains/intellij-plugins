package com.intellij.tapestry.core.util;

import com.intellij.tapestry.core.TapestryConstants;
import com.intellij.tapestry.core.resource.xml.XmlAttribute;
import com.intellij.tapestry.core.resource.xml.XmlTag;
import com.intellij.tapestry.intellij.lang.descriptor.TapestryXmlExtension;

/**
 * Utility methods related to Tapestry components.
 */  // todo remove it
public final class ComponentUtils {

  /**
   * Checks if a tag in a HTML document is a component tag.
   *
   * @param tag the tag to check.
   * @return {@code true} if the given tag is a opening or closing tag of a Tapestry component, {@code false} otherwise.
   */
  public static boolean _isComponentTag(XmlTag tag) {
    return TapestryXmlExtension.isTapestryTemplateNamespace(tag.getNamespace())
                                || tag.getNamespace().equals(TapestryConstants.PARAMETERS_NAMESPACE)
                                || hasTapestryNamespaceAttribute(tag.getAttributes());
  }

  private static boolean hasTapestryNamespaceAttribute(XmlAttribute[] attributes) {
    for (XmlAttribute attribute : attributes) {
      final boolean isTapestryNamespace = TapestryXmlExtension.isTapestryTemplateNamespace(attribute.getNamespace()) ||
                        attribute.getNamespace().equals(TapestryConstants.PARAMETERS_NAMESPACE);
      if (attribute.getLocalName().length() > 0 && isTapestryNamespace) return true;
    }
    return false;
  }
}
