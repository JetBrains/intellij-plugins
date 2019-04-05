// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.xml;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import com.intellij.xml.XmlAttributeDescriptor;
import com.intellij.xml.XmlElementDescriptor;
import com.intellij.xml.XmlElementsGroup;
import com.intellij.xml.XmlNSDescriptor;
import com.jetbrains.lang.dart.analyzer.DartServerData;
import com.jetbrains.lang.dart.resolve.DartResolver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Descriptor for DartAngular-specific tag that serves for resolving tag name reference and also for making
 * {@link com.intellij.codeInspection.htmlInspections.HtmlUnknownTagInspection} happy.
 * Information from the Dart Analysis Server is used.
 */
class DartHtmlElementDescriptor implements XmlElementDescriptor {
  @NotNull private final Project myProject;
  @NotNull private final String myName;
  @NotNull private final DartServerData.DartNavigationTarget myTarget;

  DartHtmlElementDescriptor(@NotNull Project project,
                            @NotNull String name,
                            @NotNull DartServerData.DartNavigationTarget target) {
    myProject = project;
    myName = name;
    myTarget = target;
  }

  @Nullable
  @Override
  public PsiElement getDeclaration() {
    return DartResolver.getElementForNavigationTarget(myProject, myTarget);
  }

  @NotNull
  @Override
  public String getName() {
    return myName;
  }

  @Override
  public String getName(PsiElement context) {
    return myName;
  }

  @Override
  public String getDefaultName() {
    return myName;
  }

  @Override
  public String getQualifiedName() {
    return myName;
  }

  @Override
  public XmlElementDescriptor[] getElementsDescriptors(XmlTag context) {
    return XmlElementDescriptor.EMPTY_ARRAY;
  }

  @Nullable
  @Override
  public XmlElementDescriptor getElementDescriptor(XmlTag childTag, XmlTag contextTag) {
    return null;
  }

  @Override
  public XmlAttributeDescriptor[] getAttributesDescriptors(@Nullable XmlTag context) {
    return XmlAttributeDescriptor.EMPTY;
  }

  @Nullable
  @Override
  public XmlAttributeDescriptor getAttributeDescriptor(String attributeName, @Nullable XmlTag context) {
    return null;
  }

  @Nullable
  @Override
  public XmlAttributeDescriptor getAttributeDescriptor(XmlAttribute attribute) {
    return null;
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
    return CONTENT_TYPE_UNKNOWN;
  }

  @Nullable
  @Override
  public String getDefaultValue() {
    return null;
  }

  @Override
  public void init(PsiElement element) {}
}
