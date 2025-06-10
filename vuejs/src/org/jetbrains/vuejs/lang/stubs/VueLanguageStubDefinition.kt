// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.stubs

import com.intellij.psi.PsiFile
import com.intellij.psi.StubBuilder
import com.intellij.psi.stubs.DefaultStubBuilder
import com.intellij.psi.stubs.LanguageStubDefinition
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.xml.HtmlLanguageStubDefinition
import org.jetbrains.vuejs.lang.expr.parser.VueJSStubElementTypes
import org.jetbrains.vuejs.lang.html.VueFile
import org.jetbrains.vuejs.lang.html.parser.VueStubElementTypes
import org.jetbrains.vuejs.lang.html.stub.impl.VueFileStubImpl

class VueLanguageStubDefinition : LanguageStubDefinition {
  override val stubVersion: Int
    get() = HtmlLanguageStubDefinition.getHtmlStubVersion() + VueStubElementTypes.VERSION + VueJSStubElementTypes.STUB_VERSION

  override val builder: StubBuilder
    get() = object : DefaultStubBuilder() {
      override fun createStubForFile(file: PsiFile): StubElement<*> {
        return if (file is VueFile) VueFileStubImpl(file) else super.createStubForFile(file)
      }
    }
}