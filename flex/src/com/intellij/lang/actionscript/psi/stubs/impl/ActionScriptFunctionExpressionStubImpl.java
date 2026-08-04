package com.intellij.lang.actionscript.psi.stubs.impl;

import com.intellij.lang.actionscript.ActionScriptElementTypes;
import com.intellij.lang.actionscript.psi.impl.ActionScriptFunctionExpressionImpl;
import com.intellij.lang.javascript.psi.JSFunctionExpression;
import com.intellij.lang.javascript.psi.stubs.impl.JSFunctionExpressionStubImpl;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;

import java.io.IOException;


public final class ActionScriptFunctionExpressionStubImpl extends JSFunctionExpressionStubImpl {
  public ActionScriptFunctionExpressionStubImpl(JSFunctionExpression function,
                                                StubElement parent) {
    super(function, parent, ActionScriptElementTypes.ACTIONSCRIPT_FUNCTION_EXPRESSION, 0);
  }

  public ActionScriptFunctionExpressionStubImpl(StubInputStream dataStream,
                                                StubElement parentStub) throws IOException {
    super(dataStream, parentStub, ActionScriptElementTypes.ACTIONSCRIPT_FUNCTION_EXPRESSION);
  }

  @Override
  public JSFunctionExpression createPsi() {
    return new ActionScriptFunctionExpressionImpl(this, ActionScriptElementTypes.ACTIONSCRIPT_FUNCTION_EXPRESSION);
  }
}
