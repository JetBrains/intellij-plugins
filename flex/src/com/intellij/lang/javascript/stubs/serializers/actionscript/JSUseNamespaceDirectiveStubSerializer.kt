package com.intellij.lang.javascript.stubs.serializers.actionscript

import com.intellij.lang.actionscript.ActionScriptElementTypes.USE_NAMESPACE_DIRECTIVE
import com.intellij.lang.javascript.psi.ecmal4.JSUseNamespaceDirective
import com.intellij.lang.javascript.psi.stubs.JSUseNamespaceDirectiveStub
import com.intellij.lang.javascript.psi.stubs.impl.JSUseNamespaceDirectiveStubImpl
import com.intellij.lang.javascript.stubs.serializers.JSStubSerializer
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.stubs.StubInputStream

internal class JSUseNamespaceDirectiveStubSerializer : JSStubSerializer<JSUseNamespaceDirectiveStub, JSUseNamespaceDirective>({ USE_NAMESPACE_DIRECTIVE }) {
  override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>?): JSUseNamespaceDirectiveStub =
    JSUseNamespaceDirectiveStubImpl(dataStream, parentStub, elementType)
}