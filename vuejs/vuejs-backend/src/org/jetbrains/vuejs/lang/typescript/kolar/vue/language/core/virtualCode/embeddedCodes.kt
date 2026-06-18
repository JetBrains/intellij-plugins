// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.virtualCode

import com.intellij.lang.typescript.kolar.sourceMap.KolarMapping
import org.jetbrains.vuejs.lang.typescript.kolar.js.symbol.Symbol
import org.jetbrains.vuejs.lang.typescript.kolar.muggle.string.DataSegment
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.Code
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.IRBlock
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.VueCodeInformation
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.utils.buildMappings

class VueEmbeddedCode(
  val id: String,
  val lang: String,
  val content: List<Code>,
) {
  var parentCodeId: String? = null
  val linkedCodeMappings: MutableList<KolarMapping<Nothing?>> = mutableListOf()
  val embeddedCodes: MutableList<VueEmbeddedCode> = mutableListOf()
}

fun getMappingsForCode(
  code: VueEmbeddedCode,
  nameToBlockMap: Map<String, IRBlock>,
): List<KolarMapping<VueCodeInformation>> {
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

  val newMappings = mutableListOf<AccumulatingMapping>()
  val tokenMappings = mutableMapOf<Symbol, AccumulatingMapping>()

  for (mapping in mappings) {
    val combineToken = mapping.data.__combineToken
    if (combineToken != null) {
      val existing = tokenMappings[combineToken]
      if (existing != null) {
        existing.sourceOffsets.addAll(mapping.sourceOffsets.toList())
        existing.generatedOffsets.addAll(mapping.generatedOffsets.toList())
        existing.lengths.addAll(mapping.lengths.toList())
      }
      else {
        val acc = mapping.toAccumulating()
        tokenMappings[combineToken] = acc
        newMappings.add(acc)
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
            lengths = intArrayOf(linkedToken.description?.toInt() ?: 0),
            data = null,
          )
        )
      }
      else {
        tokenMappings[linkedToken] = mapping.toAccumulating()
      }
      continue
    }
    newMappings.add(mapping.toAccumulating())
  }

  return newMappings.map { it.toKolarMapping() }
}

private fun KolarMapping<VueCodeInformation>.toAccumulating() = AccumulatingMapping(
  sourceOffsets = sourceOffsets.toMutableList(),
  generatedOffsets = generatedOffsets.toMutableList(),
  lengths = lengths.toMutableList(),
  data = data,
)

private class AccumulatingMapping(
  val sourceOffsets: MutableList<Int>,
  val generatedOffsets: MutableList<Int>,
  val lengths: MutableList<Int>,
  val data: VueCodeInformation,
) {
  fun toKolarMapping() = KolarMapping(
    sourceOffsets = sourceOffsets.toIntArray(),
    generatedOffsets = generatedOffsets.toIntArray(),
    lengths = lengths.toIntArray(),
    data = data,
  )
}
