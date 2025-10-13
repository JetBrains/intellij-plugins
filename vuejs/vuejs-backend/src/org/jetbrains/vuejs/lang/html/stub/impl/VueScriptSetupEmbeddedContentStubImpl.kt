// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.html.stub.impl

import com.intellij.lang.javascript.psi.JSEmbeddedContent
import com.intellij.lang.javascript.psi.stubs.impl.JSEmbeddedContentStubImpl
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.stubs.StubInputStream
import com.intellij.psi.tree.IElementType
import org.jetbrains.vuejs.lang.html.psi.impl.VueScriptSetupEmbeddedContentImpl

class VueScriptSetupEmbeddedContentStubImpl : JSEmbeddedContentStubImpl {
  constructor(psi: JSEmbeddedContent, parent: StubElement<*>?, elementType: IElementType)
    : super(psi, parent, elementType)

  constructor(dataStream: StubInputStream, parent: StubElement<*>?, elementType: IElementType)
    : super(dataStream, parent, elementType)

  override fun createPsi(): JSEmbeddedContent =
    VueScriptSetupEmbeddedContentImpl(this, elementType)

}