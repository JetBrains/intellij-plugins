// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.cucumber.psi;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.cucumber.CucumberUtil;
import org.jetbrains.plugins.cucumber.OutlineStepSubstitution;
import org.jetbrains.plugins.cucumber.psi.impl.GherkinFileImpl;
import org.jetbrains.plugins.cucumber.steps.AbstractStepDefinition;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Roman.Chernyatchik
 */
public final class GherkinPsiUtil {
  private GherkinPsiUtil() {
  }

  public static @Nullable GherkinFileImpl getGherkinFile(@NotNull PsiElement element) {
    if (!element.isValid()) {
      return null;
    }
    final PsiFile containingFile = element.getContainingFile();
    return containingFile instanceof GherkinFileImpl file ? file : null;
  }

  public static @Nullable List<TextRange> buildParameterRanges(@NotNull GherkinStep step,
                                                               @NotNull AbstractStepDefinition definition,
                                                               int shiftOffset) {

    OutlineStepSubstitution substitution = convertOutlineStepName(step);

    final List<TextRange> parameterRanges = new ArrayList<>();
    final Pattern pattern = definition.getPattern();
    if (pattern == null) return null;

    Matcher matcher = pattern.matcher(substitution.getSubstitution());
    if (matcher.find()) {
      final int groupCount = matcher.groupCount();
      for (int i = 0; i < groupCount; i++) {
        final int start = matcher.start(i + 1);
        final int end = matcher.end(i + 1);
        if (start >= 0 && end >= 0) {
          int rangeStart = substitution.getOffsetInOutlineStep(start);
          int rangeEnd = substitution.getOffsetInOutlineStep(end);
          TextRange range = new TextRange(rangeStart, rangeEnd).shiftRight(shiftOffset);
          if (!parameterRanges.contains(range)) {
            parameterRanges.add(range);
          }
        }
      }
    }

    int k = step.getText().indexOf(step.getName());
    k += step.getName().length();
    if (k < step.getText().length() - 1) {
      String text = step.getText().substring(k + 1);
      boolean inParam = false;
      int paramStart = 0;
      int i = 0;
      while (i < text.length()) {
        if (text.charAt(i) == '<') {
          paramStart = i;
          inParam = true;
        }

        if (text.charAt(i) == '>' && inParam) {
          parameterRanges.add(new TextRange(paramStart, i + 1).shiftRight(shiftOffset + step.getName().length() + 1));
          inParam = false;
        }
        i++;
      }
    }

    return parameterRanges;
  }

  public static OutlineStepSubstitution convertOutlineStepName(@NotNull GherkinStep step) {
    if (!(step.getStepHolder() instanceof GherkinScenarioOutline)) {
      return new OutlineStepSubstitution(step.getName());
    }

    Map<String, String> outlineTableMap = ((GherkinScenarioOutline)step.getStepHolder()).getOutlineTableMap();
    return CucumberUtil.substituteTableReferences(step.getName(), outlineTableMap);
  }
}
