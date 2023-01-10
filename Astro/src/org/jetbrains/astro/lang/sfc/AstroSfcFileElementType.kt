// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.astro.lang.sfc

import com.intellij.lang.javascript.types.JSFileElementType
import com.intellij.psi.impl.source.html.HtmlFileImpl
import com.intellij.psi.stubs.PsiFileStub
import com.intellij.psi.tree.IStubFileElementType
import com.intellij.psi.xml.HtmlFileElementType
import org.jetbrains.astro.lang.sfc.stub.AstroSfcStubElementTypes

class AstroSfcFileElementType private constructor()
  : IStubFileElementType<PsiFileStub<HtmlFileImpl>>("html.astro-sfc", AstroSfcLanguage.INSTANCE) {

  override fun getStubVersion(): Int {
    return astroSfcStubVersion
  }

  companion object {
    @JvmField
    val INSTANCE: IStubFileElementType<PsiFileStub<HtmlFileImpl>> = AstroSfcFileElementType()

    val astroSfcStubVersion: Int
      get() = JSFileElementType.getVersion(HtmlFileElementType.getHtmlStubVersion() + AstroSfcStubElementTypes.STUB_VERSION)
  }
}