package com.intellij.lang.javascript.intentions;

import com.intellij.lang.javascript.generation.JavaScriptGenerateAccessorHandler;
import com.intellij.lang.javascript.psi.JSFunction;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;

public class CreateGetterAndSetterIntention extends CreateAccessorIntentionBase {

  protected String getDescription() {
    return "Create Getter and Setter";
  }

  protected String getMessageKey() {
    return "intention.create.getter.setter";
  }

  protected boolean isAvailableFor(final JSClass jsClass, final String accessorName) {
    return jsClass.findFunctionByNameAndKind(accessorName, JSFunction.FunctionKind.GETTER) == null
           && jsClass.findFunctionByNameAndKind(accessorName, JSFunction.FunctionKind.SETTER) == null;
  }

  protected JavaScriptGenerateAccessorHandler.GenerationMode getGenerationMode() {
    return JavaScriptGenerateAccessorHandler.GenerationMode.GetterAndSetter;
  }
}
