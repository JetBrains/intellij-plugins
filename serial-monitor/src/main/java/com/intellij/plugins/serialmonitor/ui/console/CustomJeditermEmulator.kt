package com.intellij.plugins.serialmonitor.ui.console

import com.intellij.plugins.serialmonitor.SerialProfileService.NewLine
import com.jediterm.terminal.Terminal
import com.jediterm.terminal.TerminalDataStream
import com.jediterm.terminal.emulator.JediEmulator

class CustomJeditermEmulator(dataStream: TerminalDataStream?, terminal: Terminal?) : JediEmulator(dataStream, terminal) {
  var newLine: NewLine = NewLine.CRLF
  override fun processChar(ch: Char, terminal: Terminal) {
    when (ch) {
      '\r' -> {
        terminal.carriageReturn()
        if (newLine == NewLine.CR) {
          terminal.newLine()
        }
      }
      '\n' -> {
        if (newLine == NewLine.LF) {
          terminal.carriageReturn()
        }
        terminal.newLine()
      }
      else -> super.processChar(ch, terminal)
    }
  }

  override fun unsupported(vararg sequenceChars: Char) {
  }

}