package com.intellij.plugins.serialmonitor.ui.actions

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.fileChooser.FileChooserFactory
import com.intellij.openapi.fileChooser.FileSaverDescriptor
import com.intellij.openapi.progress.currentThreadCoroutineScope
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.plugins.serialmonitor.SerialMonitorCollector
import com.intellij.plugins.serialmonitor.SerialPortProfile
import com.intellij.plugins.serialmonitor.ui.SerialMonitorBundle
import com.jediterm.terminal.model.TerminalTextBuffer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.io.path.Path

/**
 * @author Jan Papesch
 */
class SaveHistoryToFileAction(val terminalTextBuffer: TerminalTextBuffer, val serialPortProfile: SerialPortProfile) : DumbAwareAction(SerialMonitorBundle.message("action.save.text"), SerialMonitorBundle.message("action.save.description"), AllIcons.Actions.MenuSaveall) {

  private fun SerialPortProfile.defaultLogFilename() = "${Path(this.defaultName()).fileName}.log"

  override fun actionPerformed(e: AnActionEvent) {

    val descriptor = FileSaverDescriptor(SerialMonitorBundle.message("dialog.save.title"), SerialMonitorBundle.message("dialog.save.desc"))
    val fileSaverDialog = FileChooserFactory.getInstance().createSaveFileDialog(descriptor, e.project)

    val file = fileSaverDialog.save(serialPortProfile.defaultLogFilename())?.file
    if (file == null) return

    currentThreadCoroutineScope().launch(Dispatchers.Default) {
      val lines = try {
        terminalTextBuffer.lock()
        val historyLines = terminalTextBuffer.historyLinesStorage
        val screenLines = terminalTextBuffer.screenLinesStorage
        historyLines + screenLines
      }
      finally {
        terminalTextBuffer.unlock()
      }

      val text = lines.joinToString(separator = System.lineSeparator(), transform = { it.text} )
      withContext(Dispatchers.IO) {
        file.writeText(text)
      }
      SerialMonitorCollector.logSave(lines.size)
    }
  }
}