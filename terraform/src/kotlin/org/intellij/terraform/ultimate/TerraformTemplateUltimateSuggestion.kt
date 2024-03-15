// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.ultimate

import com.intellij.ide.IdeBundle
import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.updateSettings.impl.pluginsAdvertisement.FUSEventSource
import com.intellij.openapi.updateSettings.impl.pluginsAdvertisement.SuggestedIde
import com.intellij.openapi.updateSettings.impl.pluginsAdvertisement.createTryUltimateActionLabel
import com.intellij.ui.EditorNotificationPanel
import com.intellij.ui.EditorNotifications
import org.intellij.terraform.hcl.HCLBundle
import java.util.function.Function

internal class TerraformTemplateUltimateSuggestion(private val project: Project,
                                                   private val suggestedIde: SuggestedIde) : Function<FileEditor, EditorNotificationPanel?> {
  override fun apply(fileEditor: FileEditor): EditorNotificationPanel {
    val panel = EditorNotificationPanel(fileEditor, EditorNotificationPanel.Status.Promo)
    panel.text = IdeBundle.message("plugins.advertiser.extensions.supported.in.ultimate", HCLBundle.message("tftpl.file.presentable.name"), suggestedIde.name)
    panel.createTryUltimateActionLabel(suggestedIde, project, PluginId.getId(TERRAFORM_TEMPLATE_PLUGIN_ID))

    panel.createActionLabel(IdeBundle.message("plugins.advertiser.action.ignore.ultimate")) {
      FUSEventSource.EDITOR.logIgnoreExtension(project)
      dismissPluginSuggestion()
      EditorNotifications.getInstance(project).updateAllNotifications()
    }

    return panel
  }

  private fun dismissPluginSuggestion() {
    PropertiesComponent.getInstance().setValue(TERRAFORM_TEMPLATE_SUGGESTION_DISMISSED_KEY, true)
  }
}

internal const val TERRAFORM_TEMPLATE_SUGGESTION_DISMISSED_KEY: String = "terraform.template.suggestion.dismissed"
internal const val TERRAFORM_TEMPLATE_PLUGIN_ID: String = "org.intellij.plugins.hcl"
internal const val TERRAFORM_TEMPLATE_EXTENSION: String = "tftpl"