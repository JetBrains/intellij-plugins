// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.lang.jsx.psi

import com.intellij.lang.ASTNode
import com.intellij.lang.javascript.psi.JSEmbeddedContent
import com.intellij.lang.javascript.psi.JSStubElementType
import com.intellij.lang.javascript.psi.impl.JSEmbeddedContentImpl
import com.intellij.lang.javascript.psi.stubs.JSEmbeddedContentStub
import com.intellij.lang.javascript.psi.stubs.impl.JSEmbeddedContentStubImpl
import com.intellij.psi.PsiElement
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.stubs.StubInputStream
import org.jetbrains.astro.lang.jsx.psi.AstroJsxStubElementTypes.Companion.EXTERNAL_ID_PREFIX

class AstroJsxExpressionElementType :
  JSStubElementType<JSEmbeddedContentStub, JSEmbeddedContent>("${EXTERNAL_ID_PREFIX}EXPRESSION") {

  override fun getExternalId(): String {
    return "${EXTERNAL_ID_PREFIX}EXPRESSION"
  }

  override fun createStub(psi: JSEmbeddedContent, parentStub: StubElement<out PsiElement>?): JSEmbeddedContentStub {
    return JSEmbeddedContentStubImpl(psi, parentStub, this)
  }

  override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>?): JSEmbeddedContentStub {
    return JSEmbeddedContentStubImpl(dataStream, parentStub, this)
  }

  override fun construct(node: ASTNode?): PsiElement {
    return JSEmbeddedContentImpl(node)
  }
}