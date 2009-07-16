package com.intellij.tapestry.intellij.lang.descriptor;

import com.intellij.psi.xml.XmlTag;
import com.intellij.xml.XmlAttributeDescriptor;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Alexey Chmutov
 *         Date: Jun 10, 2009
 *         Time: 3:56:33 PM
 */
public class TapestryUnknownTagDescriptor extends BasicTapestryTagDescriptor {
  private final String myQualifiedName;

  public TapestryUnknownTagDescriptor(@NotNull String componentName, @Nullable String namespacePrefix) {
    super(namespacePrefix);
    myQualifiedName = getPrefix() + componentName.toLowerCase();
  }

  public String getDefaultName() {
    return myQualifiedName;
  }

  public XmlAttributeDescriptor[] getAttributesDescriptors(@Nullable XmlTag context) {
    return context != null ? DescriptorUtil.getAttributeDescriptors(context) : XmlAttributeDescriptor.EMPTY;
  }

  public XmlAttributeDescriptor getAttributeDescriptor(@NonNls String attributeName, @Nullable XmlTag context) {
    return context != null ? DescriptorUtil.getAttributeDescriptor(attributeName, context) : null;
  }
}