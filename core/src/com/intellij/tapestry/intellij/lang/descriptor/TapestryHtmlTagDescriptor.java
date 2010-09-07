package com.intellij.tapestry.intellij.lang.descriptor;

import com.intellij.psi.PsiElement;
import com.intellij.psi.meta.PsiWritableMetaData;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import com.intellij.tapestry.core.TapestryConstants;
import com.intellij.tapestry.core.model.presentation.Component;
import com.intellij.tapestry.intellij.core.java.IntellijJavaClassType;
import com.intellij.tapestry.intellij.util.TapestryUtils;
import com.intellij.util.ArrayUtil;
import com.intellij.util.IncorrectOperationException;
import com.intellij.xml.XmlAttributeDescriptor;
import com.intellij.xml.XmlElementDescriptor;
import com.intellij.xml.XmlElementsGroup;
import com.intellij.xml.XmlNSDescriptor;
import com.intellij.xml.util.XmlUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Alexey Chmutov
 *         Date: Jul 8, 2009
 *         Time: 9:18:09 PM
 */
public class TapestryHtmlTagDescriptor implements XmlElementDescriptor, PsiWritableMetaData {
  private final XmlElementDescriptor myHtmlDelegate;
  @Nullable
  private final Component myComponent;

  public TapestryHtmlTagDescriptor(@NotNull XmlElementDescriptor htmlDelegate, @Nullable Component component) {
    myHtmlDelegate = htmlDelegate;
    myComponent = component;
  }

  public String getQualifiedName() {
    return myHtmlDelegate.getQualifiedName();
  }

  public String getDefaultName() {
    return myHtmlDelegate.getDefaultName();
  }

  public XmlElementDescriptor[] getElementsDescriptors(XmlTag context) {
    XmlElementDescriptor[] htmlDescriptors = myHtmlDelegate.getElementsDescriptors(context);
    XmlElementDescriptor[] tapestryDescriptors = DescriptorUtil.getTmlSubelementDescriptors(context);
    return ArrayUtil.mergeArrays(htmlDescriptors, tapestryDescriptors, XmlElementDescriptor.class);
  }

  public XmlElementDescriptor getElementDescriptor(XmlTag childTag, XmlTag contextTag) {
    XmlElementDescriptor childDescriptor = myHtmlDelegate.getElementDescriptor(childTag, contextTag);
    if (childDescriptor != null) {
      return childDescriptor;
    }
    if (XmlUtil.XHTML_URI.equals(childTag.getNamespace()) && TapestryUtils.getIdentifyingAttribute(contextTag) != null) {
      return DescriptorUtil.getHtmlTagDescriptorViaNsDescriptor(childTag);
    }
    return DescriptorUtil.getTmlTagDescriptor(childTag);
  }

  public XmlAttributeDescriptor[] getAttributesDescriptors(@Nullable XmlTag context) {
    XmlAttributeDescriptor[] tapestryAttrs =
      context != null ? DescriptorUtil.getAttributeDescriptors(context) : DescriptorUtil.getAttributeDescriptors(myComponent, null);
    return ArrayUtil.mergeArrays(tapestryAttrs, myHtmlDelegate.getAttributesDescriptors(context), XmlAttributeDescriptor.class);
  }

  public XmlAttributeDescriptor getAttributeDescriptor(@NonNls String attributeName, @Nullable XmlTag context) {
    XmlAttributeDescriptor attributeDescriptor = myHtmlDelegate.getAttributeDescriptor(attributeName, context);
    if (attributeDescriptor != null) return attributeDescriptor;
    return context != null
           ? DescriptorUtil.getAttributeDescriptor(attributeName, context)
           : DescriptorUtil.getAttributeDescriptor(attributeName, myComponent);
  }

  public XmlAttributeDescriptor getAttributeDescriptor(XmlAttribute attribute) {
    final String ns = attribute.getNamespace();
    return attribute.getNamespacePrefix().length() == 0 || TapestryConstants.TEMPLATE_NAMESPACE.equals(ns) || XmlUtil.XHTML_URI.equals(ns)
           ? getAttributeDescriptor(attribute.getName(), attribute.getParent())
           : null;
  }

  public XmlNSDescriptor getNSDescriptor() {
    return myHtmlDelegate.getNSDescriptor();
  }

  @Override
  public XmlElementsGroup getTopGroup() {
    return null;
  }

  public int getContentType() {
    return myHtmlDelegate.getContentType();
  }

  public PsiElement getDeclaration() {
    if (myComponent != null) return ((IntellijJavaClassType)myComponent.getElementClass()).getPsiClass();
    return myHtmlDelegate.getDeclaration();
  }

  public String getName(PsiElement context) {
    return myHtmlDelegate.getName(context);
  }

  public String getName() {
    return myHtmlDelegate.getName();
  }

  public void init(PsiElement element) {
  }

  public Object[] getDependences() {
    return ArrayUtil.EMPTY_OBJECT_ARRAY;
  }

  public void setName(String name) throws IncorrectOperationException {
  }
}
