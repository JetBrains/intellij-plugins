// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.stubs.serializers

import com.intellij.lang.javascript.psi.ecma6.TypeScriptTypeParameterList
import com.intellij.lang.javascript.psi.stubs.TypeScriptTypeParameterListStub
import com.intellij.lang.javascript.stubs.serializers.JSStubSerializer
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.stubs.StubInputStream
import org.jetbrains.vuejs.lang.expr.parser.VueJSStubElementTypes.SCRIPT_SETUP_TYPE_PARAMETER_LIST
import org.jetbrains.vuejs.lang.expr.stub.impl.VueJSScriptSetupTypeParameterListStubImpl

class VueJSScriptSetupTypeParameterListStubSerializer : JSStubSerializer<TypeScriptTypeParameterListStub, TypeScriptTypeParameterList>(SCRIPT_SETUP_TYPE_PARAMETER_LIST) {
  override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>?): TypeScriptTypeParameterListStub =
    VueJSScriptSetupTypeParameterListStubImpl(dataStream, parentStub, elementType)
}