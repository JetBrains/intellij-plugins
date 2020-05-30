// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
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

/**
 * @author Max Medvedev
 */
public class GrCucumberLineMarkerProvider implements LineMarkerProvider {
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
