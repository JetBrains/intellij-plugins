// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight.tags;

import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.html.dtd.HtmlNSDescriptorImpl;
import com.intellij.psi.impl.source.xml.XmlDescriptorUtil;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import com.intellij.xml.XmlAttributeDescriptor;
import com.intellij.xml.XmlElementDescriptor;
import com.intellij.xml.XmlElementsGroup;
import com.intellij.xml.XmlNSDescriptor;
import org.angular2.codeInsight.attributes.Angular2AttributeDescriptorsProvider;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Angular2TagDescriptor implements XmlElementDescriptor {
  private final String myName;
  private final PsiElement myDeclaration;

  public Angular2TagDescriptor(@NotNull String name, @NotNull PsiElement declaration) {
    myName = name;
    myDeclaration = declaration;
  }

  @NotNull
  @Override
  public String getQualifiedName() {
    return myName;
  }

  @NotNull
  @Override
  public String getDefaultName() {
    return myName;
  }

  @NotNull
  @Override
  public XmlElementDescriptor[] getElementsDescriptors(XmlTag context) {
    return XmlDescriptorUtil.getElementsDescriptors(context);
  }

  @Override
  @Nullable
  public XmlElementDescriptor getElementDescriptor(XmlTag childTag, XmlTag contextTag) {
    return XmlDescriptorUtil.getElementDescriptor(childTag, contextTag);
  }

  @Override
  @NotNull
  public XmlAttributeDescriptor[] getAttributesDescriptors(@Nullable XmlTag context) {
    return HtmlNSDescriptorImpl.getCommonAttributeDescriptors(context);
  }

  @Nullable
  @Override
  public XmlAttributeDescriptor getAttributeDescriptor(XmlAttribute attribute) {
    return getAttributeDescriptor(attribute.getName(), attribute.getParent());
  }

  @Nullable
  @Override
  public XmlAttributeDescriptor getAttributeDescriptor(@NonNls final String attributeName, @Nullable XmlTag context) {
    return Angular2AttributeDescriptorsProvider.getAttributeDescriptor(
      attributeName, context, this::getAttributesDescriptors);
  }

  @Nullable
  @Override
  public XmlNSDescriptor getNSDescriptor() {
    return null;
  }

  @Nullable
  @Override
  public XmlElementsGroup getTopGroup() {
    return null;
  }

  @Override
  public int getContentType() {
    return CONTENT_TYPE_ANY;
  }

  @Nullable
  @Override
  public String getDefaultValue() {
    return null;
  }

  @NotNull
  @Override
  public PsiElement getDeclaration() {
    return myDeclaration;
  }

  @NotNull
  @Override
  public String getName(PsiElement context) {
    return getName();
  }

  @NotNull
  @Override
  public String getName() {
    return myName;
  }

  @Override
  public void init(PsiElement element) {
  }
}
