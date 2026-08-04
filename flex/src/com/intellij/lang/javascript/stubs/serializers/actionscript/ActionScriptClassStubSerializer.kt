package com.intellij.lang.javascript.stubs.serializers.actionscript

import com.intellij.lang.actionscript.ActionScriptElementTypes.ACTIONSCRIPT_CLASS
import com.intellij.lang.javascript.psi.ecmal4.JSClass
import com.intellij.lang.javascript.psi.stubs.JSClassStub
import com.intellij.lang.javascript.psi.stubs.impl.ActionScriptClassStubImpl
import com.intellij.lang.javascript.stubs.serializers.JSStubSerializer
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.stubs.StubInputStream

internal class ActionScriptClassStubSerializer : JSStubSerializer<JSClassStub<JSClass>, JSClass>({ ACTIONSCRIPT_CLASS }) {
  override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>?): JSClassStub<JSClass> =
    ActionScriptClassStubImpl(dataStream, parentStub, elementType)
}