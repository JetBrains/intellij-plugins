// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.stubs.serializers

import com.intellij.lang.javascript.stubs.serializers.JSStubSerializer
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.stubs.StubInputStream
import org.jetbrains.vuejs.lang.expr.parser.VueJSEmbeddedExpressionContentElementType
import org.jetbrains.vuejs.lang.expr.psi.VueJSEmbeddedExpressionContent
import org.jetbrains.vuejs.lang.expr.stub.VueJSEmbeddedExpressionContentStub
import org.jetbrains.vuejs.lang.expr.stub.impl.VueJSEmbeddedExpressionContentStubImpl

internal class VueJSEmbeddedExpressionContentStubSerializer(
  elementTypeSupplier: () -> VueJSEmbeddedExpressionContentElementType,
) : JSStubSerializer<VueJSEmbeddedExpressionContentStub, VueJSEmbeddedExpressionContent>(elementTypeSupplier) {
  override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>?): VueJSEmbeddedExpressionContentStub =
    VueJSEmbeddedExpressionContentStubImpl(parentStub, elementType)
}