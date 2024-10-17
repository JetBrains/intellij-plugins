// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.opentofu.runtime

import com.intellij.execution.configurations.PathEnvironmentVariableUtil
import com.intellij.execution.wsl.WslPath
import com.intellij.openapi.components.*
import com.intellij.openapi.diagnostic.fileLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.util.containers.SmartHashSet
import com.intellij.util.xmlb.XmlSerializerUtil
import com.intellij.util.xmlb.annotations.Attribute
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runInterruptible
import kotlinx.coroutines.withContext
import org.intellij.terraform.install.TFToolType
import org.intellij.terraform.runtime.ToolPathDetector
import org.intellij.terraform.runtime.ToolPathDetectorBase
import org.intellij.terraform.runtime.ToolSettings

@Service(Service.Level.PROJECT)
@State(name = "OpenTofuProjectSettings", storages = [Storage("opentofu_settings.xml")])
internal class OpenTofuProjectSettings : PersistentStateComponent<OpenTofuProjectSettings>, ToolSettings {
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

  override fun getState(): OpenTofuProjectSettings = this

  override fun loadState(state: OpenTofuProjectSettings) {
    XmlSerializerUtil.copyBean(state, this)
  }

  companion object {
    fun getInstance(project: Project): OpenTofuProjectSettings = project.service()
  }
}

@Service(Service.Level.PROJECT)
internal class OpenTofuPathDetector(project: Project, coroutineScope: CoroutineScope): ToolPathDetectorBase(project, coroutineScope, TFToolType.OPENTOFU) {

  companion object {
    fun getInstance(project: Project): OpenTofuPathDetector = project.service<OpenTofuPathDetector>()
  }

  override val actualPath: String = OpenTofuProjectSettings.getInstance(project).toolPath.ifEmpty { detectedPath() ?: toolType.getBinaryName() }

  class DetectOnStart : ProjectActivity {
    override suspend fun execute(project: Project) {
      project.serviceAsync<OpenTofuPathDetector>().detect()
    }
  }

}