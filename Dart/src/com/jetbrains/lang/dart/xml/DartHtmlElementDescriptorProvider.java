// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.xml;

import com.intellij.openapi.util.io.FileUtil;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.source.html.HtmlFileImpl;
import com.intellij.psi.impl.source.xml.XmlElementDescriptorProvider;
import com.intellij.psi.xml.XmlTag;
import com.intellij.psi.xml.XmlToken;
import com.intellij.xml.XmlElementDescriptor;
import com.intellij.xml.util.XmlTagUtil;
import com.jetbrains.lang.dart.analyzer.DartServerData;
import com.jetbrains.lang.dart.resolve.DartResolver;
import org.jetbrains.annotations.Nullable;

public final class DartHtmlElementDescriptorProvider implements XmlElementDescriptorProvider {
  /**
   * Creates {@link DartHtmlElementDescriptor} for DartAngular-specific tags to provide tag name reference resolution and also to make
   * {@link com.intellij.codeInspection.htmlInspections.HtmlUnknownTagInspection} happy.
   * Information from the Dart Analysis Server is used.
   */
  @Nullable
  @Override
  public XmlElementDescriptor getDescriptor(XmlTag tag) {
    PsiFile psiFile = tag.getContainingFile();
    if (!(psiFile instanceof HtmlFileImpl) || psiFile.getContext() != null) return null;

    XmlToken nameToken = XmlTagUtil.getStartTagNameElement(tag);
    if (nameToken == null) return null;

    DartServerData.DartNavigationRegion navRegion =
      DartResolver.findRegion(psiFile, nameToken.getTextRange().getStartOffset(), nameToken.getTextLength());
    if (navRegion == null || navRegion.getTargets().isEmpty()) return null;

    for (DartServerData.DartNavigationTarget target : navRegion.getTargets()) {
      if (FileUtil.toSystemIndependentName(target.getFile()).endsWith("lib/html/dart2js/html_dart2js.dart")) {
        // Standard HTML tags - no need in Dart-specific support
        return null;
      }
    }

    return new DartHtmlElementDescriptor(tag.getProject(), tag.getName(), navRegion.getTargets().get(0));
  }
}
