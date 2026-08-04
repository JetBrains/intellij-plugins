package com.intellij.lang.javascript.stubs.factories.actionscript

import com.intellij.lang.actionscript.ActionScriptElementTypes.ACTIONSCRIPT_FUNCTION
import com.intellij.lang.actionscript.psi.impl.ActionScriptFunctionImpl
import com.intellij.lang.actionscript.psi.stubs.impl.ActionScriptFunctionStubImpl
import com.intellij.lang.javascript.stubs.factories.JSStubFactory
import com.intellij.psi.PsiElement
import com.intellij.psi.stubs.StubElement

internal class ActionScriptFunctionStubFactory : JSStubFactory<ActionScriptFunctionStubImpl, ActionScriptFunctionImpl>({ ACTIONSCRIPT_FUNCTION }) {
  override fun createStub(psi: ActionScriptFunctionImpl, parentStub: StubElement<out PsiElement>?): ActionScriptFunctionStubImpl =
    ActionScriptFunctionStubImpl(psi, parentStub)
}