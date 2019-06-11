package com.intellij.tapestry.intellij.lang.descriptor;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.xml.XmlTag;
import com.intellij.xml.XmlAttributeDescriptor;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Alexey Chmutov
 */
public class TapestryUnknownTagDescriptor extends BasicTapestryTagDescriptor {
  private final String myQualifiedName;

  public TapestryUnknownTagDescriptor(@NotNull String componentName,
                                      @Nullable String namespacePrefix,
                                      TapestryNamespaceDescriptor descriptor) {
    super(namespacePrefix, descriptor);
    myQualifiedName = getPrefixWithColon() + StringUtil.toLowerCase(componentName);
  }

  @Override
  public String getDefaultName() {
    return myQualifiedName;
  }

  @Override
  public XmlAttributeDescriptor[] getAttributesDescriptors(@Nullable XmlTag context) {
    return context != null ? DescriptorUtil.getAttributeDescriptors(context) : XmlAttributeDescriptor.EMPTY;
  }

  @Override
  public XmlAttributeDescriptor getAttributeDescriptor(@NonNls String attributeName, @Nullable XmlTag context) {
    return context != null ? DescriptorUtil.getAttributeDescriptor(attributeName, context) : null;
  }
}