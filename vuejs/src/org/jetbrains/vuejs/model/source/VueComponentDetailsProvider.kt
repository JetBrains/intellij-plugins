// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model.source

import org.jetbrains.vuejs.codeInsight.attributes.VueAttributesProvider.Companion.isVSlot

class VueComponentDetailsProvider {
  companion object {
    private val BIND_VARIANTS = setOf(".prop", ".camel", ".sync")
    private val ON_VARIANTS = setOf("*")
    private val PREFIX_VARIANTS = mapOf(Pair(":", BIND_VARIANTS), Pair("v-bind:", BIND_VARIANTS),
                                        Pair("@", ON_VARIANTS), Pair("v-on:", ON_VARIANTS),
                                        Pair("v-slot:", emptySet()))
    private val EVENT_MODIFIERS = setOf(".stop", ".prevent", ".capture", ".self", ".once", ".passive", ".native")
    private val NO_VALUE = mapOf(Pair("@", EVENT_MODIFIERS), Pair("v-on:", EVENT_MODIFIERS))

    fun attributeAllowsNoValue(attributeName: String): Boolean {
      return isVSlot(attributeName) || NO_VALUE.any {
        val cutPrefix = attributeName.substringAfter(it.key, "")
        cutPrefix.isNotEmpty() && it.value.any { eventModifier -> cutPrefix.endsWith(eventModifier) }
      }
    }

    fun getBoundName(attributeName: String): String? {
      return PREFIX_VARIANTS.mapNotNull {
        val after = attributeName.substringAfter(it.key, "")
        if (after.isNotEmpty()) {
          return after.substringBefore(".", after)
        }
        return@mapNotNull null
      }.firstOrNull()
             ?: if (attributeName.contains('.')) {
               // without prefix, but might be with postfix
               attributeName.substringBefore(".", "")
             }
             // if just attribute name should be used, return null
             else null
    }
  }
}
