// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.ide.marker;

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzerSettings;
import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.openapi.editor.colors.CodeInsightColors;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.editor.markup.SeparatorPlacement;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.FunctionUtil;
import com.jetbrains.lang.dart.psi.*;
import com.jetbrains.lang.dart.psi.impl.AbstractDartMethodDeclarationImpl;
import org.jetbrains.annotations.NotNull;

public final class DartMethodLineMarkerProvider implements LineMarkerProvider {
  @Override
  public LineMarkerInfo<?> getLineMarkerInfo(final @NotNull PsiElement element) {
    if (!DaemonCodeAnalyzerSettings.getInstance().SHOW_METHOD_SEPARATORS) {
      return null;
    }

    // only continue if element is one of the markable elements (such as methods)
    if (isMarkableElement(element)) {

      // the method line markers are not nestable, aka, methods inside of methods, are not marked
      if (PsiTreeUtil.findFirstParent(element, true, DartMethodLineMarkerProvider::isMarkableElement) != null) {
        return null;
      }

      // move the marker to previous siblings until comments have been included
      PsiElement markerLocation = element;
      while (markerLocation.getPrevSibling() != null &&
             (markerLocation.getPrevSibling() instanceof PsiComment || (markerLocation.getPrevSibling() instanceof PsiWhiteSpace &&
                                                                        markerLocation.getPrevSibling().getPrevSibling() != null &&
                                                                        markerLocation.getPrevSibling()
                                                                          .getPrevSibling() instanceof PsiComment))) {
        markerLocation = markerLocation.getPrevSibling();
      }

      // if the markerLocation element doesn't have a previous sibling (not whitespace), do not mark
      PsiElement prevElement = markerLocation;
      while (prevElement.getPrevSibling() != null && prevElement.getPrevSibling() instanceof PsiWhiteSpace) {
        prevElement = prevElement.getPrevSibling();
      }
      if (prevElement.getPrevSibling() == null) {
        return null;
      }

      PsiElement anchor = PsiTreeUtil.getDeepestFirst(markerLocation);
      // finally, create the marker
      LineMarkerInfo info = new LineMarkerInfo<>(anchor, anchor.getTextRange(), null, FunctionUtil.<Object, String>nullConstant(), null,
                                                 GutterIconRenderer.Alignment.RIGHT);
      EditorColorsScheme scheme = EditorColorsManager.getInstance().getGlobalScheme();
      info.separatorColor = scheme.getColor(CodeInsightColors.METHOD_SEPARATORS_COLOR);
      info.separatorPlacement = SeparatorPlacement.TOP;
      return info;
    }
    return null;
  }

  /**
   * Return true if this is such a PsiElement type that is separated by this LineMarkerProvider.
   */
  private static boolean isMarkableElement(@NotNull final PsiElement element) {
    return element instanceof DartMethodDeclaration ||
           element instanceof DartFunctionDeclarationWithBody ||
           element instanceof DartFunctionDeclarationWithBodyOrNative ||
           element instanceof DartGetterDeclaration ||
           element instanceof DartSetterDeclaration ||
           element instanceof DartFactoryConstructorDeclaration ||
           element instanceof AbstractDartMethodDeclarationImpl ||
           element instanceof DartNamedConstructorDeclaration ||
           element instanceof DartIncompleteDeclaration;
  }
}
