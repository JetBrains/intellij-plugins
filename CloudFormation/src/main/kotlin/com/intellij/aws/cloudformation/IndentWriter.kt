package com.intellij.aws.cloudformation

import java.io.Writer

class IndentWriter(val writer: Writer, val indentString: String) {
  private var needIndent = true
  private var level = 0

  fun print(text: String) {
    if (text.isEmpty()) return

    if (needIndent) {
      writer.write(indentString.repeat(level))
      needIndent = false
    }

    val endsWithNewLine = text.endsWith("\n")

    val textWithoutEndNewLine = if (endsWithNewLine) text.substring(0, text.length - 1) else text
    writer.write(textWithoutEndNewLine.replace("\n", "\n" + indentString.repeat(level)))

    if (endsWithNewLine) {
      writer.write("\n")
      needIndent = true
    }

    text.let { }
  }

  fun println(text: String = "") {
    print(text + "\n")
  }

  fun <R> indent(block: () -> R): R {
    level++

    try {
      return block()
    } finally {
      level--
    }
  }
}