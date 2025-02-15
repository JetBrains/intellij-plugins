// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.lang

import com.intellij.psi.stubs.PsiFileStub
import com.intellij.psi.tree.IStubFileElementType
import com.intellij.psi.xml.HtmlFileElementType
import org.jetbrains.astro.lang.parser.AstroStubElementTypes

class AstroFileElementType : IStubFileElementType<PsiFileStub<*>>("astro", AstroLanguage.INSTANCE) {

  override fun getStubVersion(): Int {
    return HtmlFileElementType.getHtmlStubVersion() + 3 + AstroStubElementTypes.STUB_VERSION
  }

  companion object {
    @JvmField
    val INSTANCE = AstroFileElementType()
  }

}