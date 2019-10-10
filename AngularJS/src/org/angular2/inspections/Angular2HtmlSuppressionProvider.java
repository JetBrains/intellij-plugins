// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.inspections;

import com.intellij.codeInsight.daemon.impl.analysis.HtmlUnknownTargetInspection;
import com.intellij.codeInspection.DefaultXmlSuppressionProvider;
import com.intellij.codeInspection.InspectionProfileEntry;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.util.containers.ContainerUtil;
import org.angular2.lang.html.Angular2HtmlLanguage;
import org.jetbrains.annotations.NotNull;

import static org.angular2.lang.html.lexer.Angular2HtmlTokenTypes.INTERPOLATION_START;

public class Angular2HtmlSuppressionProvider extends DefaultXmlSuppressionProvider {

  private final String HTML_UNKNOWN_TARGET_INSPECTION_ID = InspectionProfileEntry.getShortName(
    HtmlUnknownTargetInspection.class.getSimpleName());

  @Override
  public boolean isProviderAvailable(@NotNull PsiFile file) {
    return file.getLanguage().isKindOf(Angular2HtmlLanguage.INSTANCE);
  }

  @Override
  public boolean isSuppressedFor(@NotNull PsiElement element, @NotNull String inspectionId) {
    if (HTML_UNKNOWN_TARGET_INSPECTION_ID.equals(inspectionId)
        && element instanceof XmlAttributeValue
        && ContainerUtil.exists(element.getChildren(),
                                el -> el.getNode().getElementType() == INTERPOLATION_START)) {
      return true;
    }
    return super.isSuppressedFor(element, inspectionId);
  }
}
