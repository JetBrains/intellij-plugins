package com.intellij.plugins.serialmonitor.ui.console

import com.intellij.execution.filters.Filter
import com.intellij.execution.filters.HyperlinkInfo
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.ui.ConsoleView
import com.intellij.execution.ui.ConsoleViewContentType
import com.intellij.icons.AllIcons
import com.intellij.idea.ActionsBundle
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.ToggleAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.plugins.serialmonitor.SerialProfileService.NewLine
import com.intellij.plugins.serialmonitor.service.SerialPortService
import com.intellij.plugins.serialmonitor.ui.SerialMonitorBundle
import com.intellij.terminal.JBTerminalSystemSettingsProviderBase
import com.intellij.terminal.JBTerminalWidget
import com.jediterm.terminal.*
import com.jediterm.terminal.emulator.JediEmulator
import com.jediterm.terminal.model.JediTerminal
import com.jediterm.terminal.model.TerminalTextBuffer
import org.apache.commons.io.input.buffer.CircularByteBuffer
import java.io.InputStream
import java.io.InputStreamReader
import java.io.Reader
import java.nio.charset.Charset
import javax.swing.JComponent
import kotlin.math.min

private const val BUFFER_SIZE = 100000

class JeditermConsoleView(project: Project, connection: SerialPortService.SerialConnection) : ConsoleView {

  private val widget: JBTerminalWidget

  private val serialConnector = SerialTtyConnector(this, connection)

  private var emulator: CustomJeditermEmulator? = null

  private val bytesBuffer = CircularByteBuffer(BUFFER_SIZE)
  private val lock = Object()

  @Volatile
  private var bufferReader: Reader? = null

  @Volatile
  var paused: Boolean = false

  fun isTimestamped() = emulator?.isTimestamped == true

  private val bytesStream = object : InputStream() {
    override fun read(): Int {
      synchronized(lock) {
        while (!Thread.interrupted() && !bytesBuffer.hasBytes()) {
          lock.wait()
        }
        return if (bytesBuffer.hasBytes()) bytesBuffer.read().toInt() else -1
      }
    }

    override fun read(buffer: ByteArray, offset: Int, length: Int): Int {
      synchronized(lock) {
        while (!Thread.interrupted() && !bytesBuffer.hasBytes()) {
          try {
            lock.wait()
          }
          catch(_ : InterruptedException) {
            Thread.currentThread().interrupt()
            break
          }
        }
        val toRead = min(length, bytesBuffer.currentNumberOfBytes)
        bytesBuffer.read(buffer, offset, toRead)
        return toRead
      }
    }

    override fun available(): Int {
      synchronized(lock) {
        return bytesBuffer.currentNumberOfBytes
      }
    }
  }


  init {
    widget = object : JBTerminalWidget(project, JBTerminalSystemSettingsProviderBase(), this) {
      override fun createTerminalStarter(terminal: JediTerminal, connector: TtyConnector): TerminalStarter =
        object : TerminalStarter(terminal, connector,
                                 TtyBasedArrayDataStream(connector) { typeAheadManager.onTerminalStateChanged() },
                                 typeAheadManager, getExecutorServiceManager()) {
          override fun createEmulator(dataStream: TerminalDataStream, terminal: Terminal): JediEmulator =
            CustomJeditermEmulator(dataStream, terminal).apply { emulator = this }
        }
    }
    widget.start(serialConnector)
    Disposer.register(this, connection)
  }


  override fun dispose() {
  }

  override fun getComponent(): JComponent = widget
  override fun getPreferredFocusableComponent(): JComponent = widget

  override fun print(text: String, contentType: ConsoleViewContentType) {
    throw NotImplementedError("Not supported")
  }

  override fun clear() {
    widget.terminalTextBuffer.clearScreenAndHistoryBuffers()
    widget.terminal.clearScreen()
    widget.terminal.cursorPosition(0, 1)
  }

  override fun scrollTo(offset: Int) {
    widget.terminal.setScrollingRegion(offset, Int.MAX_VALUE)
  }

  override fun attachToProcess(processHandler: ProcessHandler) {
    throw IllegalArgumentException("Should not be called")
  }

  override fun setOutputPaused(value: Boolean) {
    paused = value
  }

  override fun isOutputPaused(): Boolean = paused

  override fun hasDeferredOutput(): Boolean {
    return false
  }

  override fun performWhenNoDeferredOutput(runnable: Runnable) {
    runnable.run()
  }

  override fun setHelpId(helpId: String) {}

  override fun addMessageFilter(filter: Filter) {
    throw NotImplementedError("Operation not supported")
  }

  override fun printHyperlink(hyperlinkText: String, info: HyperlinkInfo?) {
    print(hyperlinkText, ConsoleViewContentType.NORMAL_OUTPUT)
  }

  override fun getContentSize(): Int =
    with(widget.terminalTextBuffer) { screenLinesCount + widget.terminalTextBuffer.screenLinesCount }

  override fun canPause(): Boolean = true

  override fun createConsoleActions(): Array<AnAction> {
    return emptyArray()
  }

  override fun allowHeavyFilters() {}

  fun output(dataChunk: ByteArray) {
    if (!paused) {
      synchronized(lock) {
        val length = min(dataChunk.size, bytesBuffer.space)
        if (length > 0) {
          bytesBuffer.add(dataChunk, 0, length)
          lock.notify()
        }
      }
    }
  }

  fun reconnect(charset: Charset, newLine: NewLine, localEcho: Boolean) {
    emulator?.newLine = newLine
    widget.terminal.setAutoNewLine(newLine == NewLine.CRLF) //todo LF mode is not supported due JediTerm limitations
    serialConnector.charset = charset
    serialConnector.localEcho = localEcho
    synchronized(lock) {
      bytesBuffer.clear()
      bufferReader = InputStreamReader(bytesStream, charset)
    }
  }

  fun readChars(buf: CharArray, offset: Int, length: Int): Int {
    synchronized(lock) {
      while (true) {
        val currentReader = bufferReader
        if (currentReader?.ready() == true) {
          return currentReader.read(buf, offset, length)
        }
        try {
          lock.wait()
        }
        catch (_: InterruptedException) {
        }
      }
    }
  }

  fun getTerminalTextBuffer(): TerminalTextBuffer = widget.terminalTextBuffer

  val scrollToTheEndToolbarAction = object : ToggleAction(
    ActionsBundle.messagePointer("action.EditorConsoleScrollToTheEnd.text"),
    ActionsBundle.messagePointer("action.EditorConsoleScrollToTheEnd.text"),
    AllIcons.RunConfigurations.Scroll_down) {
    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun isSelected(e: AnActionEvent): Boolean {
      return widget.terminalPanel.verticalScrollModel.value == 0
    }

    override fun update(e: AnActionEvent) {
      e.presentation.isEnabledAndVisible = widget.isShowing
    }

    override fun setSelected(e: AnActionEvent, state: Boolean) {
      if (state) {
        widget.terminalPanel.verticalScrollModel.value = 0
      }
    }
  }

  val printTimestampsToggleAction = object : ToggleAction(
    SerialMonitorBundle.message("action.print.timestamps.text"),
    SerialMonitorBundle.message("action.print.timestamps.description"),
    AllIcons.Scope.Scratches) {

    override fun update(e: AnActionEvent) {
      e.presentation.isEnabled = widget.isShowing
    }

    override fun isSelected(e: AnActionEvent): Boolean = isTimestamped()
    override fun setSelected(e: AnActionEvent, isSelected: Boolean) {
      emulator?.isTimestamped = isSelected
    }
    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.EDT
  }
}
