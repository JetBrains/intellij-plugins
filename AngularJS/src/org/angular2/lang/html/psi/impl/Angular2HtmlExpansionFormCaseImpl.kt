// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html.psi.impl

import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.impl.source.tree.CompositePsiElement
import org.angular2.lang.html.parser.Angular2HtmlElementTypes
import org.angular2.lang.html.psi.Angular2HtmlElementVisitor
import org.angular2.lang.html.psi.Angular2HtmlExpansionFormCase

class Angular2HtmlExpansionFormCaseImpl(type: Angular2HtmlElementTypes.Angular2ElementType)
  : CompositePsiElement(type), Angular2HtmlExpansionFormCase {
  override fun accept(visitor: PsiElementVisitor) {
    when (visitor) {
      is Angular2HtmlElementVisitor -> {
        visitor.visitExpansionFormCase(this)
      }
      else -> {
        visitor.visitElement(this)
      }
    }
  }

  override val value: String?
    get() = null
}