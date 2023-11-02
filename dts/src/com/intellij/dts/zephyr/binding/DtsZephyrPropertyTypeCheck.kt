package com.intellij.dts.zephyr.binding

import com.intellij.dts.lang.psi.DtsArray
import com.intellij.dts.lang.psi.DtsProperty
import com.intellij.dts.lang.psi.DtsValue
import java.util.function.Predicate
import kotlin.reflect.KClass

private fun macroOr(value: DtsValue, predicate: Predicate<DtsValue>): Boolean {
    return value is DtsValue.Macro || predicate.test(value)
}

private fun scalar(predicate: Predicate<DtsValue>): Predicate<List<DtsValue>> {
    return Predicate { values -> values.size == 1 && macroOr(values[0], predicate) }
}

private fun list(predicate: Predicate<DtsValue>): Predicate<List<DtsValue>> {
    return Predicate { values -> values.all { value -> macroOr(value, predicate) } }
}

private fun notEmptyList(predicate: Predicate<DtsValue>): Predicate<List<DtsValue>> {
    return Predicate { values -> values.isNotEmpty() && list(predicate).test(values) }
}

private fun dropMacros(values: List<DtsValue>, predicate: Predicate<List<DtsValue>>): Boolean {
    val nonMacro = values.filter { value -> value !is DtsValue.Macro }
    if (nonMacro.isEmpty() && values.isNotEmpty()) return true

    return predicate.test(nonMacro)
}

private fun cellArray(predicate: Predicate<List<DtsValue>>): Predicate<DtsValue> {
    return Predicate { value -> value is DtsArray.Cell && dropMacros(value.dtsValues, predicate) }
}

private fun byteArray(predicate: Predicate<List<DtsValue>>): Predicate<DtsValue> {
    return Predicate { value -> value is DtsArray.Byte && dropMacros(value.dtsValues, predicate) }
}

private fun type(vararg types: KClass<*>): Predicate<DtsValue> {
    return Predicate { value -> types.any { type -> type.isInstance(value) } }
}

fun DtsProperty.dtsAssignableTo(type: DtsZephyrPropertyType): Boolean {
    val predicate = when (type) {
        DtsZephyrPropertyType.String -> scalar(type(DtsValue.String::class))
        DtsZephyrPropertyType.Int -> scalar(cellArray(scalar(type(DtsValue.Int::class, DtsValue.Expression::class))))
        DtsZephyrPropertyType.PHandle -> scalar(cellArray(scalar(type(DtsValue.PHandle::class))))
        DtsZephyrPropertyType.Boolean -> Predicate { values -> values.isEmpty() }
        DtsZephyrPropertyType.Ints -> list(cellArray(list(type(DtsValue.Int::class, DtsValue.Expression::class))))
        DtsZephyrPropertyType.Bytes -> scalar(byteArray(list(type(DtsValue.Byte::class))))
        DtsZephyrPropertyType.PHandles -> list(cellArray(list(type(DtsValue.PHandle::class))))
        DtsZephyrPropertyType.StringList -> notEmptyList(type(DtsValue.String::class))
        DtsZephyrPropertyType.PHandleList -> list(
          cellArray(list(type(DtsValue.PHandle::class, DtsValue.Int::class, DtsValue.Expression::class))))
        DtsZephyrPropertyType.Path -> scalar(type(DtsValue.String::class, DtsValue.PHandle::class))
        DtsZephyrPropertyType.Compound -> Predicate { true }
    }

    return predicate.test(dtsValues)
}