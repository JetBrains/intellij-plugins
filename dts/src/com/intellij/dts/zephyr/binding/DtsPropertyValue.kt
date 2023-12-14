package com.intellij.dts.zephyr.binding

import com.intellij.dts.lang.DtsPropertyType
import com.intellij.util.asSafely

sealed interface DtsPropertyValue {
  val assignableTo: Collection<DtsPropertyType>

  data class String(val value: kotlin.String) : DtsPropertyValue {
    override val assignableTo: Collection<DtsPropertyType> = listOf(DtsPropertyType.String, DtsPropertyType.StringList)
  }

  data class StringList(val value: List<kotlin.String>) : DtsPropertyValue {
    override val assignableTo: Collection<DtsPropertyType> = listOf(DtsPropertyType.StringList)
  }

  data class Int(val value: kotlin.Int) : DtsPropertyValue {
    override val assignableTo: Collection<DtsPropertyType> = listOf(DtsPropertyType.Int, DtsPropertyType.Ints)
  }

  data class IntList(val value: List<kotlin.Int>) : DtsPropertyValue {
    override val assignableTo: Collection<DtsPropertyType>
      get() {
        return if (value.all { it in 0..255 }) {
          listOf(DtsPropertyType.Ints, DtsPropertyType.Bytes)
        }
        else {
          listOf(DtsPropertyType.Ints)
        }
      }

    fun asIntList(): List<kotlin.String> = value.map { it.toString() }

    fun asByteList(): List<kotlin.String> = value.map { kotlin.String.format("%02X", it) }
  }

  companion object {
    fun fromZephyr(value: Any): DtsPropertyValue? {
      return when (value) {
        is kotlin.String -> String(value)
        is kotlin.Int -> Int(value)
        is List<*> -> when {
          value.all { it is kotlin.Int } -> value.asSafely<List<kotlin.Int>>()?.let(::IntList)
          value.all { it is kotlin.String } -> value.asSafely<List<kotlin.String>>()?.let(::StringList)
          else -> null
        }
        else -> null
      }
    }
  }
}