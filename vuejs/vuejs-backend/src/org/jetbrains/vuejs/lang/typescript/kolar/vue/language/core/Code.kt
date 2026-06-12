// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core

import org.jetbrains.vuejs.lang.typescript.kolar.muggle.string.Segment
import org.jetbrains.vuejs.lang.typescript.kolar.muggle.string.StringSegment

typealias Code = Segment<VueCodeInformation>

suspend fun SequenceScope<Code>.yield(
  value: String,
) {
  yield(StringSegment(value))
}