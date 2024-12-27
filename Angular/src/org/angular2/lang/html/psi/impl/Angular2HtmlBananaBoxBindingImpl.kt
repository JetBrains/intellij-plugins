// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html.psi.impl

import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.XmlElementVisitor
import org.angular2.lang.html.parser.Angular2HtmlElementTypes
import org.angular2.lang.html.psi.Angular2HtmlBananaBoxBinding
import org.angular2.lang.html.psi.Angular2HtmlElementVisitor

internal class Angular2HtmlBananaBoxBindingImpl(type: Angular2HtmlElementTypes.Angular2ElementType)
  : Angular2HtmlPropertyBindingBase(type), Angular2HtmlBananaBoxBinding {
  override fun accept(visitor: PsiElementVisitor) {
    when (visitor) {
      is Angular2HtmlElementVisitor -> {
        visitor.visitBananaBoxBinding(this)
      }
      is XmlElementVisitor -> {
        visitor.visitXmlAttribute(this)
      }
      else -> {
        visitor.visitElement(this)
      }
    }
  }
}