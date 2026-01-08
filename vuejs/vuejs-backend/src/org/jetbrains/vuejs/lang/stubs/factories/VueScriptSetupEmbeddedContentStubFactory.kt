// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.stubs.factories

import com.intellij.lang.javascript.psi.JSEmbeddedContent
import com.intellij.lang.javascript.psi.stubs.JSEmbeddedContentStub
import com.intellij.lang.javascript.stubs.factories.JSStubFactory
import com.intellij.psi.PsiElement
import com.intellij.psi.stubs.StubElement
import org.jetbrains.vuejs.lang.html.parser.VueScriptSetupEmbeddedContentElementType
import org.jetbrains.vuejs.lang.html.stub.impl.VueScriptSetupEmbeddedContentStubImpl

internal class VueScriptSetupEmbeddedContentStubFactory(
  elementType: VueScriptSetupEmbeddedContentElementType,
) : JSStubFactory<JSEmbeddedContentStub, JSEmbeddedContent>({ elementType }) {
  override fun createStub(
    psi: JSEmbeddedContent,
    parentStub: StubElement<out PsiElement>?,
  ): JSEmbeddedContentStub =
    VueScriptSetupEmbeddedContentStubImpl(psi, parentStub, elementType)
}