package com.intellij.aws.cloudformation.tests

import org.apache.commons.lang.SystemUtils
import org.apache.commons.lang.builder.ReflectionToStringBuilder
import org.apache.commons.lang.builder.ToStringStyle

class MyToStringStyle private constructor(internal var offset: Int, val excludedFields: Array<String>) : ToStringStyle() {
  private val indent = "  "
  private val linesep = SystemUtils.LINE_SEPARATOR

  companion object {
    fun toString(obj: Any?, excludedFields: Array<String>, offset: Int = 0): String =
        ReflectionToStringBuilder(obj, MyToStringStyle(offset, excludedFields)).setExcludeFieldNames(excludedFields).toString()
  }

  init {
    contentStart = "{"
    fieldSeparator = linesep + indent.repeat(offset + 1)
    isFieldSeparatorAtStart = true
    isUseShortClassName = true
    isUseIdentityHashCode = false
    contentEnd = linesep + indent.repeat(offset) + "}"
  }

  override fun appendDetail(buffer: StringBuffer, fieldName: String, col: Collection<*>) {
    if (col.isEmpty()) {
      buffer.append("[]")
      return
    }

    buffer.append('[')
    for (iter in col.withIndex()) {
      buffer.append(linesep + indent.repeat(offset + 2))
      buffer.append(toString(iter.value, excludedFields, offset + 2))

      if (iter.index < col.size - 1) {
        buffer.append(',')
      }
    }
    buffer.append(linesep).append(indent.repeat(offset + 1)).append(']')
  }

  override fun appendDetail(buffer: StringBuffer, fieldName: String, value: Any) {
    if (value.javaClass.name.startsWith("com.intellij.aws.cloudformation")) {
      buffer.append(toString(value, excludedFields, offset + 1))
    } else {
      super.appendDetail(buffer, fieldName, value)
    }
  }
}