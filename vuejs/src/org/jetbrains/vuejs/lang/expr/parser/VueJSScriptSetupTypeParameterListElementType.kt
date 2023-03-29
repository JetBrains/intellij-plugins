// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.expr.parser

import com.intellij.lang.ASTNode
import com.intellij.lang.javascript.psi.JSStubElementType
import com.intellij.lang.javascript.psi.ecma6.TypeScriptTypeParameterList
import com.intellij.lang.javascript.psi.ecma6.impl.TypeScriptTypeParameterListImpl
import com.intellij.psi.PsiElement
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.stubs.StubInputStream
import org.jetbrains.vuejs.lang.expr.parser.VueJSStubElementTypes.EXTERNAL_ID_PREFIX
import org.jetbrains.vuejs.lang.expr.stub.VueJSScriptSetupTypeParameterListStub
import org.jetbrains.vuejs.lang.expr.stub.impl.VueJSScriptSetupTypeParameterListStubImpl
import java.io.IOException

class VueJSScriptSetupTypeParameterListElementType : JSStubElementType<VueJSScriptSetupTypeParameterListStub, TypeScriptTypeParameterList>(
  "SCRIPT_SETUP_TYPE_PARAMETER_LIST") {
  override fun createStub(psi: TypeScriptTypeParameterList, parentStub: StubElement<*>?): VueJSScriptSetupTypeParameterListStub {
    return VueJSScriptSetupTypeParameterListStubImpl(psi, parentStub, this)
  }

  override fun construct(node: ASTNode): PsiElement {
    return TypeScriptTypeParameterListImpl(node)
  }

  @Throws(IOException::class)
  override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>): VueJSScriptSetupTypeParameterListStub {
    return VueJSScriptSetupTypeParameterListStubImpl(dataStream, parentStub, this)
  }

  override fun shouldCreateStub(node: ASTNode): Boolean =
    true

  override fun getExternalId(): String = EXTERNAL_ID_PREFIX + debugName
}
