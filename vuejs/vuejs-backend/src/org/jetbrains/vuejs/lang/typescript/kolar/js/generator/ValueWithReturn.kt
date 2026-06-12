// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.js.generator

data class ValueWithReturn<out V, out R>(
  val value: V,
  val returnValue: R,
)

suspend fun <V, R> SequenceScope<V>.yield(
  valueWithReturn: ValueWithReturn<V, R>,
): R {
  val (value, returnValue) = valueWithReturn
  yield(value)
  return returnValue
}
