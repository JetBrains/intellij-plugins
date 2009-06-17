package com.intellij.tapestry.intellij.lang.descriptor;

import com.intellij.psi.impl.source.xml.XmlElementDescriptorProvider;
import com.intellij.psi.xml.XmlTag;
import com.intellij.tapestry.core.model.presentation.Component;
import com.intellij.tapestry.intellij.util.TapestryUtils;
import com.intellij.xml.XmlElementDescriptor;

/**
 * @author Alexey Chmutov
 * Date: Jun 10, 2009
 * Time: 2:10:05 PM
 */
public class TapestryTagDescriptorProvider implements XmlElementDescriptorProvider {
  public XmlElementDescriptor getDescriptor(XmlTag tag) {
    Component component = TapestryUtils.getComponentFromTag(tag);
    return component == null ? null : new TapestryTagDescriptor(component, tag.getNamespacePrefix());
  }
}
