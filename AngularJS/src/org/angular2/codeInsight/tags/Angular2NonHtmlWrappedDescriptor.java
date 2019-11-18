// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight.tags;

import com.intellij.html.impl.DelegatingRelaxedHtmlElementDescriptor;
import com.intellij.html.impl.RelaxedHtmlFromSchemaElementDescriptor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.html.dtd.HtmlElementDescriptorImpl;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import com.intellij.xml.XmlAttributeDescriptor;
import com.intellij.xml.XmlElementDescriptor;
import com.intellij.xml.XmlElementsGroup;
import com.intellij.xml.XmlNSDescriptor;
import com.intellij.xml.impl.schema.AnyXmlAttributeDescriptor;
import com.intellij.xml.util.HtmlUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.intellij.html.impl.RelaxedHtmlFromSchemaElementDescriptor.addAttrDescriptorsForFacelets;
import static org.angular2.codeInsight.tags.Angular2StandardTagDescriptor.mergeWithAngularDescriptorIfPossible;

public class Angular2NonHtmlWrappedDescriptor implements XmlElementDescriptor {

  private final XmlElementDescriptor myDelegate;

  public Angular2NonHtmlWrappedDescriptor(@NotNull XmlElementDescriptor delegate) {
    assert !(delegate instanceof HtmlElementDescriptorImpl
             || delegate instanceof Angular2TagDescriptor
             || delegate instanceof Angular2NonHtmlWrappedDescriptor)
      : delegate.getClass().getName();
    myDelegate = delegate;
  }

  @Override
  public String getQualifiedName() {
    return myDelegate.getQualifiedName();
  }

  @Override
  public String getDefaultName() {
    return myDelegate.getDefaultName();
  }

  @Override
  public XmlElementDescriptor[] getElementsDescriptors(XmlTag context) {
    return myDelegate.getElementsDescriptors(context);
  }

  @Nullable
  @Override
  public XmlElementDescriptor getElementDescriptor(XmlTag childTag, XmlTag contextTag) {
    return myDelegate.getElementDescriptor(childTag, contextTag);
  }

  @Override
  public XmlAttributeDescriptor[] getAttributesDescriptors(@Nullable XmlTag context) {
    return addAttrDescriptorsForFacelets(context, myDelegate.getAttributesDescriptors(context));
  }

  @Nullable
  @Override
  public XmlAttributeDescriptor getAttributeDescriptor(String attributeName, @Nullable XmlTag context) {
    XmlAttributeDescriptor descriptor = mergeWithAngularDescriptorIfPossible(myDelegate.getAttributeDescriptor(attributeName, context),
                                                                             attributeName, context);
    if (descriptor == null && attributeName.startsWith(HtmlUtil.HTML5_DATA_ATTR_PREFIX)) {
      return new AnyXmlAttributeDescriptor(attributeName);
    }
    return descriptor;
  }

  @Nullable
  @Override
  public XmlAttributeDescriptor getAttributeDescriptor(XmlAttribute attribute) {
    return getAttributeDescriptor(attribute.getName(), attribute.getParent());
  }

  public XmlAttributeDescriptor[] getDefaultAttributeDescriptors(XmlTag tag) {
    if (!(myDelegate instanceof RelaxedHtmlFromSchemaElementDescriptor
          || myDelegate instanceof DelegatingRelaxedHtmlElementDescriptor)) {
      return myDelegate.getAttributesDescriptors(tag);
    }
    return XmlAttributeDescriptor.EMPTY;
  }

  @Nullable
  @Override
  public XmlNSDescriptor getNSDescriptor() {
    return myDelegate.getNSDescriptor();
  }

  @Nullable
  @Override
  public XmlElementsGroup getTopGroup() {
    return myDelegate.getTopGroup();
  }

  @Override
  public int getContentType() {
    return myDelegate.getContentType();
  }

  @Nullable
  @Override
  public String getDefaultValue() {
    return myDelegate.getDefaultValue();
  }

  @Override
  public PsiElement getDeclaration() {
    return myDelegate.getDeclaration();
  }

  @Override
  public String getName(PsiElement context) {
    return myDelegate.getName(context);
  }

  @Override
  public String getName() {
    return myDelegate.getName();
  }

  @Override
  public void init(PsiElement element) {
    myDelegate.init(element);
  }

  @NotNull
  @Override
  public Object[] getDependencies() {
    return myDelegate.getDependencies();
  }
}
