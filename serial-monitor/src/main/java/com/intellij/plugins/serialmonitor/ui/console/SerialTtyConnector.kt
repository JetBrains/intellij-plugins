package com.intellij.plugins.serialmonitor.ui.console

import com.intellij.plugins.serialmonitor.service.SerialPortService
import com.jediterm.core.util.TermSize
import com.jediterm.terminal.TtyConnector
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

class SerialTtyConnector(private val consoleView: JeditermConsoleView,
                         private val connection: SerialPortService.SerialConnection) : TtyConnector {

  var charset: Charset = StandardCharsets.US_ASCII
  var localEcho: Boolean = false


  override fun read(buf: CharArray, offset: Int, length: Int): Int {
    return consoleView.readChars(buf, offset, length)
  }

  override fun write(bytes: ByteArray) =
    connection.write(bytes)

  override fun write(string: String) =
    write(string.toByteArray(charset))

  override fun isConnected(): Boolean = true

  override fun waitFor(): Int = 0

  override fun ready(): Boolean = true

  override fun getName(): String = connection.portName

  override fun close() {
  }

  override fun resize(size: TermSize) {
    //Nothing to do here, serial consoles do not care of the window size
  }
}

