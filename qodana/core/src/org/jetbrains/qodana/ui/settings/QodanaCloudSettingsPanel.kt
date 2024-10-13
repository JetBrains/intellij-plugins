package org.jetbrains.qodana.ui.settings

import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.options.*
import com.intellij.openapi.options.Configurable.NoScroll
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.util.Disposer
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.panel
import org.jetbrains.qodana.QodanaBundle
import org.jetbrains.qodana.registry.QodanaRegistry

class QodanaCloudSettingsPanel(val project: Project) :
  BoundConfigurable(QodanaBundle.message("configurable.name"), null),
  SearchableConfigurable,
  NoScroll {
  companion object {
    fun openSettings(project: Project?) = ShowSettingsUtil.getInstance().showSettingsDialog(project, QodanaCloudSettingsPanel::class.java)
  }

  private lateinit var view: QodanaCloudSettingsView

  override fun isModified(): Boolean {
    return view.viewModel.areSettingsModified
  }

  override fun apply() {
    view.viewModel.finish()
  }

  override fun getId(): String = "settings.qodana"

  override fun createPanel(): DialogPanel {
    val myDisposable = disposable
    if (myDisposable == null) {
      thisLogger().error("Can't create Qodana Settings Panel: disposable is `null`")
      return panel {}
    }
    view = QodanaCloudSettingsView(project)
    Disposer.register(myDisposable, view)
    return panel {
      row {
        cell(view.getView())
          .align(Align.FILL)
      }.resizableRow()
    }
  }
}

class FactorySettingsPanel(val project: Project) : ConfigurableProvider() {
  override fun createConfigurable(): Configurable? {
    return if (QodanaRegistry.isQodanaCloudIntegrationEnabled)
      QodanaCloudSettingsPanel(project)
    else
      null
  }

  override fun canCreateConfigurable(): Boolean {
    return QodanaRegistry.isQodanaCloudIntegrationEnabled
  }
}
