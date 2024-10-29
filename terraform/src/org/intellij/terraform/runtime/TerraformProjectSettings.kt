// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.runtime

import com.intellij.openapi.components.*
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.util.containers.SmartHashSet
import com.intellij.util.xmlb.XmlSerializerUtil
import com.intellij.util.xmlb.annotations.Attribute
import kotlinx.coroutines.CoroutineScope
import org.intellij.terraform.install.TfToolType

@Service(Service.Level.PROJECT)
@State(name = "TerraformProjectSettings", storages = [Storage("terraform.xml")])
class TerraformProjectSettings : PersistentStateComponent<TerraformProjectSettings>, ToolSettings {
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
}

@Service(Service.Level.PROJECT)
internal class TerraformPathDetector(project: Project, coroutineScope: CoroutineScope): ToolPathDetectorBase(project, coroutineScope, TfToolType.TERRAFORM)  {

  companion object {
    fun getInstance(project: Project): TerraformPathDetector = project.service<TerraformPathDetector>()
  }

  override fun actualPath(): String {
    return TerraformProjectSettings.getInstance(project).toolPath.ifEmpty { detectedPath() ?: toolType.getBinaryName() }
  }

  class DetectOnStart : ProjectActivity {
    override suspend fun execute(project: Project) {
      project.serviceAsync<TerraformPathDetector>().detect()
    }
  }

}