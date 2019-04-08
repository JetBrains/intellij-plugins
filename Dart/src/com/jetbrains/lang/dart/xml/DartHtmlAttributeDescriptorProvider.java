// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.xml;

import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.source.html.HtmlFileImpl;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlElement;
import com.intellij.psi.xml.XmlTag;
import com.intellij.xml.XmlAttributeDescriptor;
import com.intellij.xml.XmlAttributeDescriptorsProvider;
import com.jetbrains.lang.dart.analyzer.DartServerData;
import com.jetbrains.lang.dart.resolve.DartResolver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Creates {@link DartHtmlAttributeDescriptor} for DartAngular-specific attributes to provide attribute name reference resolution
 * and also to make {@link com.intellij.codeInspection.htmlInspections.HtmlUnknownAttributeInspection} happy.
 * Information from the Dart Analysis Server is used.
 */

public class DartHtmlAttributeDescriptorProvider implements XmlAttributeDescriptorsProvider {
  @Override
  public XmlAttributeDescriptor[] getAttributeDescriptors(XmlTag context) {
    return XmlAttributeDescriptor.EMPTY;
  }

  @Nullable
  @Override
  public XmlAttributeDescriptor getAttributeDescriptor(String attributeName, @NotNull XmlTag tag) {
    PsiFile psiFile = tag.getContainingFile();
    if (!(psiFile instanceof HtmlFileImpl) || psiFile.getContext() != null) return null;

    XmlAttribute attribute = tag.getAttribute(attributeName);
    return attribute != null ? getAttributeDescriptor(attribute) : null;
  }

  @Nullable
  static XmlAttributeDescriptor getAttributeDescriptor(@NotNull XmlAttribute attribute) {
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
}
