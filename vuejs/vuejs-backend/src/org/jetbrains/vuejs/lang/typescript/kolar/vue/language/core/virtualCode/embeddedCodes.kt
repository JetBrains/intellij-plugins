// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.virtualCode

import org.jetbrains.vuejs.lang.typescript.kolar.js.symbol.Symbol
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.Code
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.VueCodeInformation
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.VueMapping
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.utils.buildMappings

class VueEmbeddedCode(
  val id: String,
  val lang: String,
  var content: List<Code>,
) {
  var parentCodeId: String? = null
  val embeddedCodes: MutableList<VueEmbeddedCode> = mutableListOf()

  companion object {
    const val SCRIPT_ID: String = "script"
  }
}

fun getMappingsForCode(
  code: VueEmbeddedCode,
): List<VueMapping> {
  val mappings = buildMappings(code.content)

  val newMappings = mutableListOf<VueMappingBuilder>()
  val tokenMappings = mutableMapOf<Symbol, VueMappingBuilder>()

  for (mapping in mappings) {
    val combineToken = mapping.data.__combineToken
    if (combineToken != null) {
      val existing = tokenMappings[combineToken]
      if (existing != null) {
        existing.sourceOffsets += mapping.sourceOffsets
        existing.generatedOffsets += mapping.generatedOffsets
        existing.lengths += mapping.lengths
      }
      else {
        val builder = mapping.toBuilder()
        tokenMappings[combineToken] = builder
        newMappings.add(builder)
      }
      continue
    }
    newMappings.add(mapping.toBuilder())
  }

  return newMappings.map { it.toKolarMapping() }
}

private fun VueMapping.toBuilder() =
  VueMappingBuilder(
    sourceOffsets = sourceOffsets,
    generatedOffsets = generatedOffsets,
    lengths = lengths,
    data = data,
  )

private class VueMappingBuilder(
  var sourceOffsets: IntArray,
  var generatedOffsets: IntArray,
  var lengths: IntArray,
  val data: VueCodeInformation,
) {
  fun toKolarMapping(): VueMapping =
    VueMapping(
      sourceOffsets = sourceOffsets,
      generatedOffsets = generatedOffsets,
      lengths = lengths,
      data = data,
    )
}
