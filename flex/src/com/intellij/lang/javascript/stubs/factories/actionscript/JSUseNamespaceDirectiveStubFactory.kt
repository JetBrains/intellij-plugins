package com.intellij.lang.javascript.stubs.factories.actionscript

import com.intellij.lang.actionscript.ActionScriptElementTypes.USE_NAMESPACE_DIRECTIVE
import com.intellij.lang.javascript.psi.ecmal4.JSUseNamespaceDirective
import com.intellij.lang.javascript.psi.stubs.JSUseNamespaceDirectiveStub
import com.intellij.lang.javascript.psi.stubs.impl.JSUseNamespaceDirectiveStubImpl
import com.intellij.lang.javascript.stubs.factories.JSStubFactory
import com.intellij.psi.PsiElement
import com.intellij.psi.stubs.StubElement

internal class JSUseNamespaceDirectiveStubFactory : JSStubFactory<JSUseNamespaceDirectiveStub, JSUseNamespaceDirective>({ USE_NAMESPACE_DIRECTIVE }) {
  override fun createStub(psi: JSUseNamespaceDirective, parentStub: StubElement<out PsiElement>?): JSUseNamespaceDirectiveStub =
    JSUseNamespaceDirectiveStubImpl(psi, parentStub, elementType)
}