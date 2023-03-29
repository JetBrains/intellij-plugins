// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.expr.parser

import com.intellij.lang.ASTNode
import com.intellij.lang.Language
import com.intellij.lang.javascript.psi.JSStubElementType
import com.intellij.psi.PsiElement
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.stubs.StubInputStream
import com.intellij.psi.util.parentOfType
import com.intellij.psi.xml.XmlTag
import org.jetbrains.vuejs.index.isScriptSetupTag
import org.jetbrains.vuejs.lang.expr.psi.VueJSEmbeddedExpressionContent
import org.jetbrains.vuejs.lang.expr.psi.impl.VueJSEmbeddedExpressionContentImpl
import org.jetbrains.vuejs.lang.expr.stub.VueJSEmbeddedExpressionContentStub
import org.jetbrains.vuejs.lang.expr.stub.impl.VueJSEmbeddedExpressionContentStubImpl
import java.io.IOException

class VueJSEmbeddedExpressionContentElementType(debugName: String, private val language: Language)
  : JSStubElementType<VueJSEmbeddedExpressionContentStub, VueJSEmbeddedExpressionContent>(debugName) {

  override fun getLanguage(): Language = language

  override fun createStub(psi: VueJSEmbeddedExpressionContent, parentStub: StubElement<*>?): VueJSEmbeddedExpressionContentStub {
    return VueJSEmbeddedExpressionContentStubImpl(parentStub, this)
  }

  override fun construct(node: ASTNode): PsiElement {
    return VueJSEmbeddedExpressionContentImpl(node)
  }

  @Throws(IOException::class)
  override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>): VueJSEmbeddedExpressionContentStub {
    return VueJSEmbeddedExpressionContentStubImpl(parentStub, this)
  }

  override fun shouldCreateStub(node: ASTNode): Boolean =
    node.psi.parentOfType<XmlTag>()?.isScriptSetupTag() == true

  override fun getExternalId(): String = VueJSStubElementTypes.EXTERNAL_ID_PREFIX + debugName
}
