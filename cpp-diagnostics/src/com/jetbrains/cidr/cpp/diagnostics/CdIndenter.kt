package com.jetbrains.cidr.cpp.diagnostics

class CdIndenter {
  private val builder = StringBuilder()
  private var indent = 0

  fun put(vararg lineParts: Any?): CdIndenter {
    for (i in 0 until indent) {
      builder.append(' ')
    }
    for (part in lineParts) {
      builder.append(part)
    }
    builder.append('\n')
    return this
  }

  fun indent() {
    indent++
  }

  fun unIndent() {
    indent--
  }

  val result: String
    get() = builder.toString()

  inline fun scope(block: () -> Unit) {
    indent()
    try {
      block()
    }
    finally {
      unIndent()
    }
  }
}
