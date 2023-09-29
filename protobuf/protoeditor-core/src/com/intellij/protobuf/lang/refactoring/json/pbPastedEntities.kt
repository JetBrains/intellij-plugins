package com.intellij.protobuf.lang.refactoring.json

import com.google.protobuf.Struct

internal sealed class PbPastedEntity {
  abstract fun render(): String

  data class PbStruct(
    val name: String,
    val fields: List<PbField>
  ) : PbPastedEntity() {
    override fun render(): String {
      return """
        message $name {
          ${fields.joinToString(separator = "\n") { it.render() }}
        }
      """.trimIndent()
    }
  }

  data class PbField(
    val name: String,
    val isRepeated: Boolean,
    val type: String,
    val order: Int
  ) : PbPastedEntity() {
    override fun render(): String {
      return """
        ${if (isRepeated) "repeated" else ""} $type $name = $order;
      """.trimIndent()
    }
  }
}

internal data class PbStructInJson(
  val jsonNodeName: String,
  val struct: Struct
)