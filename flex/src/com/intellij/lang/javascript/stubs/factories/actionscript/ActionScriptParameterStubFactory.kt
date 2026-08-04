package com.intellij.lang.javascript.stubs.factories.actionscript

import com.intellij.lang.actionscript.ActionScriptElementTypes.ACTIONSCRIPT_PARAMETER
import com.intellij.lang.actionscript.psi.impl.ActionScriptParameterImpl
import com.intellij.lang.actionscript.psi.stubs.impl.ActionScriptParameterStubImpl
import com.intellij.lang.javascript.psi.JSParameter
import com.intellij.lang.javascript.psi.stubs.JSParameterStub
import com.intellij.lang.javascript.stubs.factories.JSStubFactory
import com.intellij.psi.PsiElement
import com.intellij.psi.stubs.StubElement

internal class ActionScriptParameterStubFactory : JSStubFactory<JSParameterStub, JSParameter>({ ACTIONSCRIPT_PARAMETER }) {
  override fun createStub(psi: JSParameter, parentStub: StubElement<out PsiElement>?): JSParameterStub =
    ActionScriptParameterStubImpl(psi as ActionScriptParameterImpl, parentStub)
}