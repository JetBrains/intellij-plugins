package com.jetbrains.lang.dart.ide.marker;

import com.intellij.codeHighlighting.Pass;
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
import com.intellij.util.FunctionUtil;
import com.jetbrains.lang.dart.psi.*;
import com.jetbrains.lang.dart.psi.impl.AbstractDartMethodDeclarationImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

public class DartMethodLineMarkerProvider implements LineMarkerProvider {

  private final DaemonCodeAnalyzerSettings myDaemonSettings;
  private final EditorColorsManager myColorsManager;

  public DartMethodLineMarkerProvider(DaemonCodeAnalyzerSettings daemonSettings, EditorColorsManager colorsManager) {
    myDaemonSettings = daemonSettings;
    myColorsManager = colorsManager;
  }

  @Nullable
  @Override
  public LineMarkerInfo getLineMarkerInfo(@NotNull final PsiElement element) {
    if (myDaemonSettings.SHOW_METHOD_SEPARATORS) {
      if (element instanceof DartMethodDeclaration ||
          element instanceof DartFunctionDeclarationWithBody ||
          element instanceof DartFunctionDeclarationWithBodyOrNative ||
          element instanceof DartGetterDeclaration ||
          element instanceof DartSetterDeclaration ||
          element instanceof DartFactoryConstructorDeclaration ||
          element instanceof AbstractDartMethodDeclarationImpl ||
          element instanceof DartNamedConstructorDeclaration ||
          element instanceof DartIncompleteDeclaration) {

        PsiElement markerLocation = element;
        while (markerLocation.getPrevSibling() != null &&
               (markerLocation.getPrevSibling() instanceof PsiComment ||
                (markerLocation.getPrevSibling() instanceof PsiWhiteSpace &&
                 markerLocation.getPrevSibling().getPrevSibling() != null &&
                 markerLocation.getPrevSibling().getPrevSibling() instanceof PsiComment))) {
          markerLocation = markerLocation.getPrevSibling();
        }

        LineMarkerInfo info = new LineMarkerInfo<PsiElement>(markerLocation, markerLocation.getTextRange(), null, Pass.UPDATE_ALL,
                                                             FunctionUtil.<Object, String>nullConstant(), null,
                                                             GutterIconRenderer.Alignment.RIGHT);
        EditorColorsScheme scheme = myColorsManager.getGlobalScheme();
        info.separatorColor = scheme.getColor(CodeInsightColors.METHOD_SEPARATORS_COLOR);
        info.separatorPlacement = SeparatorPlacement.TOP;
        return info;
      }
    }
    return null;
  }

  @Override
  public void collectSlowLineMarkers(@NotNull List<PsiElement> elements, @NotNull Collection<LineMarkerInfo> result) {
  }
}
