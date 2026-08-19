package com.intellij.mdx.backend.lang

import com.intellij.psi.StubBuilder
import com.intellij.psi.stubs.DefaultStubBuilder
import com.intellij.psi.stubs.LanguageStubDefinition
import com.intellij.psi.tree.TemplateLanguageStubBaseVersion

internal class MdxLanguageStubDefinition : LanguageStubDefinition {
  override val stubVersion: Int
    get() = TemplateLanguageStubBaseVersion.version

  override val builder: StubBuilder
    get() = DefaultStubBuilder()
}
