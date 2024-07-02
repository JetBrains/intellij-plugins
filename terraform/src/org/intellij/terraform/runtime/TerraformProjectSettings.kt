// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.runtime

import com.intellij.execution.configurations.PathEnvironmentVariableUtil
import com.intellij.execution.wsl.WslPath
import com.intellij.openapi.components.*
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.util.containers.SmartHashSet
import com.intellij.util.xmlb.XmlSerializerUtil
import com.intellij.util.xmlb.annotations.Attribute
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runInterruptible
import kotlinx.coroutines.withContext
import org.intellij.terraform.install.getBinaryName
import kotlin.text.ifEmpty

@Service(Service.Level.PROJECT)
@State(name = "TerraformProjectSettings", storages = [Storage("terraform.xml")])
class TerraformProjectSettings : PersistentStateComponent<TerraformProjectSettings> {
  @Attribute
  private var ignoredTemplateCandidatePaths: MutableSet<String> = SmartHashSet()

  @Volatile
  var terraformPath: String = ""
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
internal class TerraformPathDetector(private val project: Project, private val coroutineScope: CoroutineScope) {

  companion object {
    fun getInstance(project: Project): TerraformPathDetector = project.service()
  }

  var detectedPath: String? = null
    private set

  val actualTerraformPath: String
    get() = TerraformProjectSettings.getInstance(project).terraformPath.ifEmpty { detectedPath ?: getBinaryName() }

  suspend fun detect(): Boolean {
    return withContext(Dispatchers.IO) {
      runInterruptible {
        val projectFilePath = project.projectFilePath
        if (projectFilePath != null) {
          val wslDistribution = WslPath.getDistributionByWindowsUncPath(projectFilePath)
          if (wslDistribution != null) {
            try {
              val out = wslDistribution.executeOnWsl(3000, "which", "terraform")
              if (out.exitCode == 0) {
                detectedPath = wslDistribution.getWindowsPath(out.stdout.trim())
                return@runInterruptible true
              }
            }
            catch (e: Exception) {
              logger<TerraformPathDetector>().warn(e)
            }
          }
        }

        val terraform = PathEnvironmentVariableUtil.findInPath(getBinaryName())
        if (terraform != null && terraform.canExecute()) {
          detectedPath = terraform.absolutePath
          return@runInterruptible true
        }
        return@runInterruptible false
      }
    }
  }

  class DetectOnStart : ProjectActivity {
    override suspend fun execute(project: Project) {
      project.serviceAsync<TerraformPathDetector>().detect()
    }
  }

}