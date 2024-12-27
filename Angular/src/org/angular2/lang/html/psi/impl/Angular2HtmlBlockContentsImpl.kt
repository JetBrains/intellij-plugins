// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html.psi.impl

import com.intellij.psi.PsiElementVisitor
import org.angular2.lang.html.parser.Angular2HtmlElementTypes
import org.angular2.lang.html.psi.Angular2HtmlBlock
import org.angular2.lang.html.psi.Angular2HtmlBlockContents
import org.angular2.lang.html.psi.Angular2HtmlElementVisitor

internal class Angular2HtmlBlockContentsImpl(type: Angular2HtmlElementTypes.Angular2ElementType)
  : Angular2HtmlCompositePsiElement(type), Angular2HtmlBlockContents {
  override fun accept(visitor: PsiElementVisitor) {
    when (visitor) {
      is Angular2HtmlElementVisitor -> {
        visitor.visitBlockContents(this)
      }
      else -> {
        visitor.visitElement(this)
      }
    }
  }

  override val block: Angular2HtmlBlock
    get() = parent as Angular2HtmlBlock

}