package org.angular2.entities

import com.intellij.lang.javascript.psi.JSElement

data class Angular2TemplateGuard(
  val inputName: String,
  val type: Kind,
  val member: JSElement
) {
  enum class Kind {
    Binding,
    Method
  }
}