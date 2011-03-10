package com.intellij.lang.javascript.generation;

/**
 * @author Maxim.Mossienko
 *         Date: Jul 19, 2008
 *         Time: 1:01:05 AM
 */
public class JavaScriptGenerateGetAccessorAction extends BaseJSGenerateAction {

  protected BaseJSGenerateHandler getGenerateHandler() {
    return new JavaScriptGenerateAccessorHandler(JavaScriptGenerateAccessorHandler.GenerationMode.Getter);
  }
}
