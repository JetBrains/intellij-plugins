package com.intellij.lang.javascript.stubs.serializers.actionscript

import com.intellij.lang.actionscript.ActionScriptElementTypes.ACTIONSCRIPT_PARAMETER
import com.intellij.lang.actionscript.psi.stubs.impl.ActionScriptParameterStubImpl
import com.intellij.lang.javascript.psi.JSParameter
import com.intellij.lang.javascript.psi.stubs.JSParameterStub
import com.intellij.lang.javascript.stubs.serializers.JSStubSerializer
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.stubs.StubInputStream

internal class ActionScriptParameterStubSerializer : JSStubSerializer<JSParameterStub, JSParameter>({ ACTIONSCRIPT_PARAMETER }) {
  override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>?): JSParameterStub =
    ActionScriptParameterStubImpl(dataStream, parentStub)
}