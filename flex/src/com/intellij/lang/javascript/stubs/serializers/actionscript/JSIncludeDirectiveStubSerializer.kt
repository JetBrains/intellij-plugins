package com.intellij.lang.javascript.stubs.serializers.actionscript

import com.intellij.lang.actionscript.ActionScriptElementTypes.INCLUDE_DIRECTIVE
import com.intellij.lang.javascript.psi.ecmal4.JSIncludeDirective
import com.intellij.lang.javascript.psi.stubs.JSIncludeDirectiveStub
import com.intellij.lang.javascript.psi.stubs.impl.JSIncludeDirectiveStubImpl
import com.intellij.lang.javascript.stubs.serializers.JSStubSerializer
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.stubs.StubInputStream

internal class JSIncludeDirectiveStubSerializer : JSStubSerializer<JSIncludeDirectiveStub, JSIncludeDirective>({ INCLUDE_DIRECTIVE }) {
  override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>?): JSIncludeDirectiveStub =
    JSIncludeDirectiveStubImpl(dataStream, parentStub, elementType)
}