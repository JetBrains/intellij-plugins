package org.angular2.lang.stubs

import com.intellij.psi.PsiElement
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.stubs.StubElementFactory
import org.angular2.lang.html.psi.Angular2HtmlNgContentSelector
import org.angular2.lang.html.stub.Angular2HtmlNgContentSelectorStub
import org.angular2.lang.html.stub.impl.Angular2HtmlNgContentSelectorStubImpl

class Angular2HtmlNgContentSelectorStubFactory : StubElementFactory<Angular2HtmlNgContentSelectorStub, Angular2HtmlNgContentSelector> {
  override fun createStub(psi: Angular2HtmlNgContentSelector, parentStub: StubElement<out PsiElement>?): Angular2HtmlNgContentSelectorStub =
    Angular2HtmlNgContentSelectorStubImpl(psi, parentStub)

  override fun createPsi(stub: Angular2HtmlNgContentSelectorStub): Angular2HtmlNgContentSelector =
    (stub as Angular2HtmlNgContentSelectorStubImpl).createPsi()
}