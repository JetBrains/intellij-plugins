package com.intellij.lang.javascript.generation;

import org.jetbrains.annotations.NotNull;

/**
 * @author Maxim.Mossienko
 */
public class JavaScriptGenerateEventHandlerAction extends ActionScriptBaseJSGenerateAction {

  @NotNull
  protected BaseJSGenerateHandler getGenerateHandler() {
    return new ActionScriptGenerateEventHandler();
  }
}
