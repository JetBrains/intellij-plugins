// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.codeInsight.documentation

import com.intellij.util.IncorrectOperationException
import org.jetbrains.vuejs.model.*

interface VueItemDocumentation {
  /**
   * Default symbol name
   */
  val defaultName: String?

  /**
   * Symbol type
   */
  val type: String

  /**
   * Description of the entity with HTML markup
   */
  val description: String?

  /**
   * URL for external documentation
   */
  val docUrl: String?

  /**
   * Library of origin
   */
  val library: String?

  /**
   * Custom section to display in the documentation
   */
  val customSections: Map<String, String> get() = emptyMap()

  companion object {
    fun typeOf(item: VueDocumentedItem): String =
      when (item) {
        is VueFunctionComponent -> "functional component"
        is VueComponent -> "component"
        is VueDirective -> "directive"
        is VueFilter -> "filter"
        is VueMethod -> "component method"
        is VueEmitCall -> "component event"
        is VueSlot -> "slot"
        is VueInputProperty -> "component property"
        is VueComputedProperty -> "component computed property"
        is VueDataProperty -> "component data property"
        is VueDirectiveModifier -> "directive modifier"
        is VueDirectiveArgument -> "directive argument"
        else -> throw IncorrectOperationException(item.javaClass.name)
      }

    fun nameOf(item: VueDocumentedItem): String? =
      when (item) {
        is VueNamedEntity -> item.defaultName
        is VueNamedSymbol -> item.name
        is VueDirectiveArgument -> null
        else -> throw IncorrectOperationException(item.javaClass.name)
      }

    fun createSections(item: VueDocumentedItem): Map<String, String> {
      val sections = LinkedHashMap<String, String>()
      when (item) {
        is VueDirective -> {
          item.argument?.documentation?.description?.let { sections["Argument:"] = it }
        }
        is VueDirectiveArgument -> {
          if (item.required) {
            sections["Required"] = ""
          }
          item.pattern?.let { sections["Pattern:"] = it.toString() }
        }
        is VueDirectiveModifier -> {
          item.pattern?.let { sections["Pattern:"] = it.toString() }
        }
        is VueSlot -> {
          item.pattern?.let { sections["Pattern:"] = it.toString() }
        }
        is VueInputProperty -> {
          if (item.required) {
            sections["Required"] = ""
          }
          item.defaultValue
            ?.takeIf { it != "null" }
            ?.let { sections["Default:"] = it }
        }
      }
      return sections
    }
  }
}
