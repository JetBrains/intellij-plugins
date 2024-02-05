package com.intellij.dts.completion.insert

interface DtsInsertMetaData {
  val offset: Int
  val requestedAutocomplete: Boolean
}

private class InsertOffsetBackend : DtsInsertBackend, DtsInsertMetaData {
  override var offset = 0
  override var requestedAutocomplete = false

  override fun shouldWrite(char: Char): Int = 0

  override fun write(text: String) {}

  override fun moveCaret(offset: Int) {
    this.offset = offset
  }

  override fun openAutocomplete() {
    requestedAutocomplete = true
  }
}

fun dtsInsertMetaData(writer: DtsInsertSession.() -> Unit): DtsInsertMetaData {
  val backend = InsertOffsetBackend()

  dtsRunInsertSession(DtsInsertSession(backend), writer)

  return backend
}