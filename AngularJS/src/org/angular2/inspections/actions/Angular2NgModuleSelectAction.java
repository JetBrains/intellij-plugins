// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.inspections.actions;

import com.intellij.lang.javascript.flex.JSQualifiedNamedElementRenderer;
import com.intellij.lang.javascript.modules.ES6ImportAction;
import com.intellij.lang.javascript.modules.JSModuleNameInfo;
import com.intellij.lang.javascript.psi.JSElement;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.util.ObjectUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class Angular2NgModuleSelectAction extends ES6ImportAction {

  private final @NotNull String myActionName;
  protected final boolean myCodeCompletion;

  public Angular2NgModuleSelectAction(@Nullable Editor editor,
                                      @Nullable PsiElement context,
                                      @Nullable String name,
                                      @NotNull ImportElementFilter filter,
                                      @NotNull String actionName,
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
  protected @NotNull String getDebugNameForElement(@NotNull JSElement element,
                                                   @NotNull Map<PsiElement, JSModuleNameInfo> preRenderedQNames) {
    JSModuleNameInfo info = preRenderedQNames.get(element);
    return element.getName()
           + " - " + ObjectUtils.coalesce(info == null ? null : info.getPath(),
                                          JSQualifiedNamedElementRenderer.getContainerText(element));
  }

  @Override
  protected boolean shouldShowPopup(@NotNull List<JSElement> candidates) {
    return myCodeCompletion || super.shouldShowPopup(candidates);
  }
}
