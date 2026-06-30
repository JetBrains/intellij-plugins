// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.muggle.string

sealed interface Segment<out T> {
  val text: String
}

@JvmInline
value class StringSegment(
  override val text: String,
) : Segment<Nothing>

@JvmInline
value class Source(
  val value: String,
)

data class DataSegment<out T>(
  override val text: String,
  val source: Source?,
  val sourceOffset: Int,
  val data: T,
) : Segment<T>
