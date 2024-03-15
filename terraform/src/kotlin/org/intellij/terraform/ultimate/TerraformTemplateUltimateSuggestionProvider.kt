// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.ultimate

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.application.impl.ApplicationInfoImpl
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.updateSettings.impl.pluginsAdvertisement.PluginAdvertiserService
import com.intellij.openapi.updateSettings.impl.pluginsAdvertisement.PluginSuggestionProvider
import com.intellij.openapi.updateSettings.impl.pluginsAdvertisement.SuggestedIde
import com.intellij.openapi.updateSettings.impl.pluginsAdvertisement.tryUltimateIsDisabled
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.EditorNotificationPanel
import java.util.function.Function

class TerraformTemplateUltimateSuggestionProvider : PluginSuggestionProvider {
  override fun getSuggestion(project: Project, file: VirtualFile): Function<FileEditor, EditorNotificationPanel?>? {
    if (file.extension != TERRAFORM_TEMPLATE_EXTENSION
        || !PluginAdvertiserService.isCommunityIde()
        || isPluginSuggestionDismissed()
        || tryUltimateIsDisabled()) {
      return null
    }
    val suggestedIde = suggestUltimateIde() ?: return null
    return TerraformTemplateUltimateSuggestion(project, suggestedIde)
  }

  private fun isPluginSuggestionDismissed(): Boolean {
    return PropertiesComponent.getInstance().isTrueValue(TERRAFORM_TEMPLATE_SUGGESTION_DISMISSED_KEY)
  }

  private fun suggestUltimateIde(): SuggestedIde? {
    val thisProductCode = ApplicationInfoImpl.getShadowInstanceImpl().build.productCode
    val suggestedIdeCode = PluginAdvertiserService.getSuggestedCommercialIdeCode(thisProductCode)
    return PluginAdvertiserService.getIde(suggestedIdeCode)
  }
}

