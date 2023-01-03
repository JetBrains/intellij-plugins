// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.expr.psi

import com.intellij.lang.javascript.psi.JSExpression
import com.intellij.psi.xml.XmlAttribute
import org.angular2.Angular2InjectionUtils.findInjectedAngularExpression
import org.angular2.lang.html.psi.Angular2HtmlBananaBoxBinding
import org.angular2.lang.html.psi.Angular2HtmlPropertyBinding

interface Angular2Binding : Angular2EmbeddedExpression {

  val expression: JSExpression?
  val quote: Angular2Quote?

  companion object {
    @JvmStatic
    fun get(attribute: XmlAttribute): Angular2Binding? {
      return when (attribute) {
        is Angular2HtmlPropertyBinding -> attribute.binding
        is Angular2HtmlBananaBoxBinding -> attribute.binding
        else -> findInjectedAngularExpression(attribute, Angular2Binding::class.java)
      }
    }
  }
}