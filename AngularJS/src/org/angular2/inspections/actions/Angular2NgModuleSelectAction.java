// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.inspections.actions;

import com.intellij.lang.javascript.modules.imports.ES6ImportExecutorFactory;
import com.intellij.lang.javascript.modules.imports.JSImportAction;
import com.intellij.lang.javascript.modules.imports.JSImportCandidate;
import com.intellij.lang.javascript.modules.imports.JSImportCandidateWithExecutor;
import com.intellij.lang.javascript.psi.JSElement;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.psi.PsiElement;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Angular2NgModuleSelectAction extends JSImportAction {

  private final @NotNull @NlsContexts.Command String myActionName;
  protected final boolean myCodeCompletion;

  public Angular2NgModuleSelectAction(@Nullable Editor editor,
                                      @NotNull PsiElement context,
                                      @NotNull String name,
                                      @NotNull @NlsContexts.Command String actionName,
                                      boolean codeCompletion) {
    super(editor, context, name);
    myActionName = actionName;
    myCodeCompletion = codeCompletion;
  }

  @Override
  protected @NotNull List<JSImportCandidateWithExecutor> filterAndSort(@NotNull List<? extends JSImportCandidate> candidates,
                                                                       @NotNull PsiElement place) {
    return ContainerUtil.map(candidates, el -> new JSImportCandidateWithExecutor(el, ES6ImportExecutorFactory.FACTORY));
  }

  @Override
  public @NotNull String getName() {
    return myActionName;
  }

  @Override
  protected @NotNull String getDebugNameForElement(@NotNull JSImportCandidateWithExecutor element) {
    JSElement psiElement = element.getElement();
    if (psiElement == null) return super.getDebugNameForElement(element);
    JSImportCandidate candidate = element.getCandidate();
    String text = candidate.getContainerText();

    return psiElement.getName() + " - " + text;
  }

  @Override
  protected boolean shouldShowPopup(@NotNull List<? extends JSImportCandidateWithExecutor> candidates) {
    return myCodeCompletion || super.shouldShowPopup(candidates);
  }
}
