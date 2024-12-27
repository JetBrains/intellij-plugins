// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html.psi.impl

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.util.childrenOfType
import org.angular2.lang.expr.psi.Angular2BlockParameter
import org.angular2.lang.html.parser.Angular2HtmlElementTypes
import org.angular2.lang.html.psi.Angular2HtmlBlockParameters
import org.angular2.lang.html.psi.Angular2HtmlElementVisitor

internal class Angular2HtmlBlockParametersImpl(type: Angular2HtmlElementTypes.Angular2ElementType)
  : Angular2HtmlCompositePsiElement(type), Angular2HtmlBlockParameters {
  override fun accept(visitor: PsiElementVisitor) {
    when (visitor) {
      is Angular2HtmlElementVisitor -> {
        visitor.visitBlockParameters(this)
      }
      else -> {
        visitor.visitElement(this)
      }
    }
  }

  override val parameters: List<Angular2BlockParameter>
    get() = childrenOfType<ASTWrapperPsiElement>()
      .flatMap { it.childrenOfType<Angular2BlockParameter>() }

}