// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.expr.parser

import com.intellij.lang.ASTNode
import com.intellij.lang.javascript.psi.JSParameter
import com.intellij.lang.javascript.psi.stubs.JSParameterStub
import com.intellij.lang.javascript.types.JSParameterElementType
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.psi.PsiElement
import com.intellij.psi.stubs.StubElement
import org.jetbrains.vuejs.lang.expr.parser.VueJSStubElementTypes.EXTERNAL_ID_PREFIX
import org.jetbrains.vuejs.lang.expr.psi.impl.VueJSSlotPropsParameterImpl

class VueJSSlotPropsParameterElementType : JSParameterElementType("SLOT_PROPS_PARAMETER") {
  override fun construct(node: ASTNode): PsiElement = VueJSSlotPropsParameterImpl(node)

  override fun createPsi(stub: JSParameterStub): JSParameter {
    thisLogger().error("createPsi(stub: JSParameterStub) was called, but shouldCreateStub=false")
    return super.createPsi(stub)
  }

  override fun createStub(psi: JSParameter, parentStub: StubElement<*>?): JSParameterStub {
    thisLogger().error("createStub(psi: JSParameter, parentStub: StubElement<*>?) was called, but shouldCreateStub=false")
    return super.createStub(psi, parentStub)
  }

  override fun shouldCreateStub(node: ASTNode): Boolean = false

  override fun getExternalId(): String = EXTERNAL_ID_PREFIX + debugName
}