// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight.tags;

import com.intellij.lang.javascript.psi.stubs.JSImplicitElement;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.html.dtd.HtmlNSDescriptorImpl;
import com.intellij.psi.impl.source.xml.XmlDescriptorUtil;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import com.intellij.xml.XmlAttributeDescriptor;
import com.intellij.xml.XmlElementDescriptor;
import com.intellij.xml.XmlElementsGroup;
import com.intellij.xml.XmlNSDescriptor;
import org.angular2.codeInsight.attributes.Angular2AttributeDescriptor;
import org.angular2.codeInsight.attributes.Angular2AttributeDescriptorsProvider;
import org.angular2.entities.Angular2Component;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.intellij.xml.XmlAttributeDescriptor.EMPTY;

public class Angular2TagDescriptor implements XmlElementDescriptor {
  private final String myName;
  private final Angular2Component myComponent;
  private final PsiElement myDeclaration;

  public Angular2TagDescriptor(@NotNull Angular2Component component) {
    myName = component.getSelector();
    myComponent = component;
    myDeclaration = component.getSourceElement();
  }

  public Angular2TagDescriptor(@NotNull JSImplicitElement declaration) {
    myName = declaration.getName();
    myComponent = null;
    myDeclaration = declaration;
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
    return XmlDescriptorUtil.getElementsDescriptors(context);
  }

  @Override
  public XmlElementDescriptor getElementDescriptor(XmlTag childTag, XmlTag contextTag) {
    return XmlDescriptorUtil.getElementDescriptor(childTag, contextTag);
  }

  @Override
  public XmlAttributeDescriptor[] getAttributesDescriptors(@Nullable XmlTag context) {
    final List<XmlAttributeDescriptor> result = new ArrayList<>();
    if (myComponent != null) {
      result.addAll(Angular2AttributeDescriptor.getDirectiveDescriptors(myComponent, false));
    }
    //if (context != null) {
    //  result.addAll(Angular2AttributeDescriptor.getApplicableDirectiveDescriptors(context));
    //  result.addAll(Angular2AttributeDescriptor.getExistingVarsAndRefsDescriptors(context));
    //}
    result.addAll(Arrays.asList(HtmlNSDescriptorImpl.getCommonAttributeDescriptors(context)));
    return result.toArray(EMPTY);
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

  @Override
  @NotNull
  public PsiElement getDeclaration() {
    return myDeclaration;
  }

  public Angular2Component getComponent() {
    return myComponent;
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
