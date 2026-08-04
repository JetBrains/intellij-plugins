package com.intellij.lang.javascript.stubs.factories.actionscript

import com.intellij.lang.actionscript.ActionScriptElementTypes.INCLUDE_DIRECTIVE
import com.intellij.lang.javascript.psi.ecmal4.JSIncludeDirective
import com.intellij.lang.javascript.psi.stubs.JSIncludeDirectiveStub
import com.intellij.lang.javascript.psi.stubs.impl.JSIncludeDirectiveStubImpl
import com.intellij.lang.javascript.stubs.factories.JSStubFactory
import com.intellij.psi.PsiElement
import com.intellij.psi.stubs.StubElement

internal class JSIncludeDirectiveStubFactory : JSStubFactory<JSIncludeDirectiveStub, JSIncludeDirective>({ INCLUDE_DIRECTIVE }) {
  override fun createStub(psi: JSIncludeDirective, parentStub: StubElement<out PsiElement>?): JSIncludeDirectiveStub =
    JSIncludeDirectiveStubImpl(psi, parentStub, elementType)
}