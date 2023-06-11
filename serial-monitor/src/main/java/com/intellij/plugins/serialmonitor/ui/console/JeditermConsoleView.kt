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
import com.intellij.plugins.serialmonitor.SerialProfileService.NewLine
import com.intellij.terminal.JBTerminalSystemSettingsProviderBase
import com.intellij.terminal.JBTerminalWidget
import com.jediterm.terminal.*
import com.jediterm.terminal.emulator.JediEmulator
import com.jediterm.terminal.model.JediTerminal
import java.io.*
import java.nio.charset.Charset
import javax.swing.JComponent

class JeditermConsoleView(project: Project, val portName: String) : ConsoleView {

  private val widget: JBTerminalWidget

  private val serialConnector = SerialTtyConnector(this)

  private var emulator: CustomJeditermEmulator? = null

  @Volatile
  private var inPipe: PipedOutputStream? = null

  @Volatile
  private var outPipe: Reader? = null

  @Volatile
  var paused: Boolean = false

  init {
    widget = object : JBTerminalWidget(project, JBTerminalSystemSettingsProviderBase(), this) {
      override fun createTerminalStarter(terminal: JediTerminal, connector: TtyConnector): TerminalStarter =
        object : TerminalStarter(terminal, connector,
                                 TtyBasedArrayDataStream(connector) { typeAheadManager.onTerminalStateChanged() },
                                 typeAheadManager, getExecutorServiceManager()) {
          override fun createEmulator(dataStream: TerminalDataStream, terminal: Terminal): JediEmulator =
             CustomJeditermEmulator(dataStream, terminal).apply { emulator=this }
        }
    }
    widget.start(serialConnector)
  }


  override fun dispose() {
  }

  override fun getComponent(): JComponent = widget
  override fun getPreferredFocusableComponent(): JComponent = widget

  override fun print(text: String, contentType: ConsoleViewContentType) {
    throw NotImplementedError("Not supported")
  }

  override fun clear() {
    widget.terminalTextBuffer.historyBuffer.clearAll()
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
    try {
      inPipe?.write(dataChunk)
      inPipe?.flush()
    }
    catch (_: IOException) {
    }
  }

  fun reconnect(charset: Charset, newLine: NewLine) {
    emulator?.newLine = newLine
    inPipe = PipedOutputStream()
    outPipe = InputStreamReader(PipedInputStream(inPipe), charset)
  }

  fun isReady(): Boolean {
    try {
      return outPipe?.ready() ?: false
    } catch (_:IOException) {
      return false
    }
  }

  fun readBytes(buf: CharArray, offset: Int, length: Int): Int {
    while (true) {
      with(outPipe) {
        if (this != null && !paused) try {
          return read(buf, offset, length)
        }
        catch (_: Throwable) {
        }
        Thread.sleep(20);
      }

    }
  }

  val scrollToTheEndToolbarAction = object : ToggleAction(
    ActionsBundle.messagePointer("action.EditorConsoleScrollToTheEnd.text"),
    ActionsBundle.messagePointer("action.EditorConsoleScrollToTheEnd.text"),
    AllIcons.RunConfigurations.Scroll_down) {
    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun isSelected(e: AnActionEvent): Boolean {
      return widget.terminalPanel.verticalScrollModel.value == 0
    }

    override fun setSelected(e: AnActionEvent, state: Boolean) {
      if (state) {
        widget.terminalPanel.verticalScrollModel.value = 0
      }
    }
  }


}
