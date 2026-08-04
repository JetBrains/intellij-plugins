package com.intellij.lang.javascript.stubs.serializers.actionscript

import com.intellij.lang.actionscript.ActionScriptElementTypes.NAMESPACE_DECLARATION
import com.intellij.lang.javascript.psi.ecmal4.JSNamespaceDeclaration
import com.intellij.lang.javascript.psi.stubs.JSNamespaceDeclarationStub
import com.intellij.lang.javascript.psi.stubs.impl.JSNamespaceDeclarationStubImpl
import com.intellij.lang.javascript.stubs.serializers.JSStubSerializer
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.stubs.StubInputStream

internal class JSNamespaceDeclarationStubSerializer : JSStubSerializer<JSNamespaceDeclarationStub, JSNamespaceDeclaration>({ NAMESPACE_DECLARATION }) {
  override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>?): JSNamespaceDeclarationStub =
    JSNamespaceDeclarationStubImpl(dataStream, parentStub, elementType)
}