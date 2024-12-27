// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html.psi.impl

import com.intellij.lang.javascript.psi.JSStatement
import com.intellij.psi.PsiElementVisitor
import org.angular2.lang.html.parser.Angular2HtmlElementTypes
import org.angular2.lang.html.psi.Angular2HtmlElementVisitor
import org.angular2.lang.html.psi.Angular2HtmlExpansionForm

internal class Angular2HtmlExpansionFormImpl(type: Angular2HtmlElementTypes.Angular2ElementType)
  : Angular2HtmlCompositePsiElement(type), Angular2HtmlExpansionForm {
  override fun accept(visitor: PsiElementVisitor) {
    when (visitor) {
      is Angular2HtmlElementVisitor -> {
        visitor.visitExpansionForm(this)
      }
      else -> {
        visitor.visitElement(this)
      }
    }
  }

  override val switchValue: JSStatement?
    get() = null
}