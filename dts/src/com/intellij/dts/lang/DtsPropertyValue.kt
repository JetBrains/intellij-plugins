package com.intellij.dts.lang

import com.intellij.openapi.util.NlsSafe
import com.intellij.util.asSafely

sealed interface DtsPropertyValue {
  val assignableTo: Collection<DtsPropertyType>

  fun getPresentableText(type: DtsPropertyType): @NlsSafe kotlin.String

  data class String(val value: kotlin.String) : DtsPropertyValue {
    override val assignableTo: Collection<DtsPropertyType> = listOf(DtsPropertyType.String)

    override fun getPresentableText(type: DtsPropertyType): kotlin.String = "\"$value\""
  }

  data class StringList(val value: List<kotlin.String>) : DtsPropertyValue {
    override val assignableTo: Collection<DtsPropertyType> = listOf(DtsPropertyType.StringList)

    override fun getPresentableText(type: DtsPropertyType): kotlin.String = value.joinToString { "\"$it\"" }
  }

  data class Int(val value: kotlin.Int) : DtsPropertyValue {
    override val assignableTo: Collection<DtsPropertyType> = listOf(DtsPropertyType.Int)

    override fun getPresentableText(type: DtsPropertyType): kotlin.String = "<$value>"
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

    override fun getPresentableText(type: DtsPropertyType): kotlin.String {
      if (type == DtsPropertyType.Bytes) {
        return "[${asByteList()}]"
      } else {
        return "<${asIntList()}>"
      }
    }
  }

  companion object {
    fun fromZephyr(value: Any): DtsPropertyValue? {
      return when (value) {
        is kotlin.String -> String(value)
        is kotlin.Int -> Int(value)
        is List<*> -> when {
          value.all { it is kotlin.Int } -> value.asSafely<List<kotlin.Int>>()?.let(DtsPropertyValue::IntList)
          value.all { it is kotlin.String } -> value.asSafely<List<kotlin.String>>()?.let(DtsPropertyValue::StringList)
          else -> null
        }
        else -> null
      }
    }
  }
}