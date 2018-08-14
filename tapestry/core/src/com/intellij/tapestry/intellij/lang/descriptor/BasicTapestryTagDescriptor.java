package com.intellij.tapestry.intellij.lang.descriptor;

import com.intellij.psi.PsiElement;
import com.intellij.psi.meta.PsiWritableMetaData;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.IncorrectOperationException;
import com.intellij.xml.XmlAttributeDescriptor;
import com.intellij.xml.XmlElementDescriptor;
import com.intellij.xml.XmlElementsGroup;
import com.intellij.xml.XmlNSDescriptor;
import org.jetbrains.annotations.Nullable;

import static com.intellij.tapestry.intellij.lang.descriptor.DescriptorUtil.getTmlOrHtmlTagDescriptor;
import static com.intellij.tapestry.intellij.lang.descriptor.DescriptorUtil.getTmlSubelementDescriptors;

/**
 * @author Alexey Chmutov
 */
public abstract class BasicTapestryTagDescriptor implements XmlElementDescriptor, PsiWritableMetaData {
  private final TapestryNamespaceDescriptor myNamespaceDescriptor;
  private final String myNamespacePrefix;

  public BasicTapestryTagDescriptor(@Nullable String namespacePrefix, TapestryNamespaceDescriptor descriptor) {
    myNamespacePrefix = namespacePrefix;
    myNamespaceDescriptor = descriptor;
  }

  protected final String getPrefixWithColon() {
    return myNamespacePrefix != null && myNamespacePrefix.length() > 0 ? myNamespacePrefix + ":" : "";
  }

  @Override
  public String getQualifiedName() {
    return getDefaultName();
  }

  @Override
  public XmlElementDescriptor[] getElementsDescriptors(XmlTag context) {
    return getTmlSubelementDescriptors(context, myNamespaceDescriptor);
  }

  @Override
  public XmlElementDescriptor getElementDescriptor(XmlTag childTag, XmlTag contextTag) {
    return getTmlOrHtmlTagDescriptor(childTag);
  }

  @Override
  public XmlAttributeDescriptor getAttributeDescriptor(XmlAttribute attribute) {
    String prefix = attribute.getNamespacePrefix();
    return prefix.length() == 0 || prefix.equals(myNamespacePrefix)
           ? getAttributeDescriptor(attribute.getName(), attribute.getParent())
           : null;
  }

  @Override
  public XmlNSDescriptor getNSDescriptor() {
    return myNamespaceDescriptor;
  }

  /**
   * @return minimal occurrence constraint value (e.g. 0 or 1), on null if not applied
   */
  @Override
  public XmlElementsGroup getTopGroup() {
    return null;
  }

  @Override
  public int getContentType() {
    return CONTENT_TYPE_ANY;
  }

  @Override
  public String getDefaultValue() {
    return null;
  }

  @Override
  @Nullable
  public PsiElement getDeclaration() {
    return null;
  }

  @Override
  public String getName(PsiElement context) {
    return getDefaultName();
  }

  @Override
  public String getName() {
    return getDefaultName();
  }

  @Override
  public void init(PsiElement element) {
  }

  @Override
  public void setName(String name) throws IncorrectOperationException {

  }
}
