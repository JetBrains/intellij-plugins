package com.intellij.tapestry.intellij.lang.descriptor;

import com.intellij.psi.PsiElement;
import com.intellij.psi.meta.PsiWritableMetaData;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import com.intellij.tapestry.core.model.presentation.Component;
import com.intellij.tapestry.intellij.util.TapestryUtils;
import com.intellij.util.IncorrectOperationException;
import com.intellij.xml.XmlAttributeDescriptor;
import com.intellij.xml.XmlElementDescriptor;
import com.intellij.xml.XmlNSDescriptor;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Alexey Chmutov
 *         Date: Jun 10, 2009
 *         Time: 3:56:33 PM
 */
public class TapestryUnknownTagDescriptor implements XmlElementDescriptor, PsiWritableMetaData {
  private final String myQualifiedName;
  private final String myNamespacePrefix;

  public TapestryUnknownTagDescriptor(@NotNull String componentName, @Nullable String namespacePrefix) {
    String name = componentName.toLowerCase();
    myNamespacePrefix = namespacePrefix;
    myQualifiedName = myNamespacePrefix != null && myNamespacePrefix.length() > 0 ? myNamespacePrefix + ":" + name : name;
  }

  public String getQualifiedName() {
    return getDefaultName();
  }

  public String getDefaultName() {
    return myQualifiedName;
  }

  public XmlElementDescriptor[] getElementsDescriptors(XmlTag context) {
    return DescriptorUtil.getElementDescriptors(context);
  }

  public XmlElementDescriptor getElementDescriptor(XmlTag childTag, XmlTag contextTag) {
    Component childComponent = TapestryUtils.getTypeOfTag(childTag);
    if (childComponent == null) return null;
    return new TapestryTagDescriptor(childComponent, myNamespacePrefix);
  }

  public XmlAttributeDescriptor[] getAttributesDescriptors(@Nullable XmlTag context) {
    return context != null ? DescriptorUtil.getAttributeDescriptors(context) : XmlAttributeDescriptor.EMPTY;
  }

  public XmlAttributeDescriptor getAttributeDescriptor(@NonNls String attributeName, @Nullable XmlTag context) {
    return context != null ? DescriptorUtil.getAttributeDescriptor(attributeName, context) : null;
  }

  public XmlAttributeDescriptor getAttributeDescriptor(XmlAttribute attribute) {
    return DescriptorUtil.getAttributeDescriptor(attribute.getLocalName(), attribute.getParent());
  }

  public XmlNSDescriptor getNSDescriptor() {
    return TapestryNamespaceDescriptor.INSTANCE;
  }

  public int getContentType() {
    return CONTENT_TYPE_ANY;
  }

  public PsiElement getDeclaration() {
    return null;
  }

  public String getName(PsiElement context) {
    return getDefaultName();
  }

  public String getName() {
    return getDefaultName();
  }

  public void init(PsiElement element) {
  }

  public Object[] getDependences() {
    return new Object[0];
  }

  public void setName(String name) throws IncorrectOperationException {
    //To change body of implemented methods use File | Settings | File Templates.
  }

}