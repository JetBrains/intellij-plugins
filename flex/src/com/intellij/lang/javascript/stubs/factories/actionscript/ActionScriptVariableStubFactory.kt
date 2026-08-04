package com.intellij.lang.javascript.stubs.factories.actionscript

import com.intellij.lang.actionscript.ActionScriptElementTypes.ACTIONSCRIPT_VARIABLE
import com.intellij.lang.actionscript.psi.impl.ActionScriptVariableImpl
import com.intellij.lang.actionscript.psi.stubs.impl.ActionScriptVariableStubImpl
import com.intellij.lang.javascript.psi.JSVariable
import com.intellij.lang.javascript.psi.stubs.JSVariableStub
import com.intellij.lang.javascript.stubs.factories.JSVariableStubFactory
import com.intellij.psi.PsiElement
import com.intellij.psi.stubs.StubElement

internal class ActionScriptVariableStubFactory : JSVariableStubFactory({ ACTIONSCRIPT_VARIABLE }) {
  override fun createStub(psi: JSVariable, parentStub: StubElement<out PsiElement>?): JSVariableStub<JSVariable> =
    ActionScriptVariableStubImpl(psi as ActionScriptVariableImpl, parentStub)
}