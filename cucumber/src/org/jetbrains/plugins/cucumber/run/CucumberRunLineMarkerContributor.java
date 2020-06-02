// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.plugins.cucumber.run;

import com.intellij.execution.TestStateStorage;
import com.intellij.execution.lineMarker.RunLineMarkerContributor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.source.tree.LeafElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.intellij.psi.util.PsiUtilCore;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.cucumber.CucumberUtil;
import org.jetbrains.plugins.cucumber.psi.*;

public class CucumberRunLineMarkerContributor extends RunLineMarkerContributor {
  private static final TokenSet RUN_LINE_MARKER_ELEMENTS = TokenSet
    .create(GherkinTokenTypes.FEATURE_KEYWORD, GherkinTokenTypes.SCENARIO_KEYWORD, GherkinTokenTypes.SCENARIO_OUTLINE_KEYWORD,
            GherkinTokenTypes.RULE_KEYWORD, GherkinTokenTypes.EXAMPLE_KEYWORD);

  @Nullable
  @Override
  public Info getInfo(@NotNull PsiElement element) {
    if (!(element instanceof LeafElement)) {
      return null;
    }
    PsiFile psiFile = element.getContainingFile();
    if (!(psiFile instanceof GherkinFile)) {
      return null;
    }
    IElementType type = PsiUtilCore.getElementType(element);
    if (!RUN_LINE_MARKER_ELEMENTS.contains(type)) {
      return null;
    }

    TestStateStorage.Record state = getTestStateStorage(element);
    return getInfo(state);
  }

  private static @Nullable TestStateStorage.Record getTestStateStorage(@NotNull PsiElement element) {
    String url = element.getContainingFile().getVirtualFile().getUrl() + ":" + CucumberUtil.getLineNumber(element);
    return TestStateStorage.getInstance(element.getProject()).getState(url);
  }

  private static @NotNull Info getInfo(@Nullable TestStateStorage.Record state) {
    return RunLineMarkerContributor.withExecutorActions(getTestStateIcon(state, true));
  }
}