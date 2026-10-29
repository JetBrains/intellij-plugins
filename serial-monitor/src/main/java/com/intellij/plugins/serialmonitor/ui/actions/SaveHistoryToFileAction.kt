package com.intellij.plugins.serialmonitor.ui.actions

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.fileChooser.FileChooserFactory
import com.intellij.openapi.fileChooser.FileSaverDescriptor
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.plugins.serialmonitor.ui.SerialMonitorBundle
import com.jediterm.terminal.model.TerminalTextBuffer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.function.IntConsumer
import java.util.function.Supplier

/**
 * Saves the history of [terminalTextBuffer] to a file the user picks, offering [defaultFileName], and tells [onSaved] how many lines
 * it saved.
 *
 * @author Jan Papesch
 */
class SaveHistoryToFileAction(val terminalTextBuffer: TerminalTextBuffer,
                              private val defaultFileName: Supplier<String>,
                              private val onSaved: IntConsumer) : DumbAwareAction(SerialMonitorBundle.message("action.save.text"), SerialMonitorBundle.message("action.save.description"), AllIcons.Actions.MenuSaveall) {

  override fun actionPerformed(e: AnActionEvent) {

    val descriptor = FileSaverDescriptor(SerialMonitorBundle.message("dialog.save.title"), SerialMonitorBundle.message("dialog.save.desc"))
    val fileSaverDialog = FileChooserFactory.getInstance().createSaveFileDialog(descriptor, e.project)

    val file = fileSaverDialog.save(defaultFileName.get())?.file
    if (file == null) return

    e.coroutineScope.launch(Dispatchers.Default) {
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
      onSaved.accept(lines.size)
    }
  }
}
