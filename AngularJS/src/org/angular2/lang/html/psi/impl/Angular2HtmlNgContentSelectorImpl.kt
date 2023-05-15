// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html.psi.impl

import com.intellij.extapi.psi.StubBasedPsiElementBase
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiReference
import com.intellij.psi.StubBasedPsiElement
import com.intellij.psi.impl.source.resolve.reference.ReferenceProvidersRegistry
import com.intellij.psi.stubs.IStubElementType
import org.angular2.entities.Angular2DirectiveSelector
import org.angular2.entities.Angular2DirectiveSelectorImpl
import org.angular2.lang.html.psi.Angular2HtmlElementVisitor
import org.angular2.lang.html.psi.Angular2HtmlNgContentSelector
import org.angular2.lang.html.stub.Angular2HtmlNgContentSelectorStub

class Angular2HtmlNgContentSelectorImpl : StubBasedPsiElementBase<Angular2HtmlNgContentSelectorStub?>,
                                          Angular2HtmlNgContentSelector, StubBasedPsiElement<Angular2HtmlNgContentSelectorStub?> {
  constructor(stub: Angular2HtmlNgContentSelectorStub, nodeType: IStubElementType<*, *>)
    : super(stub, nodeType)

  constructor(node: ASTNode) : super(node)

  override val selector: Angular2DirectiveSelector
    get() {
      val stub = greenStub
      val text: String? = if (stub != null) {
        stub.selector
      }
      else {
        text
      }
      return Angular2DirectiveSelectorImpl(this, text, 0)
    }

  override fun toString(): String {
    return "Angular2HtmlNgContentSelector ($selector)"
  }

  override fun getReference(): PsiReference? {
    return references.firstOrNull()
  }

  override fun getReferences(): Array<PsiReference> {
    return ReferenceProvidersRegistry.getReferencesFromProviders(this)
  }

  override fun accept(visitor: PsiElementVisitor) {
    if (visitor is Angular2HtmlElementVisitor) {
      visitor.visitNgContentSelector(this)
    }
    else {
      super.accept(visitor)
    }
  }
}