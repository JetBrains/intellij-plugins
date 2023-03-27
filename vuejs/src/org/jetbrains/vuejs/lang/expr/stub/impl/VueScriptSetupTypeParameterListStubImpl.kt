// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.expr.stub.impl

import com.intellij.lang.javascript.psi.JSStubElementType
import com.intellij.lang.javascript.psi.ecma6.TypeScriptTypeParameterList
import com.intellij.lang.javascript.psi.ecma6.impl.TypeScriptTypeParameterListImpl
import com.intellij.lang.javascript.psi.stubs.impl.JSStubBase
import com.intellij.lang.typescript.TypeScriptStubElementTypes
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.stubs.StubInputStream
import org.jetbrains.vuejs.lang.expr.stub.VueScriptSetupTypeParameterListStub


class VueScriptSetupTypeParameterListStubImpl : JSStubBase<TypeScriptTypeParameterList>, VueScriptSetupTypeParameterListStub {
  constructor(psi: TypeScriptTypeParameterList?,
              parent: StubElement<*>?,
              elementType: JSStubElementType<*, *>) : super(psi!!, parent, elementType)

  constructor(dataStream: StubInputStream?,
              parent: StubElement<*>?,
              elementType: JSStubElementType<*, *>) : super(dataStream!!, parent, elementType)

  override fun createPsi(): TypeScriptTypeParameterList {
    return TypeScriptTypeParameterListImpl(this, TypeScriptStubElementTypes.TYPE_PARAMETER_LIST)
  }
}

