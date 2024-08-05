// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.javascript.generation;

import org.jetbrains.annotations.NotNull;

/**
 * @author Maxim.Mossienko
 */
public class JavaScriptGenerateEventHandlerAction extends ActionScriptBaseJSGenerateAction {

  @Override
  protected @NotNull BaseJSGenerateHandler getGenerateHandler() {
    return new ActionScriptGenerateEventHandler();
  }
}
