// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.expr.psi

import com.intellij.lang.javascript.psi.JSEvaluableElement
import com.intellij.lang.javascript.psi.resolve.JSEvaluateContext
import com.intellij.lang.javascript.psi.resolve.JSTypeProcessor
import com.intellij.psi.xml.XmlAttribute
import org.angular2.Angular2InjectionUtils.findInjectedAngularExpression
import org.angular2.lang.expr.psi.impl.Angular2EmptyTemplateBindings
import org.angular2.lang.html.psi.Angular2HtmlTemplateBindings
import org.angular2.lang.types.Angular2TypeUtils

/**
 * @see Angular2HtmlTemplateBindings
 *
 * @see Angular2TemplateBinding
 */
interface Angular2TemplateBindings : Angular2EmbeddedExpression, JSEvaluableElement {

  val templateName: String

  val bindings: Array<Angular2TemplateBinding>
  override fun evaluate(evaluateContext: JSEvaluateContext, typeProcessor: JSTypeProcessor): Boolean {
    val type = Angular2TypeUtils.getTemplateBindingsContextType(this)
    if (type != null) typeProcessor.process(type, evaluateContext)
    return true
  }

  companion object {
    operator fun get(attribute: XmlAttribute): Angular2TemplateBindings {
      if (attribute is Angular2HtmlTemplateBindings) {
        return attribute.bindings
      }
      assert(attribute.name.startsWith("*"))
      return findInjectedAngularExpression(attribute, Angular2TemplateBindings::class.java)
             ?: Angular2EmptyTemplateBindings(attribute, attribute.name.substring(1))
    }
  }
}