// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.codeInsight.documentation

import com.intellij.util.IncorrectOperationException
import org.jetbrains.annotations.Nls
import org.jetbrains.vuejs.VueBundle
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

    @Nls
    fun typeOf(item: VueDocumentedItem): String =
      when (item) {
        is VueFunctionComponent -> "vue.documentation.type.functional.component"
        is VueComponent -> "vue.documentation.type.component"
        is VueDirective -> "vue.documentation.type.directive"
        is VueFilter -> "vue.documentation.type.filter"
        is VueMethod -> "vue.documentation.type.component.method"
        is VueEmitCall -> "vue.documentation.type.component.event"
        is VueSlot -> "vue.documentation.type.slot"
        is VueInputProperty -> "vue.documentation.type.component.property"
        is VueComputedProperty -> "vue.documentation.type.component.computed.property"
        is VueDataProperty -> "vue.documentation.type.component.data.property"
        is VueDirectiveModifier -> "vue.documentation.type.directive.modifier"
        is VueDirectiveArgument -> "vue.documentation.type.directive.argument"
        else -> throw IncorrectOperationException(item.javaClass.name)
      }.let { VueBundle.message(it) }

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
          item.argument?.documentation?.description?.let { sections["vue.documentation.section.argument"] = it }
        }
        is VueDirectiveArgument -> {
          if (item.required) {
            sections["vue.documentation.section.required"] = ""
          }
          item.pattern?.let { sections["vue.documentation.section.pattern"] = it.toString() }
        }
        is VueDirectiveModifier -> {
          item.pattern?.let { sections["vue.documentation.section.pattern"] = it.toString() }
        }
        is VueSlot -> {
          item.pattern?.let { sections["vue.documentation.section.pattern"] = it.toString() }
        }
        is VueInputProperty -> {
          if (item.required) {
            sections["vue.documentation.section.required"] = ""
          }
          item.defaultValue
            ?.takeIf { it != "null" }
            ?.let { sections["vue.documentation.section.default"] = it }
        }
      }
      return sections
        .map { (key, value) -> Pair(VueBundle.message(key), value) }
        .toMap()
    }
  }
}
