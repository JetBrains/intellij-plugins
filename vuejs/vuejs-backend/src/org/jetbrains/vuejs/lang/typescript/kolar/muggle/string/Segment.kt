// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.muggle.string

sealed interface Segment<out T>

@JvmInline
value class StringSegment(
  val text: String,
) : Segment<Nothing>

data class DataSegment<out T>(
  val text: String,
  val source: String?,
  val sourceOffset: Int,
  val data: T,
) : Segment<T>
