// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.javascript.uml.actions;

import com.intellij.lang.javascript.JavaScriptBundle;
import com.intellij.ui.IconManager;
import org.jetbrains.annotations.NotNull;

/**
 * @author Konstantin Bulenkov
 * @author Kirill Safonov
 */
public class FlexCreateMethodFromDiagramAction extends JSCreateMethodActionBase {
  public FlexCreateMethodFromDiagramAction() {
    super(JavaScriptBundle.messagePointer("new.method.action.text"), JavaScriptBundle.messagePointer("new.method.action.description"),
          IconManager.getInstance().getPlatformIcon(com.intellij.ui.PlatformIcons.Method));
  }

  @Override
  protected boolean isForceConstructor() {
    return false;
  }

  @Override
  public @NotNull String getActionName() {
    return JavaScriptBundle.message("new.method.action.description");
  }
}
