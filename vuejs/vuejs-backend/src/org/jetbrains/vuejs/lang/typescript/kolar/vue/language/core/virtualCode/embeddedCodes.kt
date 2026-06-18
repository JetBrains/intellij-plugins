// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.virtualCode

import com.intellij.lang.typescript.kolar.sourceMap.KolarMapping
import org.jetbrains.vuejs.lang.typescript.kolar.js.symbol.Symbol
import org.jetbrains.vuejs.lang.typescript.kolar.muggle.string.DataSegment
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.Code
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.IRBlock
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.VueCodeInformation
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.VueMapping
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.utils.buildMappings

class VueEmbeddedCode(
  val id: String,
  val lang: String,
  var content: List<Code>,
) {
  var parentCodeId: String? = null
  val linkedCodeMappings: MutableList<KolarMapping<Nothing?>> = mutableListOf()
  val embeddedCodes: MutableList<VueEmbeddedCode> = mutableListOf()
}

fun getMappingsForCode(
  code: VueEmbeddedCode,
  nameToBlockMap: Map<String, IRBlock>,
): List<VueMapping> {
  val mappings = buildMappings(
    code.content.map { segment ->
      if (segment !is DataSegment) return@map segment
      val source = segment.source ?: return@map segment
      val block = nameToBlockMap[source] ?: return@map segment
      segment.copy(
        source = null,
        sourceOffset = segment.sourceOffset + block.startTagEnd,
      )
    }
  )

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
    val linkedToken = mapping.data.__linkedToken
    if (linkedToken != null) {
      val prevMapping = tokenMappings[linkedToken]
      if (prevMapping != null) {
        code.linkedCodeMappings.add(
          KolarMapping(
            sourceOffsets = intArrayOf(prevMapping.generatedOffsets[0]),
            generatedOffsets = intArrayOf(mapping.generatedOffsets[0]),
            lengths = intArrayOf(linkedToken.value),
            data = null,
          )
        )
      }
      else {
        tokenMappings[linkedToken] = mapping.toBuilder()
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
