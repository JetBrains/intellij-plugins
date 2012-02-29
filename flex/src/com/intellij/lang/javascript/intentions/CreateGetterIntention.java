package com.intellij.lang.javascript.intentions;

import com.intellij.lang.javascript.generation.JavaScriptGenerateAccessorHandler;
import com.intellij.lang.javascript.psi.JSFunction;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;

public class CreateGetterIntention extends CreateAccessorIntentionBase {

  protected String getDescription() {
    return "Create Getter";
  }

  protected String getMessageKey() {
    return "intention.create.getter";
  }

  protected boolean isAvailableFor(final JSClass jsClass, final String accessorName) {
    return jsClass.findFunctionByNameAndKind(accessorName, JSFunction.FunctionKind.GETTER) == null;
  }

  protected JavaScriptGenerateAccessorHandler.GenerationMode getGenerationMode() {
    return JavaScriptGenerateAccessorHandler.GenerationMode.Getter;
  }
}
