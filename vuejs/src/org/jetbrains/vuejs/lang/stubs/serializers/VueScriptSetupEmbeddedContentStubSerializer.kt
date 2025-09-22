// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.stubs.serializers

import com.intellij.lang.javascript.psi.JSEmbeddedContent
import com.intellij.lang.javascript.psi.stubs.JSEmbeddedContentStub
import com.intellij.lang.javascript.psi.stubs.TypeScriptScriptContentIndex
import com.intellij.lang.javascript.stubs.serializers.JSStubSerializer
import com.intellij.psi.stubs.IndexSink
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.stubs.StubInputStream
import org.jetbrains.vuejs.lang.expr.VueTSLanguage
import org.jetbrains.vuejs.lang.html.parser.VueScriptSetupEmbeddedContentElementType
import org.jetbrains.vuejs.lang.html.stub.impl.VueScriptSetupEmbeddedContentStubImpl

internal class VueScriptSetupEmbeddedContentStubSerializer(elementType: VueScriptSetupEmbeddedContentElementType)
  : JSStubSerializer<JSEmbeddedContentStub, JSEmbeddedContent>(elementType) {
  override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>?): JSEmbeddedContentStub =
    VueScriptSetupEmbeddedContentStubImpl(dataStream, parentStub, elementType)

  override fun indexStub(stub: JSEmbeddedContentStub, sink: IndexSink) {
    super.indexStub(stub, sink)
    if ((elementType as VueScriptSetupEmbeddedContentElementType).forcedLanguage == VueTSLanguage.INSTANCE) {
      sink.occurrence(TypeScriptScriptContentIndex.KEY, TypeScriptScriptContentIndex.DEFAULT_INDEX_KEY)
    }
  }
}