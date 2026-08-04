package com.intellij.lang.javascript.stubs.serializers.actionscript

import com.intellij.lang.actionscript.ActionScriptElementTypes.ACTIONSCRIPT_FUNCTION
import com.intellij.lang.actionscript.psi.impl.ActionScriptFunctionImpl
import com.intellij.lang.actionscript.psi.stubs.impl.ActionScriptFunctionStubImpl
import com.intellij.lang.javascript.stubs.serializers.JSStubSerializer
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.stubs.StubInputStream

internal class ActionScriptFunctionStubSerializer : JSStubSerializer<ActionScriptFunctionStubImpl, ActionScriptFunctionImpl>({ ACTIONSCRIPT_FUNCTION }) {
  override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>?): ActionScriptFunctionStubImpl =
    ActionScriptFunctionStubImpl(dataStream, parentStub)
}