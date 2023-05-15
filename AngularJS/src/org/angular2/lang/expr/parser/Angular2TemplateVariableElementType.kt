// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.expr.parser

import com.intellij.lang.ASTNode
import com.intellij.lang.javascript.psi.JSVariable
import com.intellij.lang.javascript.psi.stubs.JSVariableStub
import com.intellij.lang.javascript.types.JSVariableElementType
import com.intellij.psi.*
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.stubs.StubInputStream
import org.angular2.lang.expr.psi.impl.Angular2TemplateVariableImpl
import org.angular2.lang.expr.psi.impl.Angular2TemplateVariableStubImpl
import java.io.IOException

internal class Angular2TemplateVariableElementType : JSVariableElementType("TEMPLATE_VARIABLE") {
  override fun getExternalId(): String {
    return Angular2StubElementTypes.EXTERNAL_ID_PREFIX + debugName
  }

  override fun createStub(psi: JSVariable, parentStub: StubElement<*>?): JSVariableStub<in JSVariable> {
    return Angular2TemplateVariableStubImpl(psi, parentStub, this)
  }

  override fun shouldCreateStub(node: ASTNode): Boolean {
    return false
  }

  override fun construct(node: ASTNode): PsiElement {
    return Angular2TemplateVariableImpl(node)
  }

  @Throws(IOException::class)
  override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>?): JSVariableStub<in JSVariable> {
    return Angular2TemplateVariableStubImpl(dataStream, parentStub, this)
  }
}