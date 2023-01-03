// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html.stub

import com.intellij.lang.ASTNode
import com.intellij.lang.javascript.psi.JSVariable
import com.intellij.lang.javascript.psi.stubs.JSVariableStub
import com.intellij.lang.javascript.types.JSVariableElementType
import com.intellij.psi.*
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.stubs.StubInputStream
import org.angular2.lang.html.psi.Angular2HtmlAttrVariable
import org.angular2.lang.html.psi.impl.Angular2HtmlAttrVariableImpl
import org.angular2.lang.html.stub.impl.Angular2HtmlAttrVariableStubImpl
import java.io.IOException

class Angular2HtmlVariableElementType(val kind: Angular2HtmlAttrVariable.Kind)
  : JSVariableElementType(kind.name + "_VARIABLE") {

  override fun getExternalId(): String {
    return Angular2HtmlStubElementTypes.EXTERNAL_ID_PREFIX + debugName
  }

  override fun createStub(psi: JSVariable, parentStub: StubElement<*>?): JSVariableStub<JSVariable> {
    return Angular2HtmlAttrVariableStubImpl(psi, parentStub, this)
  }

  override fun shouldCreateStub(node: ASTNode): Boolean {
    return false
  }

  override fun construct(node: ASTNode): PsiElement {
    return Angular2HtmlAttrVariableImpl(node)
  }

  @Throws(IOException::class)
  override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>?): JSVariableStub<JSVariable> {
    return Angular2HtmlAttrVariableStubImpl(dataStream, parentStub, this)
  }
}