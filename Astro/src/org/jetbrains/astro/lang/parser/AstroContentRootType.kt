// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.lang.parser

import com.intellij.embedding.EmbeddingElementType
import com.intellij.lang.ASTNode
import com.intellij.lang.javascript.psi.stubs.JSEmbeddedContentStub
import com.intellij.lang.javascript.psi.stubs.impl.JSEmbeddedContentStubImpl
import com.intellij.lang.javascript.types.JSEmbeddedBlockElementType
import com.intellij.lang.javascript.types.PsiGenerator
import com.intellij.psi.PsiElement
import com.intellij.psi.stubs.*
import org.jetbrains.astro.lang.AstroLanguage
import org.jetbrains.astro.lang.psi.AstroContentRoot

class AstroContentRootType
  : IStubElementType<JSEmbeddedContentStub, AstroContentRoot>
    ("${AstroStubElementTypes.EXTERNAL_ID_PREFIX}CONTENT_ROOT", AstroLanguage.INSTANCE),
    EmbeddingElementType, JSEmbeddedBlockElementType, PsiGenerator<AstroContentRoot> {
  override fun createPsi(stub: JSEmbeddedContentStub): AstroContentRoot =
    AstroContentRoot(stub, this)

  override fun createStub(psi: AstroContentRoot, parentStub: StubElement<out PsiElement>?): JSEmbeddedContentStub =
    JSEmbeddedContentStubImpl(psi, parentStub, this)

  override fun getExternalId(): String =
    debugName

  override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>?): JSEmbeddedContentStub =
    JSEmbeddedContentStubImpl(dataStream, parentStub, this)

  override fun indexStub(stub: JSEmbeddedContentStub, sink: IndexSink) {
    stub.index(sink)
  }

  override fun serialize(stub: JSEmbeddedContentStub, dataStream: StubOutputStream) {
    stub.serialize(dataStream)
  }

  override fun construct(node: ASTNode): AstroContentRoot =
    AstroContentRoot(node)

  override fun isModule(): Boolean =
    true

}