// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.cucumber.steps;


import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiTreeChangeAdapter;
import com.intellij.psi.PsiTreeChangeEvent;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class CucumberPsiTreeListener extends PsiTreeChangeAdapter {

  private final Map<PsiElement, ChangesWatcher> changesWatchersMap;

  public CucumberPsiTreeListener() {
    changesWatchersMap = new HashMap<>();
  }

  public void addChangesWatcher(final PsiElement parent, final ChangesWatcher changesWatcher) {
    changesWatchersMap.put(parent, changesWatcher);
  }

  private void processChange(final PsiElement parent) {
    for (Map.Entry<PsiElement, ChangesWatcher> entry : changesWatchersMap.entrySet()) {
      if (PsiTreeUtil.isAncestor(entry.getKey(), parent, false)) {
        entry.getValue().onChange(parent);
      }
    }
  }

  @Override
  public void childAdded(@NotNull PsiTreeChangeEvent event) {
    processChange(event.getParent());
  }

  @Override
  public void childRemoved(final @NotNull PsiTreeChangeEvent event) {
    processChange(event.getParent());
  }

  @Override
  public void childReplaced(final @NotNull PsiTreeChangeEvent event) {
    processChange(event.getParent());
  }

  @Override
  public void childrenChanged(final @NotNull PsiTreeChangeEvent event) {
    processChange(event.getParent());
  }

  @Override
  public void childMoved(final @NotNull PsiTreeChangeEvent event) {
    processChange(event.getOldParent());
    processChange(event.getNewParent());
  }

  public interface ChangesWatcher {
    void onChange(final PsiElement parentPsiElement);
  }
}
