// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.lang.html

import com.intellij.psi.impl.source.html.HtmlFileImpl
import com.intellij.psi.stubs.PsiFileStub
import com.intellij.psi.tree.IStubFileElementType

class Angular181HtmlFileElementType private constructor()
  : IStubFileElementType<PsiFileStub<HtmlFileImpl>>("html.angular181", Angular181HtmlLanguage) {
  override fun getStubVersion(): Int {
    return Angular2HtmlFileElementType.angular2HtmlStubVersion
  }

  companion object {
    @JvmField
    val INSTANCE: IStubFileElementType<PsiFileStub<HtmlFileImpl>> = Angular181HtmlFileElementType()
  }
}