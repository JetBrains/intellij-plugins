// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.inspections.actions;

import com.intellij.lang.javascript.modules.imports.JSImportAction;
import com.intellij.lang.javascript.modules.imports.JSImportCandidate;
import com.intellij.lang.javascript.modules.imports.JSImportElementFilter;
import com.intellij.lang.javascript.psi.JSElement;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Angular2NgModuleSelectAction extends JSImportAction {

  private final @NotNull @NlsContexts.Command String myActionName;
  protected final boolean myCodeCompletion;

  public Angular2NgModuleSelectAction(@Nullable Editor editor,
                                      @NotNull PsiElement context,
                                      @NotNull String name,
                                      @NotNull JSImportElementFilter filter,
                                      @NotNull @NlsContexts.Command String actionName,
                                      boolean codeCompletion) {
    super(editor, context, name, filter);
    myActionName = actionName;
    myCodeCompletion = codeCompletion;
  }

  @Override
  public @NotNull String getName() {
    return myActionName;
  }

  @Override
  protected @NotNull String getDebugNameForElement(@NotNull JSImportCandidate element) {
    JSElement psiElement = element.getElement();
    if (psiElement == null) return super.getDebugNameForElement(element);
    String text = element.getContainerText();

    return psiElement.getName() + " - " + text;
  }

  @Override
  protected boolean shouldShowPopup(@NotNull List<? extends JSImportCandidate> candidates) {
    return myCodeCompletion || super.shouldShowPopup(candidates);
  }
}
