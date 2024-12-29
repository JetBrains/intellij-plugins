package com.intellij.plugins.serialmonitor.ui.actions

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.plugins.serialmonitor.ui.SerialMonitor
import com.intellij.plugins.serialmonitor.ui.SerialMonitorBundle
import com.intellij.plugins.serialmonitor.ui.serialSettings
import com.intellij.ui.JBColor
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.panel
import org.jetbrains.annotations.Nls
import javax.swing.BorderFactory
import javax.swing.JComponent

/**
 * @author Dmitry_Cherkas, Ilia Motornyi
 */
internal class EditSettingsAction(private val myName: @Nls String, private val serialMonitor: SerialMonitor) :
  DumbAwareAction(SerialMonitorBundle.message("edit-settings.title"),
                  SerialMonitorBundle.message("edit-settings.tooltip"),
                  AllIcons.General.Settings) {
  override fun actionPerformed(e: AnActionEvent) {

    val settingsDialog = SettingsDialog(e.project)
    val okClicked: Boolean = settingsDialog.showAndGet()
    if (okClicked) {
      serialMonitor.notifyProfileChanged()
    }
  }

  private inner class SettingsDialog(project: Project?) : DialogWrapper(project, false, IdeModalityType.IDE/*todo change to PROJECT when platform is fixed*/) {
    init {
      title = SerialMonitorBundle.message("dialog.title.serial.port.settings", myName)
      init()
    }

    override fun createCenterPanel(): JComponent {
      return panel {
        row {
          label(serialMonitor.portProfile.portName).align(Align.FILL)
            .resizableColumn()
            .label(SerialMonitorBundle.message("label.port"))
            .applyToComponent {
              border = BorderFactory.createLineBorder(JBColor.border())
            }
        }.layout(com.intellij.ui.dsl.builder.RowLayout.LABEL_ALIGNED)
        serialSettings(disposable, serialMonitor.portProfile) { }
      }
    }
  }
}
