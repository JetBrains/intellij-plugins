package com.intellij.dts.lang

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

fun DtsProperty.dtsAssignableTo(type: DtsPropertyType): Boolean {
    val predicate = when (type) {
        DtsPropertyType.String -> scalar(type(DtsValue.String::class))
        DtsPropertyType.Int -> scalar(cellArray(scalar(type(DtsValue.Int::class, DtsValue.Expression::class))))
        DtsPropertyType.PHandle -> scalar(cellArray(scalar(type(DtsValue.PHandle::class))))
        DtsPropertyType.Boolean -> Predicate { values -> values.isEmpty() }
        DtsPropertyType.Ints -> list(cellArray(list(type(DtsValue.Int::class, DtsValue.Expression::class))))
        DtsPropertyType.Bytes -> scalar(byteArray(list(type(DtsValue.Byte::class))))
        DtsPropertyType.PHandles -> list(cellArray(list(type(DtsValue.PHandle::class))))
        DtsPropertyType.StringList -> notEmptyList(type(DtsValue.String::class))
        DtsPropertyType.PHandleList -> list(cellArray(list(type(DtsValue.PHandle::class, DtsValue.Int::class, DtsValue.Expression::class))))
        DtsPropertyType.Path -> scalar(type(DtsValue.String::class, DtsValue.PHandle::class))
        DtsPropertyType.Compound -> Predicate { true }
    }

    return predicate.test(dtsValues)
}