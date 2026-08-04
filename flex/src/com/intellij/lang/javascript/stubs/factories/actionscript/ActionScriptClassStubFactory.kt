package com.intellij.lang.javascript.stubs.factories.actionscript

import com.intellij.lang.actionscript.ActionScriptElementTypes.ACTIONSCRIPT_CLASS
import com.intellij.lang.javascript.psi.ecmal4.JSClass
import com.intellij.lang.javascript.psi.stubs.JSClassStub
import com.intellij.lang.javascript.psi.stubs.impl.ActionScriptClassStubImpl
import com.intellij.lang.javascript.stubs.factories.JSStubFactory
import com.intellij.psi.PsiElement
import com.intellij.psi.stubs.StubElement

internal class ActionScriptClassStubFactory : JSStubFactory<JSClassStub<JSClass>, JSClass>({ ACTIONSCRIPT_CLASS }) {
  override fun createStub(psi: JSClass, parentStub: StubElement<out PsiElement>?): JSClassStub<JSClass> =
    ActionScriptClassStubImpl(psi, parentStub, elementType)
}