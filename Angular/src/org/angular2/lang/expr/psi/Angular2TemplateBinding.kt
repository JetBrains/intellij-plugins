// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.expr.psi

import com.intellij.lang.javascript.psi.JSExpression
import com.intellij.lang.javascript.psi.JSStatement
import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.JSVariable
import org.angular2.lang.html.psi.Angular2HtmlTemplateBindings

/**
 * @see Angular2HtmlTemplateBindings
 *
 * @see Angular2TemplateBindings
 */
interface Angular2TemplateBinding : JSStatement {

  val key: String
  val keyElement: Angular2TemplateBindingKey?
  val keyJSType: JSType?
  fun keyIsVar(): Boolean
  val keyKind: KeyKind

  override fun getName(): String?
  val variableDefinition: JSVariable?

  val expression: JSExpression?

  enum class KeyKind {
    BINDING, LET, AS,
  }

  companion object {
    val EMPTY_ARRAY: Array<Angular2TemplateBinding> = emptyArray<Angular2TemplateBinding>()
  }
}