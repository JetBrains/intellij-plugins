// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.service.settings

import com.intellij.codeInsight.template.impl.TemplateEditorUtil
import com.intellij.json.JsonLanguage
import com.intellij.lang.typescript.lsp.bind
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.platform.lsp.api.LspServerManager
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFileFactory
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.layout.ComponentPredicate
import com.intellij.util.ui.JBDimension
import javax.swing.JComponent
import org.jetbrains.astro.AstroBundle
import org.jetbrains.astro.service.AstroLspServerLoader
import org.jetbrains.astro.service.AstroLspServerSupportProvider

class AstroServiceConfigurable(private val project: Project) : Configurable {
  private val settings = getAstroServiceSettings(project)
  private lateinit var panel: DialogPanel
  private lateinit var workspaceConfigurationEditor: Editor

  private val currentWorkspaceConfiguration: String
    get() = workspaceConfigurationEditor.document.text.trim()

  override fun getDisplayName(): String = AstroBundle.message("astro.service.configurable.title")

  override fun getHelpTopic(): String = "settings.astroservice"

  override fun createComponent(): JComponent {
    val jsonFile = PsiFileFactory.getInstance(project).createFileFromText("dummy.json", JsonLanguage.INSTANCE, "", true, false)
    val jsonDocument = PsiDocumentManager.getInstance(project).getDocument(jsonFile)

    workspaceConfigurationEditor = TemplateEditorUtil.createEditor(false, jsonDocument, project)
    workspaceConfigurationEditor.settings.additionalLinesCount = 0
    workspaceConfigurationEditor.component.preferredSize = JBDimension(200, 100)

    panel = panel {
      group(AstroBundle.message("astro.service.configurable.service.group")) {
        row(AstroBundle.message("astro.service.configurable.service.languageServerPackage")) {
          cell(AstroLspServerLoader.createNodePackageField(project))
            .align(AlignX.FILL)
            .bind(settings::lspServerPackageRef)
        }

        row(AstroBundle.message("astro.service.configurable.service.tsPluginPackage")) {
          cell(org.jetbrains.astro.service.AstroTSPluginLoader.createNodePackageField(project))
            .align(AlignX.FILL)
            .bind(settings::tsPluginPackageRef)
        }

        buttonsGroup {
          row {
            radioButton(AstroBundle.message("astro.service.configurable.service.disabled"), AstroServiceMode.DISABLED)
              .comment(AstroBundle.message("astro.service.configurable.service.disabled.help"))
          }
          row {
            radioButton(AstroBundle.message("astro.service.configurable.service.lsp"), AstroServiceMode.ENABLED)
              .comment(AstroBundle.message("astro.service.configurable.service.lsp.help"))
          }
        }.apply {
          bind(settings::serviceMode)
        }
      }

      row {
        label(AstroBundle.message("astro.service.configurable.configuration.label"))
          .resizableColumn()

        comment(AstroBundle.message("astro.service.configurable.restore.default.configuration.link")) {
          WriteAction.run<Throwable> { workspaceConfigurationEditor.document.setText(AstroServiceSettings.DEFAULT_WORKSPACE_CONFIGURATION) }
        }
          .align(AlignY.BOTTOM)
          .visibleIf(object : ComponentPredicate() {
            override fun addListener(listener: (Boolean) -> Unit) {
              workspaceConfigurationEditor.document.addDocumentListener(object : DocumentListener {
                override fun documentChanged(event: DocumentEvent) {
                  listener(currentWorkspaceConfiguration != AstroServiceSettings.DEFAULT_WORKSPACE_CONFIGURATION)
                }
              })
            }

            override fun invoke(): Boolean = currentWorkspaceConfiguration != AstroServiceSettings.DEFAULT_WORKSPACE_CONFIGURATION
          })

        comment(AstroBundle.message("astro.service.configurable.available.options.comment"),
                action = HyperlinkEventAction.HTML_HYPERLINK_INSTANCE)
      }

      row { cell(workspaceConfigurationEditor.component).align(Align.FILL) }.resizableRow()
    }

    return panel
  }

  override fun reset() {
    panel.reset()
    WriteAction.run<Throwable> { workspaceConfigurationEditor.document.setText(settings.getWorkspaceConfiguration()) }
  }

  override fun isModified(): Boolean = panel.isModified() || settings.getWorkspaceConfiguration() != currentWorkspaceConfiguration

  override fun apply() {
    panel.apply()
    settings.setWorkspaceConfiguration(currentWorkspaceConfiguration)
    if (!project.isDefault) {
      LspServerManager.getInstance(project).stopAndRestartIfNeeded(AstroLspServerSupportProvider::class.java)
    }
  }

  override fun disposeUIResources() {
    if (this::workspaceConfigurationEditor.isInitialized) {
      EditorFactory.getInstance().releaseEditor(workspaceConfigurationEditor)
    }
  }
}