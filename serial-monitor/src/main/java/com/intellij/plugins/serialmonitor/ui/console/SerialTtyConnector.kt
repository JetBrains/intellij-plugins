package com.intellij.plugins.serialmonitor.ui.console

import com.intellij.plugins.serialmonitor.service.JsscSerialService
import com.jediterm.terminal.TtyConnector
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

class SerialTtyConnector(private val consoleView: JeditermConsoleView) : TtyConnector {

  var charset: Charset = StandardCharsets.US_ASCII
  private val serialService: JsscSerialService = JsscSerialService.getInstance()


  override fun read(buf: CharArray, offset: Int, length: Int): Int {
    return consoleView.readBytes(buf, offset, length)
  }

  override fun write(bytes: ByteArray) =
    serialService.write(consoleView.portName, bytes)

  override fun write(string: String) {
    write(string.toByteArray(charset))
  }

  override fun isConnected(): Boolean = true

  override fun waitFor(): Int = 0

  override fun ready(): Boolean = consoleView.isReady()

  override fun getName(): String = consoleView.portName

  override fun close() {
  }

}

