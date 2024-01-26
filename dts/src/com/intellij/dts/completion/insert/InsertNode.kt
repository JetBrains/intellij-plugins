package com.intellij.dts.completion.insert

fun DtsInsertSession.writeNodeContent() {
  writeSpace()

  if (!canWrite('{')) abort()
  write("{", moveCaret = true)

  setThreshold(2)

  if (!canWrite('}')) abort()
  write("}")

  setThreshold(0)
  writeSemicolon()
}