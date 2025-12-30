package org.angular2.lang.html

import com.intellij.psi.impl.source.html.HtmlFileImpl
import com.intellij.psi.stubs.PsiFileStub
import com.intellij.psi.tree.IStubFileElementType
import org.angular2.lang.stubs.Angular2HtmlLanguageStubDefinition

abstract class Angular2TemplateFileElementTypeBase(language: Angular2TemplateLanguageBase)
  : IStubFileElementType<PsiFileStub<HtmlFileImpl>>(
  "${if (language.svgDialect) "svg" else "html"}.angular${language.id.filter { it.isDigit() }}",
  language
) {
  override fun getStubVersion(): Int {
    return Angular2HtmlLanguageStubDefinition.angular2HtmlStubVersion
  }
}