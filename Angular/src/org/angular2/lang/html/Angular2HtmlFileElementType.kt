// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html

import com.intellij.psi.impl.source.html.HtmlFileImpl
import com.intellij.psi.stubs.PsiFileStub
import com.intellij.psi.tree.IStubFileElementType
import com.intellij.psi.xml.HtmlFileElementType
import org.angular2.lang.expr.parser.Angular2StubElementTypes
import org.angular2.lang.html.stub.Angular2HtmlStubElementTypes

class Angular2HtmlFileElementType private constructor()
  : IStubFileElementType<PsiFileStub<HtmlFileImpl>>("html.angular2", Angular2HtmlLanguage.INSTANCE) {
  override fun getStubVersion(): Int {
    return angular2HtmlStubVersion
  }

  companion object {
    @JvmField
    val INSTANCE: IStubFileElementType<PsiFileStub<HtmlFileImpl>> = Angular2HtmlFileElementType()

    val angular2HtmlStubVersion: Int
      get() = HtmlFileElementType.getHtmlStubVersion() + Angular2StubElementTypes.STUB_VERSION + Angular2HtmlStubElementTypes.STUB_VERSION
  }
}