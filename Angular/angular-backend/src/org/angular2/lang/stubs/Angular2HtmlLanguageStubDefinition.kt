package org.angular2.lang.stubs

import com.intellij.psi.StubBuilder
import com.intellij.psi.stubs.DefaultStubBuilder
import com.intellij.psi.stubs.LanguageStubDefinition
import com.intellij.xml.HtmlLanguageStubVersionUtil

class Angular2HtmlLanguageStubDefinition : LanguageStubDefinition {
  override val stubVersion: Int
    get() = angular2HtmlStubVersion

  override val builder: StubBuilder
    get() = DefaultStubBuilder()

  companion object {
    private const val STUB_VERSION: Int = 6
    private const val HTML_STUB_VERSION: Int = 2

    val angular2HtmlStubVersion: Int
      get() = HtmlLanguageStubVersionUtil.getHtmlStubVersion() + STUB_VERSION + HTML_STUB_VERSION
  }
}