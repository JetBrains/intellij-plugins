package com.intellij.plugins.serialmonitor.ui.console

import com.intellij.plugins.serialmonitor.SerialProfileService.NewLine
import com.jediterm.terminal.Terminal
import com.jediterm.terminal.TerminalDataStream
import com.jediterm.terminal.emulator.JediEmulator
import java.text.SimpleDateFormat
import java.util.Date

class CustomJeditermEmulator(dataStream: TerminalDataStream?, terminal: Terminal?) : JediEmulator(dataStream, terminal) {
  var newLine: NewLine = NewLine.CRLF

  var isTimestamped = false
  private var onNewLineStart = true

  companion object {
    const val TIMESTAMP_FORMAT = "[HH:mm:ss.SSS] "
    fun getTimestamp(): String = SimpleDateFormat(TIMESTAMP_FORMAT).format(Date())
  }

  private fun maybeInsertTimestamp(terminal: Terminal) {
    if (isTimestamped && onNewLineStart) {
      terminal.writeCharacters(getTimestamp())
    }
  }

  override fun processChar(ch: Char, terminal: Terminal) {

    maybeInsertTimestamp(terminal)

    when (ch) {
      '\r' -> {
        terminal.carriageReturn()
        if (newLine == NewLine.CR) {
          terminal.newLine()
          onNewLineStart = true
        }
      }
      '\n' -> {
        if (newLine == NewLine.LF) {
          terminal.carriageReturn()
        }
        terminal.newLine()
        onNewLineStart = true
      }
      else -> {
        super.processChar(ch, terminal)
        onNewLineStart = false
      }

    }
  }

  override fun unsupported(vararg sequenceChars: Char) {
  }

}