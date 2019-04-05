// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.xml;

import com.intellij.openapi.project.Project;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlElement;
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
class DartHtmlElementDescriptor extends DartHtmlDescriptorBase implements XmlElementDescriptor {

  DartHtmlElementDescriptor(@NotNull Project project,
                            @NotNull String name,
                            @NotNull DartServerData.DartNavigationTarget target) {
    super(project, name, target);
  }

  @Override
  public String getDefaultName() {
    return getName();
  }

  @Override
  public String getQualifiedName() {
    return getName();
  }

  @Nullable
  @Override
  public XmlAttributeDescriptor getAttributeDescriptor(@NotNull XmlAttribute attribute) {
    XmlElement nameElement = attribute.getNameElement();
    if (nameElement == null) return null;

    String attrName = nameElement.getText();
    int startOffset = nameElement.getTextRange().getStartOffset();
    int endOffset = nameElement.getTextRange().getEndOffset();
    if (attrName.startsWith("*") || attrName.startsWith("[") || attrName.startsWith("(")) startOffset++;
    if (attrName.endsWith("]") || attrName.endsWith(")")) endOffset--;

    DartServerData.DartNavigationRegion navRegion =
      DartResolver.findRegion(attribute.getContainingFile(), startOffset, endOffset - startOffset);
    if (navRegion == null || navRegion.getTargets().isEmpty()) return null;

    return new DartHtmlAttributeDescriptor(attribute.getProject(), attrName, navRegion.getTargets().get(0));
  }

  @Nullable
  @Override
  public XmlAttributeDescriptor getAttributeDescriptor(String attributeName, @Nullable XmlTag context) {
    return null;
  }

  @Override
  public XmlAttributeDescriptor[] getAttributesDescriptors(@Nullable XmlTag context) {
    return XmlAttributeDescriptor.EMPTY;
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
}
