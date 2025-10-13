// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.stubs

import com.intellij.psi.PsiFile
import com.intellij.psi.StubBuilder
import com.intellij.psi.stubs.DefaultStubBuilder
import com.intellij.psi.stubs.LanguageStubDefinition
import com.intellij.psi.stubs.StubElement
import com.intellij.xml.HtmlLanguageStubVersionUtil
import org.jetbrains.vuejs.lang.html.VueFile
import org.jetbrains.vuejs.lang.html.stub.impl.VueFileStubImpl

class VueLanguageStubDefinition : LanguageStubDefinition {
  companion object {
    private const val VUE_STUB_VERSION: Int = 10
    private const val VUEJS_STUB_VERSION: Int = 3
  }

  override val stubVersion: Int
    get() = HtmlLanguageStubVersionUtil.getHtmlStubVersion() + VUE_STUB_VERSION + VUEJS_STUB_VERSION

  override val builder: StubBuilder
    get() = object : DefaultStubBuilder() {
      override fun createStubForFile(file: PsiFile): StubElement<*> {
        return if (file is VueFile) VueFileStubImpl(file) else super.createStubForFile(file)
      }
    }
}