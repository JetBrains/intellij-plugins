package org.jetbrains.qodana.ui

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.UIBundle
import com.intellij.ui.dsl.builder.bindSelected
import com.intellij.ui.dsl.builder.panel
import com.intellij.util.application
import com.intellij.util.ui.JBUI
import org.jetbrains.qodana.QodanaBundle
import org.jetbrains.qodana.settings.qodanaSettings
import javax.swing.JComponent

class OpenReportDialog(project: Project, private val action: () -> Unit) : DialogWrapper(project, false) {
  private lateinit var panel: DialogPanel

  init {
    title = QodanaBundle.message("open.report.dialog.title")
    isResizable = false
    setOKButtonText(QodanaBundle.message("open.report.dialog.ok.button.text"))
    init()
    initValidation()
  }

  override fun createCenterPanel(): JComponent {
    panel = promoPanel().apply {
      border = JBUI.Borders.empty(8, 12)
    }
    return panel
  }

  private fun promoPanel() = panel {
    val qodanaSettings = application.qodanaSettings()
    qodanaPromo(maxLineLength = 80)
    row { browserLink(QodanaBundle.message("qodana.get.report.help.button"), QODANA_HELP_URL) }
    row { checkBox(UIBundle.message("dialog.options.do.not.show"))
      .bindSelected({ !qodanaSettings.showPromo }, { qodanaSettings.showPromo = !it })
    }
  }

  override fun doOKAction() {
    panel.apply()
    close(OK_EXIT_CODE)
    action.invoke()
  }
}