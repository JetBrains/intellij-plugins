package com.intellij.lang.javascript.stubs.factories.actionscript

import com.intellij.lang.actionscript.ActionScriptElementTypes.NAMESPACE_DECLARATION
import com.intellij.lang.javascript.psi.ecmal4.JSNamespaceDeclaration
import com.intellij.lang.javascript.psi.stubs.JSNamespaceDeclarationStub
import com.intellij.lang.javascript.psi.stubs.impl.JSNamespaceDeclarationStubImpl
import com.intellij.lang.javascript.stubs.factories.JSStubFactory
import com.intellij.psi.PsiElement
import com.intellij.psi.stubs.StubElement

internal class JSNamespaceDeclarationStubFactory : JSStubFactory<JSNamespaceDeclarationStub, JSNamespaceDeclaration>({ NAMESPACE_DECLARATION }) {
  override fun createStub(psi: JSNamespaceDeclaration, parentStub: StubElement<out PsiElement>?): JSNamespaceDeclarationStub =
    JSNamespaceDeclarationStubImpl(psi, parentStub, elementType)
}