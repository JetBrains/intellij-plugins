// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.entities.metadata.psi

import com.intellij.model.Pointer
import com.intellij.openapi.util.NotNullLazyValue
import com.intellij.psi.PsiFile
import com.intellij.psi.impl.source.html.HtmlFileImpl
import com.intellij.refactoring.suggested.createSmartPointer
import org.angular2.entities.Angular2Component
import org.angular2.entities.Angular2DirectiveKind
import org.angular2.entities.Angular2DirectiveSelector
import org.angular2.entities.Angular2DirectiveSelectorImpl
import org.angular2.entities.metadata.stubs.Angular2MetadataComponentStub

class Angular2MetadataComponent(element: Angular2MetadataComponentStub)
  : Angular2MetadataDirectiveBase<Angular2MetadataComponentStub>(element), Angular2Component {

  private val myNgContentSelectors = NotNullLazyValue.lazy {
    stub.ngContentSelectors.map { selector ->
      Angular2DirectiveSelectorImpl(this, selector, null)
    }
  }

  override val templateFile: HtmlFileImpl?
    get() = null

  override val cssFiles: List<PsiFile>
    get() = emptyList()

  override val ngContentSelectors: List<Angular2DirectiveSelector>
    get() = myNgContentSelectors.value

  override val directiveKind: Angular2DirectiveKind
    get() = Angular2DirectiveKind.REGULAR

  override fun createPointer(): Pointer<out Angular2Component> {
    return this.createSmartPointer()
  }
}
