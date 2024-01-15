package com.intellij.dts.completion.provider

import com.intellij.dts.completion.insert.DtsInsertBackend

private object InsertAbort : Exception()

class DtsInsertSession (private val backend: DtsInsertBackend) {
  private var offset: Int = 0
  private var shouldThreshold: Int = 0

  fun abort() {
    throw InsertAbort
  }

  fun setThreshold(threshold: Int) {
    shouldThreshold = threshold
  }

  fun canWrite(char: Char): Boolean {
    return shouldThreshold >= backend.shouldWrite(char)
  }

  fun write(text: String, moveCaret: Boolean = false) {
    backend.write(text)
    offset += text.length

    if (moveCaret) {
      backend.moveCaret(offset)
    }
  }

  fun writePair(left: Char, right: Char, body: String) {
    if (!canWrite(left)) abort()
    write(left.toString(), moveCaret = true)

    if (!canWrite(right)) abort()
    write(body, moveCaret = true)
    write(right.toString())
  }

  fun writeSpace() {
    if (!canWrite(' ')) abort()
    write(" ")
  }

  fun writeAssign() {
    if (!canWrite('=')) abort()
    write(" = ", moveCaret = true)
  }

  fun writeSemicolon(moveCaret: Boolean = false) {
    if (!canWrite(';')) abort()
    write(";", moveCaret)
  }

  fun openAutocomplete(condition: Boolean = true) {
    if (condition) {
      backend.openAutocomplete()
    }
  }
}

fun dtsRunInsertSession(session: DtsInsertSession, writer: DtsInsertSession.() -> Unit) {
  try {
    writer(session)
  } catch (_: InsertAbort) {}
}
