// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.runtime

import com.intellij.execution.configurations.PathEnvironmentVariableUtil
import com.intellij.openapi.components.*
import com.intellij.openapi.project.Project
import com.intellij.util.containers.SmartHashSet
import com.intellij.util.xmlb.XmlSerializerUtil
import com.intellij.util.xmlb.annotations.Attribute
import org.intellij.terraform.install.getBinaryName

@Service(Service.Level.PROJECT)
@State(name = "TerraformProjectSettings", storages = [Storage("terraform.xml")])
class TerraformProjectSettings : PersistentStateComponent<TerraformProjectSettings> {
  @Attribute
  private var ignoredTemplateCandidatePaths: MutableSet<String> = SmartHashSet()

  @Volatile
  var terraformPath: String = getDefaultTerraformPath()
    set(value) {
      field = value.trim()
    }

  var isFormattedBeforeCommit: Boolean = false

  val actualTerraformPath: String
    get() = terraformPath.ifEmpty { getDefaultTerraformPath() }

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

    fun getDefaultTerraformPath(): String {
      val executorFileName = getBinaryName()
      val terraform = PathEnvironmentVariableUtil.findInPath(executorFileName)

      if (terraform != null && terraform.canExecute()) {
        return terraform.absolutePath
      }

      return executorFileName
    }
  }
}