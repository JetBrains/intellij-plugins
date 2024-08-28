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
  private var lastCharCR = false
  private val myFormatter = SimpleDateFormat(TIMESTAMP_FORMAT)
  private fun getTimestamp(): String = myFormatter.format(Date())

  companion object {
    const val TIMESTAMP_FORMAT = "[HH:mm:ss.SSS] "
  }

  private fun Terminal.isOnLineStart(): Boolean = this.cursorX == 1 // cursorX starts at 1

  private fun maybeInsertTimestamp(nextChar: Char, terminal: Terminal) {
    if (!isTimestamped) return // Insert only when enabled
    if (!terminal.isOnLineStart()) return // Insert only at the beginning of lines
    if (lastCharCR && nextChar == '\n' && newLine == NewLine.CRLF) return // Don't insert in the middle of CRLF

    terminal.writeCharacters(getTimestamp())
  }

  override fun processChar(ch: Char, terminal: Terminal) {
    maybeInsertTimestamp(ch, terminal)
    when (ch) {
      '\r' -> {
        terminal.carriageReturn()
        if (newLine == NewLine.CR) {
          terminal.newLine()
        }
        else if (isTimestamped) {
          // Move the cursor after the timestamp (and leave the timestamp unchanged)
          terminal.cursorForward(TIMESTAMP_FORMAT.length)
        }
      }
      '\n' -> {
        if (newLine == NewLine.LF || isTimestamped) {
          // move the cursor to the start of the line, CR moves it after the timestamp.
          terminal.carriageReturn()
        }
        terminal.newLine()
      }
      else -> super.processChar(ch, terminal)
    }
    lastCharCR = ch == '\r'
  }

  override fun unsupported(vararg sequenceChars: Char) {
  }

}