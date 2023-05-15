// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html.stub.impl

import com.intellij.lang.javascript.psi.JSStubElementType
import com.intellij.lang.javascript.psi.JSVariable
import com.intellij.lang.javascript.psi.stubs.JSVariableStub
import com.intellij.lang.javascript.psi.stubs.impl.JSVariableStubBaseImpl
import com.intellij.psi.stubs.IStubElementType
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.stubs.StubInputStream
import org.angular2.lang.html.psi.impl.Angular2HtmlAttrVariableImpl

class Angular2HtmlAttrVariableStubImpl : JSVariableStubBaseImpl<JSVariable>, JSVariableStub<JSVariable> {
  constructor(clazz: JSVariable,
              parent: StubElement<*>?,
              elementType: JSStubElementType<*, JSVariable>) : super(clazz, parent, elementType, 0)

  constructor(dataStream: StubInputStream,
              parentStub: StubElement<*>?,
              elementType: IStubElementType<*, *>) : super(dataStream, parentStub, elementType)

  override fun createPsi(): JSVariable {
    return Angular2HtmlAttrVariableImpl(this)
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