package org.angular2.lang.stubs

import com.intellij.psi.StubBuilder
import com.intellij.psi.stubs.DefaultStubBuilder
import com.intellij.psi.stubs.LanguageStubDefinition
import com.intellij.psi.xml.HtmlLanguageStubDefinition
import org.angular2.lang.expr.parser.Angular2StubElementTypes
import org.angular2.lang.html.stub.Angular2HtmlStubElementTypes

class Angular2HtmlLanguageStubDefinition : LanguageStubDefinition {
  override val stubVersion: Int
    get() = angular2HtmlStubVersion

  override val builder: StubBuilder
    get() = DefaultStubBuilder()

  companion object {
    val angular2HtmlStubVersion: Int
      get() = HtmlLanguageStubDefinition.getHtmlStubVersion() + Angular2StubElementTypes.STUB_VERSION + Angular2HtmlStubElementTypes.STUB_VERSION
  }
}