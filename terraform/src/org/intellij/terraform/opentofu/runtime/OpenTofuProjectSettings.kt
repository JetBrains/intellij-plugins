// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.opentofu.runtime

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.util.xmlb.XmlSerializerUtil
import org.intellij.terraform.runtime.TfToolSettings

@Service(Service.Level.PROJECT)
@State(name = "OpenTofuProjectSettings", storages = [Storage("opentofu_settings.xml")])
internal class OpenTofuProjectSettings : PersistentStateComponent<OpenTofuProjectSettings>, TfToolSettings {

  @Volatile
  override var toolPath: String = ""
    set(value) {
      field = value.trim()
    }

  var isFormattedBeforeCommit: Boolean = false

  override fun getState(): OpenTofuProjectSettings = this

  override fun loadState(state: OpenTofuProjectSettings) {
    XmlSerializerUtil.copyBean(state, this)
  }

  companion object {
    fun getInstance(project: Project): OpenTofuProjectSettings = project.service()
  }
}