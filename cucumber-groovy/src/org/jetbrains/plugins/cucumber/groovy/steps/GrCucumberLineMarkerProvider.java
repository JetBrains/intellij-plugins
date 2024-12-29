// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.cucumber.groovy.steps;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import icons.CucumberIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.groovy.GrCucumberUtil;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrMethodCall;

final class GrCucumberLineMarkerProvider implements LineMarkerProvider {
  @Override
  public LineMarkerInfo<?> getLineMarkerInfo(@NotNull PsiElement element) {
    if (GrCucumberUtil.isStepDefinition(element)) {
      PsiElement anchor = PsiTreeUtil.getDeepestFirst(element);

      return new LineMarkerInfo<>(
        anchor,
        anchor.getTextRange(),
        CucumberIcons.Cucumber,
        __ -> ((GrMethodCall)element).getPresentation().getPresentableText(),
        null, GutterIconRenderer.Alignment.RIGHT
      );
    }
    else {
      return null;
    }
  }
}
