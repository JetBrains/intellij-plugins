package com.intellij.lang.javascript.generation;

/**
 * @author Maxim.Mossienko
 *         Date: Jul 19, 2008
 *         Time: 1:01:05 AM
 */
public class JavaScriptGenerateEventHandlerAction extends ActionScriptBaseJSGenerateAction {

  protected BaseJSGenerateHandler getGenerateHandler() {
    return new ActionScriptGenerateEventHandler();
  }
}
