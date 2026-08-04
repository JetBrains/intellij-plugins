package com.intellij.lang.javascript.stubs.serializers.actionscript

import com.intellij.lang.actionscript.ActionScriptElementTypes.ACTIONSCRIPT_VARIABLE
import com.intellij.lang.actionscript.psi.stubs.impl.ActionScriptVariableStubImpl
import com.intellij.lang.javascript.psi.JSVariable
import com.intellij.lang.javascript.psi.stubs.JSVariableStub
import com.intellij.lang.javascript.stubs.serializers.JSStubSerializer
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.stubs.StubInputStream

internal class ActionScriptVariableStubSerializer : JSStubSerializer<JSVariableStub<JSVariable>, JSVariable>({ ACTIONSCRIPT_VARIABLE }) {
  override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>?): JSVariableStub<JSVariable> =
    ActionScriptVariableStubImpl(dataStream, parentStub)
}