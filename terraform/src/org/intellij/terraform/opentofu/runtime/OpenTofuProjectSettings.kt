// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.opentofu.runtime

import com.intellij.openapi.components.*
import com.intellij.openapi.project.Project
import com.intellij.util.containers.SmartHashSet
import com.intellij.util.xmlb.XmlSerializerUtil
import com.intellij.util.xmlb.annotations.Attribute
import org.intellij.terraform.runtime.TfToolSettings

@Service(Service.Level.PROJECT)
@State(name = "OpenTofuProjectSettings", storages = [Storage("opentofu_settings.xml")])
internal class OpenTofuProjectSettings : PersistentStateComponent<OpenTofuProjectSettings>, TfToolSettings {
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