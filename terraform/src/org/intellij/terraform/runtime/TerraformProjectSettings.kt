// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.runtime

import com.intellij.openapi.components.*
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.openapi.util.io.FileUtilRt
import com.intellij.openapi.vfs.AsyncFileListener
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import com.intellij.util.containers.SmartHashSet
import com.intellij.util.xmlb.XmlSerializerUtil
import com.intellij.util.xmlb.annotations.Attribute
import org.intellij.terraform.config.TerraformFileType
import org.intellij.terraform.hasHCLLanguageFiles
import org.intellij.terraform.install.TfToolType

@Service(Service.Level.PROJECT)
@State(name = "TerraformProjectSettings", storages = [Storage("terraform.xml")])
class TerraformProjectSettings : PersistentStateComponent<TerraformProjectSettings>, TfToolSettings {
  @Attribute
  private var ignoredTemplateCandidatePaths: MutableSet<String> = SmartHashSet()

  @Volatile
  override var toolPath: String = ""
    set(value) {
      field = value.trim()
    }

  var isFormattedBeforeCommit: Boolean = false

  fun addIgnoredTemplateCandidate(filePath: String) {
    ignoredTemplateCandidatePaths.add(filePath)
  }

  fun isIgnoredTemplateCandidate(filePath: String): Boolean = ignoredTemplateCandidatePaths.contains(filePath)

  override fun getState(): TerraformProjectSettings = this

  override fun loadState(state: TerraformProjectSettings) {
    XmlSerializerUtil.copyBean(state, this)
  }

  companion object {
    fun getInstance(project: Project): TerraformProjectSettings = project.service()
  }

  internal class DetectOnStart : ProjectActivity {
    override suspend fun execute(project: Project) {
      val settings = project.serviceAsync<TerraformProjectSettings>()
      if (settings.toolPath.isEmpty() && hasHCLLanguageFiles(project, TerraformFileType)) {
        project.serviceAsync<ToolPathDetector>().detectPathAndUpdateSettingsAsync(settings, TfToolType.TERRAFORM.executableName)
      }
    }
  }

  internal class TerraformFileListener : AsyncFileListener {
    override fun prepareChange(events: List<VFileEvent>): AsyncFileListener.ChangeApplier? {
      val fileEvents = events.filter { (FileUtilRt.extensionEquals(it.path, TerraformFileType.DEFAULT_EXTENSION)) }
      if (fileEvents.isEmpty()) return null
      return SettingsUpdater(fileEvents, TfToolType.TERRAFORM.executableName, TerraformProjectSettings::class.java)
    }
  }


}