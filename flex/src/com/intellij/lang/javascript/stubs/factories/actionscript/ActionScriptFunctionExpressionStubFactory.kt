package com.intellij.lang.javascript.stubs.factories.actionscript

import com.intellij.lang.actionscript.ActionScriptElementTypes.ACTIONSCRIPT_FUNCTION_EXPRESSION
import com.intellij.lang.actionscript.psi.stubs.impl.ActionScriptFunctionExpressionStubImpl
import com.intellij.lang.javascript.psi.JSFunctionExpression
import com.intellij.lang.javascript.psi.stubs.JSFunctionExpressionStub
import com.intellij.lang.javascript.stubs.factories.JSStubFactory
import com.intellij.psi.PsiElement
import com.intellij.psi.stubs.StubElement

internal class ActionScriptFunctionExpressionStubFactory : JSStubFactory<JSFunctionExpressionStub<JSFunctionExpression>, JSFunctionExpression>({ ACTIONSCRIPT_FUNCTION_EXPRESSION }) {
  override fun createStub(psi: JSFunctionExpression, parentStub: StubElement<out PsiElement>?): JSFunctionExpressionStub<JSFunctionExpression> =
    ActionScriptFunctionExpressionStubImpl(psi, parentStub)
}