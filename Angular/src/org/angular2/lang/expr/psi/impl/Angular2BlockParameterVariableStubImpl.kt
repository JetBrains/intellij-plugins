// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.expr.psi.impl

import com.intellij.lang.javascript.psi.JSElementType
import com.intellij.lang.javascript.psi.JSVariable
import com.intellij.lang.javascript.psi.stubs.JSVariableStub
import com.intellij.lang.javascript.psi.stubs.impl.JSVariableStubBaseImpl
import com.intellij.psi.stubs.IStubElementType
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.stubs.StubInputStream

class Angular2BlockParameterVariableStubImpl : JSVariableStubBaseImpl<JSVariable>, JSVariableStub<JSVariable> {
  constructor(clazz: JSVariable,
              parent: StubElement<*>?,
              elementType: JSElementType<JSVariable>)
    : super(clazz, parent, elementType, 0)

  constructor(dataStream: StubInputStream,
              parentStub: StubElement<*>?,
              elementType: IStubElementType<*, *>)
    : super(dataStream, parentStub, elementType)

  override fun createPsi(): JSVariable {
    return Angular2BlockParameterVariableImpl(this)
  }

  override fun doIndexQualifiedName(): Boolean {
    return false
  }

  override fun doIndexForQualifiedNameIndex(): Boolean {
    return false
  }

  override fun doIndexForGlobalQualifiedNameIndex(): Boolean {
    return false
  }
}