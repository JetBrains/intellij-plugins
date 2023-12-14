package com.intellij.dts.lang

import com.intellij.dts.lang.psi.DtsArray
import com.intellij.dts.lang.psi.DtsProperty
import com.intellij.dts.lang.psi.DtsValue
import java.util.function.Predicate
import kotlin.reflect.KClass

private fun macroOrPredicate(predicate: Predicate<DtsValue>): Predicate<DtsValue> {
  return Predicate { value -> value is DtsValue.Macro || predicate.test(value) }
}

private fun list(predicate: Predicate<DtsValue>): Predicate<List<DtsValue>> {
  return Predicate { values -> values.all(predicate::test) }
}

private fun scalarList(predicate: Predicate<DtsValue>): Predicate<List<DtsValue>> {
  return Predicate { values -> values.size == 1 && predicate.test(values[0]) }
}

private inline fun <reified T : DtsArray> array(predicate: Predicate<DtsValue>): Predicate<DtsValue> {
  return macroOrPredicate { value -> value is T && value.dtsValues.all(predicate::test) }
}

private inline fun <reified T : DtsArray> scalarArray(predicate: Predicate<DtsValue>): Predicate<DtsValue> {
  return macroOrPredicate { value -> value is T && ensureOneElement(value.dtsValues) && value.dtsValues.all(predicate::test) }
}

private fun ensureOneElement(values: List<DtsValue>): Boolean {
  return values.isNotEmpty() && values.filter { it !is DtsValue.Macro }.size <= 1
}

private fun type(vararg types: KClass<*>): Predicate<DtsValue> {
  return macroOrPredicate { value -> types.any { type -> type.isInstance(value) } }
}

fun DtsProperty.dtsAssignableTo(type: DtsPropertyType): Boolean {
  val predicate = when (type) {
    DtsPropertyType.String -> scalarList(type(DtsValue.String::class))
    DtsPropertyType.Int -> scalarList(scalarArray<DtsArray.Cell>(type(DtsValue.Int::class)))
    DtsPropertyType.PHandle -> scalarList(scalarArray<DtsArray.Cell>(type(DtsValue.PHandle::class)))
    DtsPropertyType.Boolean -> Predicate { values -> values.isEmpty() }
    DtsPropertyType.Ints -> list(array<DtsArray.Cell>(type(DtsValue.Int::class)))
    DtsPropertyType.Bytes -> list(array<DtsArray.Byte>(type(DtsValue.Byte::class)))
    DtsPropertyType.PHandles -> list(array<DtsArray.Cell>(type(DtsValue.PHandle::class)))
    DtsPropertyType.StringList -> list(type(DtsValue.String::class))
    DtsPropertyType.PHandleList -> list(array<DtsArray.Cell>(type(DtsValue.PHandle::class, DtsValue.Int::class)))
    DtsPropertyType.Path -> scalarList(type(DtsValue.String::class, DtsValue.PHandle::class))
    DtsPropertyType.Compound -> Predicate { true }
  }

  return predicate.test(dtsValues)
}

private fun iterateValues(values: List<DtsValue>): Sequence<DtsValue> = sequence {
  for (value in values) {
    if (value is DtsArray) {
      yieldAll(value.dtsValues)
    }
    else {
      yield(value)
    }
  }
}

fun DtsProperty.dtsAssignableTo(const: DtsPropertyValue): Boolean {
  if (const.assignableTo.none(this::dtsAssignableTo)) return false

  val expectedValues = when (const) {
    is DtsPropertyValue.Int -> listOf(const.value)
    is DtsPropertyValue.IntList -> const.value
    is DtsPropertyValue.String -> listOf(const.value)
    is DtsPropertyValue.StringList -> const.value
  }

  val actualValues = iterateValues(dtsValues).toList()

  for ((i, element) in actualValues.takeWhile { it !is DtsValue.Macro }.withIndex()) {
    if (i >= expectedValues.size || element !is DtsValue.Parseable<*>) return false

    val value = element.dtsParse()
    if (value != null && value != expectedValues[i]) return false
  }

  return actualValues.size == expectedValues.size || actualValues.any { it is DtsValue.Macro }
}
