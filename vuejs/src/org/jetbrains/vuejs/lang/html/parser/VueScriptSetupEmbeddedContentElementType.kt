// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.html.parser

import com.intellij.lang.ASTNode
import com.intellij.lang.javascript.JSLanguageDialect
import com.intellij.lang.javascript.psi.JSEmbeddedContent
import com.intellij.lang.javascript.psi.stubs.JSEmbeddedContentStub
import com.intellij.lang.javascript.psi.stubs.TypeScriptScriptContentIndex
import com.intellij.lang.javascript.types.JSEmbeddedContentElementType
import com.intellij.psi.PsiElement
import com.intellij.psi.stubs.IndexSink
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.stubs.StubInputStream
import org.jetbrains.vuejs.lang.expr.VueTSLanguage
import org.jetbrains.vuejs.lang.html.psi.impl.VueScriptSetupEmbeddedContentImpl
import org.jetbrains.vuejs.lang.html.stub.impl.VueScriptSetupEmbeddedContentStubImpl
import java.io.IOException

class VueScriptSetupEmbeddedContentElementType(forcedLanguage: JSLanguageDialect, debugName: String)
  : JSEmbeddedContentElementType(forcedLanguage, debugName) {

  override fun getExternalId(): String =
    "VUE:$debugName"

  override fun indexStub(stub: JSEmbeddedContentStub, sink: IndexSink) {
    super.indexStub(stub, sink)
    if (forcedLanguage == VueTSLanguage.INSTANCE) {
      sink.occurrence(TypeScriptScriptContentIndex.KEY, TypeScriptScriptContentIndex.DEFAULT_INDEX_KEY)
    }
  }

  override fun construct(node: ASTNode): PsiElement {
    return VueScriptSetupEmbeddedContentImpl(node)
  }

  @Throws(IOException::class)
  override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>?): JSEmbeddedContentStub {
    return VueScriptSetupEmbeddedContentStubImpl(dataStream, parentStub, this)
  }

  override fun createStub(psi: JSEmbeddedContent, parentStub: StubElement<*>?): JSEmbeddedContentStub {
    return VueScriptSetupEmbeddedContentStubImpl(psi, parentStub, this)
  }

  override fun isModule(): Boolean =
    true

}