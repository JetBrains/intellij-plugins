package com.intellij.plugins.serialmonitor.ui.console

import com.intellij.openapi.util.NlsSafe
import com.jediterm.core.util.TermSize
import com.jediterm.terminal.TtyConnector
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.util.function.Consumer

internal class SerialTtyConnector(private val consoleView: JeditermConsoleView,
                                  private val name: @NlsSafe String,
                                  private val input: Consumer<ByteArray>?) : TtyConnector {

  var charset: Charset = StandardCharsets.US_ASCII


  override fun read(buf: CharArray, offset: Int, length: Int): Int {
    return consoleView.readChars(buf, offset, length)
  }

  override fun write(bytes: ByteArray) {
    input?.accept(bytes)
  }

  override fun write(string: String) =
    write(string.toByteArray(charset))

  override fun isConnected(): Boolean = consoleView.isConnected()

  override fun waitFor(): Int = 0

  override fun ready(): Boolean = true

  override fun getName(): String = name

  override fun close() {
    consoleView.disconnect()
  }

  override fun resize(size: TermSize) {
    //Nothing to do here, serial consoles do not care of the window size
  }
}

