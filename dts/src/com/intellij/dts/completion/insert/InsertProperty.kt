package com.intellij.dts.completion.insert

import com.intellij.dts.lang.DtsPropertyType
import com.intellij.dts.lang.DtsPropertyValue
import com.intellij.dts.lang.symbols.DtsPropertySymbol

fun DtsInsertSession.writePropertyValue(symbol: DtsPropertySymbol) {
  when (symbol.type) {
    DtsPropertyType.Boolean -> writeSemicolon(moveCaret = true)
    DtsPropertyType.String, DtsPropertyType.StringList -> writeString(symbol)
    DtsPropertyType.Int, DtsPropertyType.Ints, DtsPropertyType.PHandleList -> writeCellArray(symbol)
    DtsPropertyType.PHandle, DtsPropertyType.PHandles -> writePHandle()
    DtsPropertyType.Bytes -> writeByteArray(symbol)
    DtsPropertyType.Path, DtsPropertyType.Compound -> writeCompound()
  }
}

private fun DtsInsertSession.writeString(symbol: DtsPropertySymbol) {
  val value = when (val default = symbol.defaultValue) {
    is DtsPropertyValue.String -> default.value
    is DtsPropertyValue.StringList -> default.value.joinToString(separator = "\", \"")
    else -> ""
  }

  val isCompatible = symbol.name == "compatible"
  val isStringEnum = symbol.enum?.filterIsInstance<DtsPropertyValue.String>()?.isNotEmpty() ?: false

  writeAssign()
  writePair('"', '"', body = value)

  openAutocomplete(condition = (isCompatible || isStringEnum) && value.isEmpty())

  writeSemicolon()
}

private fun DtsInsertSession.writeCellArray(symbol: DtsPropertySymbol) {
  val value = when (val default = symbol.defaultValue) {
    is DtsPropertyValue.Int -> default.value.toString()
    is DtsPropertyValue.IntList -> default.asIntList().joinToString(separator = " ")
    else -> ""
  }

  val isIntEnum = symbol.enum?.filterIsInstance<DtsPropertyValue.Int>()?.isNotEmpty() ?: false

  writeAssign()
  writePair('<', '>', body = value)

  openAutocomplete(condition = isIntEnum && value.isEmpty())

  writeSemicolon()
}

private fun DtsInsertSession.writeByteArray(symbol: DtsPropertySymbol) {
  val value = when (val default = symbol.defaultValue) {
    is DtsPropertyValue.IntList -> default.asByteList().joinToString(separator = " ")
    else -> ""
  }

  writeAssign()
  writePair('[', ']', body = value)
  writeSemicolon()
}

private fun DtsInsertSession.writePHandle() {
  writeAssign()
  writePair('<', '>', body = "&")
  openAutocomplete()
  writeSemicolon()
}

private fun DtsInsertSession.writeCompound() {
  writeAssign()
  writeSemicolon()
}
