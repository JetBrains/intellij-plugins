// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html.psi.impl

import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.impl.source.tree.CompositePsiElement
import org.angular2.lang.html.parser.Angular2HtmlElementTypes
import org.angular2.lang.html.psi.Angular2HtmlBlock
import org.angular2.lang.html.psi.Angular2HtmlElementVisitor

class Angular2HtmlBlockImpl(type: Angular2HtmlElementTypes.Angular2ElementType)
  : CompositePsiElement(type), Angular2HtmlBlock {
  override fun accept(visitor: PsiElementVisitor) {
    when (visitor) {
      is Angular2HtmlElementVisitor -> {
        visitor.visitBlock(this)
      }
      else -> {
        visitor.visitElement(this)
      }
    }
  }
}