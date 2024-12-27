// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html.psi.impl

import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.XmlElementVisitor
import com.intellij.psi.util.PsiTreeUtil
import org.angular2.lang.expr.psi.Angular2Action
import org.angular2.lang.html.parser.Angular2AttributeNameParser
import org.angular2.lang.html.parser.Angular2HtmlElementTypes
import org.angular2.lang.html.psi.Angular2HtmlElementVisitor
import org.angular2.lang.html.psi.Angular2HtmlEvent
import org.angular2.lang.html.psi.Angular2HtmlEvent.AnimationPhase

internal class Angular2HtmlEventImpl(type: Angular2HtmlElementTypes.Angular2ElementType) : Angular2HtmlBoundAttributeImpl(type), Angular2HtmlEvent {
  override fun accept(visitor: PsiElementVisitor) {
    when (visitor) {
      is Angular2HtmlElementVisitor -> {
        visitor.visitEvent(this)
      }
      is XmlElementVisitor -> {
        visitor.visitXmlAttribute(this)
      }
      else -> {
        visitor.visitElement(this)
      }
    }
  }

  override val eventName: String
    get() = attributeInfo.name
  override val eventType: Angular2HtmlEvent.EventType
    get() = (attributeInfo as Angular2AttributeNameParser.EventInfo).eventType
  override val animationPhase: AnimationPhase?
    get() = (attributeInfo as Angular2AttributeNameParser.EventInfo).animationPhase
  override val action: Angular2Action?
    get() = PsiTreeUtil.findChildrenOfType(this, Angular2Action::class.java).firstOrNull()
}