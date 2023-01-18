// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.lang.psi

import com.intellij.lang.ASTNode
import com.intellij.lang.Language
import com.intellij.lang.javascript.psi.impl.JSEmbeddedContentImpl
import com.intellij.lang.javascript.psi.stubs.JSEmbeddedContentStub
import com.intellij.psi.stubs.IStubElementType
import org.jetbrains.astro.lang.AstroLanguage

class AstroRootContent : JSEmbeddedContentImpl {
  constructor(node: ASTNode) : super(node)

  constructor(stub: JSEmbeddedContentStub, type: IStubElementType<*, *>) : super(stub, type)

  override fun getLanguage(): Language {
    return AstroLanguage.INSTANCE
  }

}