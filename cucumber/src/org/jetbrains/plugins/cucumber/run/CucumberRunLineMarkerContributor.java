// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.cucumber.run;

import com.intellij.execution.TestStateStorage;
import com.intellij.execution.lineMarker.ExecutorAction;
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
import org.jetbrains.plugins.cucumber.psi.GherkinFile;
import org.jetbrains.plugins.cucumber.psi.GherkinTokenTypes;

public final class CucumberRunLineMarkerContributor extends RunLineMarkerContributor {
  private static final TokenSet RUN_LINE_MARKER_ELEMENTS = TokenSet
    .create(GherkinTokenTypes.FEATURE_KEYWORD, GherkinTokenTypes.SCENARIO_KEYWORD, GherkinTokenTypes.SCENARIO_OUTLINE_KEYWORD,
            GherkinTokenTypes.RULE_KEYWORD, GherkinTokenTypes.EXAMPLE_KEYWORD);

  @Override
  public @Nullable Info getInfo(@NotNull PsiElement element) {
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
    return new Info(getTestStateIcon(state, true), ExecutorAction.getActions(0), RUN_TEST_TOOLTIP_PROVIDER);
  }
}