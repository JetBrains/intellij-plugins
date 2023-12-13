// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.xml;

import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.source.html.HtmlFileImpl;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlElement;
import com.intellij.psi.xml.XmlTag;
import com.intellij.xml.XmlAttributeDescriptor;
import com.intellij.xml.XmlAttributeDescriptorsProvider;
import com.jetbrains.lang.dart.analyzer.DartAnalysisServerService;
import com.jetbrains.lang.dart.analyzer.DartServerData;
import com.jetbrains.lang.dart.resolve.DartResolver;
import com.jetbrains.lang.dart.util.DartResolveUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Creates {@link DartHtmlAttributeDescriptor} for DartAngular-specific attributes to provide attribute name reference resolution
 * and also to make {@link com.intellij.codeInspection.htmlInspections.HtmlUnknownAttributeInspection} happy.
 * Information from the Dart Analysis Server is used.
 */

public final class DartHtmlAttributeDescriptorProvider implements XmlAttributeDescriptorsProvider {
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

    VirtualFile vFile = DartResolveUtil.getRealVirtualFile(attribute.getContainingFile());
    if (vFile == null) return null;

    List<DartServerData.DartNavigationRegion> regions = DartAnalysisServerService.getInstance(attribute.getProject()).getNavigation(vFile);
    Ref<DartServerData.DartNavigationTarget> targetRef = Ref.create();
    DartResolver.processRegionsInRange(regions, nameElement.getTextRange(), region -> {
      if (!region.getTargets().isEmpty()) {
        targetRef.set(region.getTargets().get(0));
        return false;
      }
      return true;
    });

    if (!targetRef.isNull()) {
      return new DartHtmlAttributeDescriptor(attribute.getProject(), nameElement.getText(), targetRef.get());
    }
    return null;
  }
}
