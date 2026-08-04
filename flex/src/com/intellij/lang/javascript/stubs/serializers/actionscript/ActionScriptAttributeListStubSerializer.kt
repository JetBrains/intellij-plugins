package com.intellij.lang.javascript.stubs.serializers.actionscript

import com.intellij.lang.actionscript.ActionScriptElementTypes.ACTIONSCRIPT_ATTRIBUTE_LIST
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList
import com.intellij.lang.javascript.psi.stubs.JSAttributeListStub
import com.intellij.lang.javascript.psi.stubs.impl.ActionScriptAttributeListStubImpl
import com.intellij.lang.javascript.stubs.serializers.JSStubSerializer
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.stubs.StubInputStream

internal class ActionScriptAttributeListStubSerializer : JSStubSerializer<JSAttributeListStub, JSAttributeList>({ ACTIONSCRIPT_ATTRIBUTE_LIST }) {
  override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>?): JSAttributeListStub =
    ActionScriptAttributeListStubImpl(dataStream, parentStub, elementType)
}