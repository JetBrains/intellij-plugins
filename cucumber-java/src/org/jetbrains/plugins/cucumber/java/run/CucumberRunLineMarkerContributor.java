// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.plugins.cucumber.java.run;

import com.intellij.execution.TestStateStorage;
import com.intellij.execution.lineMarker.RunLineMarkerContributor;
import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.source.tree.LeafElement;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.cucumber.java.CucumberJavaUtil;
import org.jetbrains.plugins.cucumber.psi.GherkinFile;

public abstract class CucumberRunLineMarkerContributor extends RunLineMarkerContributor {

  @Nullable
  @Override
  public Info getInfo(@NotNull PsiElement element) {
    if (!(element instanceof LeafElement)) return null;
    PsiFile psiFile = element.getContainingFile();
    if (!(psiFile instanceof GherkinFile)) return null;
    IElementType type = ((LeafElement)element).getElementType();
   if (!isValidElement(element)) return null;

    InjectedLanguageManager injectedLanguageManager = InjectedLanguageManager.getInstance(element.getProject());
    if (injectedLanguageManager.isInjectedFragment(psiFile)) return null;

    TestStateStorage.Record state = getTestStateStorage(element);
    return getInfo(state);
  }

  private static TestStateStorage.Record getTestStateStorage(PsiElement element) {
    String url = element.getContainingFile().getVirtualFile().getUrl() + ":" + CucumberJavaUtil.getLineNumber(element);
    return TestStateStorage.getInstance(element.getProject()).getState(url);
  }

  private static Info getInfo(TestStateStorage.Record state) {
    return RunLineMarkerContributor.withExecutorActions(getTestStateIcon(state, true));
  }

  protected abstract boolean isValidElement(PsiElement element);
}
