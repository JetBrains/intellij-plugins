// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen.utils

import org.jetbrains.vuejs.lang.typescript.kolar.js.generator.ValueWithReturn
import org.jetbrains.vuejs.lang.typescript.kolar.muggle.string.DataSegment
import org.jetbrains.vuejs.lang.typescript.kolar.muggle.string.Source
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.Code
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.VueCodeInformation
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.VueCodeInformation.CombineToken

class Boundary
private constructor(
  private val source: Source,
  private val endOffset: Int,
  val features: VueCodeInformation,
) {
  companion object {
    fun start(
      source: Source,
      startOffset: Int,
      endOffset: Int,
      features: VueCodeInformation,
    ): ValueWithReturn<Code, Boundary> {
      val token = CombineToken(source)
      val boundaryFeatures = features.copy(__combineToken = token)
      return ValueWithReturn(
        value = DataSegment(text = "", source = source, sourceOffset = startOffset, data = boundaryFeatures),
        returnValue = Boundary(source, endOffset, boundaryFeatures),
      )
    }
  }

  fun end(): Code =
    DataSegment(text = "", source = source, sourceOffset = endOffset, data = features)
}
