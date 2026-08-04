package com.intellij.lang.javascript.stubs.serializers.actionscript

import com.intellij.lang.actionscript.ActionScriptElementTypes.ACTIONSCRIPT_FUNCTION_EXPRESSION
import com.intellij.lang.actionscript.psi.stubs.impl.ActionScriptFunctionExpressionStubImpl
import com.intellij.lang.javascript.psi.JSFunctionExpression
import com.intellij.lang.javascript.psi.stubs.JSFunctionExpressionStub
import com.intellij.lang.javascript.stubs.serializers.JSStubSerializer
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.stubs.StubInputStream

internal class ActionScriptFunctionExpressionStubSerializer : JSStubSerializer<JSFunctionExpressionStub<JSFunctionExpression>, JSFunctionExpression>({ ACTIONSCRIPT_FUNCTION_EXPRESSION }) {
  override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>?): JSFunctionExpressionStub<JSFunctionExpression> =
    ActionScriptFunctionExpressionStubImpl(dataStream, parentStub)
}