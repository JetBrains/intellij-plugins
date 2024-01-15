package com.intellij.dts.completion.insert

import com.intellij.dts.completion.provider.DtsInsertSession
import com.intellij.dts.completion.provider.dtsRunInsertSession

private class InsertStringBackend(val builder: StringBuilder) : DtsInsertBackend {
  override fun shouldWrite(char: Char): Int = 0

  override fun write(text: String) {
    builder.append(text)
  }

  override fun moveCaret(offset: Int) {}

  override fun openAutocomplete() {}
}

fun dtsInsertIntoString(writer: DtsInsertSession.() -> Unit): String {
  val builder = StringBuilder()
  val session = DtsInsertSession(InsertStringBackend(builder))

  dtsRunInsertSession(session, writer)

  return builder.toString()
}

