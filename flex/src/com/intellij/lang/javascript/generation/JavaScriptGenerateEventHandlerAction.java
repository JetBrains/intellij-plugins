package com.intellij.lang.javascript.generation;

import org.jetbrains.annotations.NotNull;

/**
 * @author Maxim.Mossienko
 *         Date: Jul 19, 2008
 *         Time: 1:01:05 AM
 */
public class JavaScriptGenerateEventHandlerAction extends ActionScriptBaseJSGenerateAction {

  @NotNull
  protected BaseJSGenerateHandler getGenerateHandler() {
    return new ActionScriptGenerateEventHandler();
  }
}
