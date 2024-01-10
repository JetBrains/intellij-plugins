// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.expr.psi

import com.intellij.lang.javascript.psi.JSExpressionStatement
import com.intellij.psi.xml.XmlAttribute
import org.angular2.Angular2InjectionUtils
import org.angular2.lang.html.psi.Angular2HtmlEvent

interface Angular2Action : Angular2EmbeddedExpression {
  val statements: Array<JSExpressionStatement>

  companion object {
    @JvmStatic
    fun get(attribute: XmlAttribute): Angular2Action? {
      return when (attribute) {
        is Angular2HtmlEvent -> attribute.action
        else -> Angular2InjectionUtils.findInjectedAngularExpression(attribute, Angular2Action::class.java)
      }
    }
  }
}