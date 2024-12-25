package com.jetbrains.plugins.meteor.spacebars;

import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.xml.XmlDocumentImpl;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import com.intellij.xml.XmlAttributeDescriptor;
import com.intellij.xml.XmlElementDescriptor;
import com.intellij.xml.XmlElementsGroup;
import com.intellij.xml.XmlNSDescriptor;
import com.jetbrains.plugins.meteor.spacebars.templates.MeteorTemplateIndex;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;


public class TemplateTagXmlElementDescriptor implements XmlElementDescriptor {

  @Override
  public PsiElement getDeclaration() {
    return myXmlTag;
  }

  protected final String myName;
  protected final XmlTag myXmlTag;

  public TemplateTagXmlElementDescriptor(XmlTag xmlTag) {
    this(xmlTag.getName(), xmlTag);
  }

  public TemplateTagXmlElementDescriptor(String name, XmlTag xmlTag) {
    myName = name;
    myXmlTag = xmlTag;
  }

  @Override
  public String getQualifiedName() {
    return myName;
  }

  @Override
  public String getDefaultName() {
    return myName;
  }

  @Override
  public XmlElementDescriptor[] getElementsDescriptors(XmlTag context) {
    XmlDocumentImpl xmlDocument = PsiTreeUtil.getParentOfType(context, XmlDocumentImpl.class);
    if (xmlDocument == null) return EMPTY_ARRAY;
    XmlNSDescriptor descriptor = xmlDocument.getRootTagNSDescriptor();
    if (descriptor == null) return EMPTY_ARRAY;
    return descriptor.getRootElementsDescriptors(xmlDocument);
  }

  @Override
  public XmlElementDescriptor getElementDescriptor(XmlTag childTag, XmlTag contextTag) {
    XmlTag parent = contextTag.getParentTag();
    if (parent == null) return null;
    final XmlNSDescriptor descriptor = parent.getNSDescriptor(childTag.getNamespace(), true);
    return descriptor == null ? null : descriptor.getElementDescriptor(childTag);
  }

  @Override
  public XmlAttributeDescriptor[] getAttributesDescriptors(@Nullable XmlTag context) {
    return new XmlAttributeDescriptor[]{new MeteorTemplateNameAttributeDescriptor()};
  }

  @Override
  public @Nullable XmlAttributeDescriptor getAttributeDescriptor(XmlAttribute attribute) {
    return getAttributeDescriptor(attribute.getName(), attribute.getParent());
  }

  @Override
  public @Nullable XmlAttributeDescriptor getAttributeDescriptor(final @NonNls String attributeName, @Nullable XmlTag context) {
    return MeteorTemplateIndex.NAME_ATTRIBUTE.equals(attributeName) ? new MeteorTemplateNameAttributeDescriptor() : null;
  }

  @Override
  public XmlNSDescriptor getNSDescriptor() {
    return null;
  }

  @Override
  public @Nullable XmlElementsGroup getTopGroup() {
    return null;
  }

  @Override
  public int getContentType() {
    return CONTENT_TYPE_ANY;
  }

  @Override
  public @Nullable String getDefaultValue() {
    return null;
  }

  @Override
  public String getName(PsiElement context) {
    return getName();
  }

  @Override
  public String getName() {
    return myName;
  }

  @Override
  public void init(PsiElement element) {
  }
}
