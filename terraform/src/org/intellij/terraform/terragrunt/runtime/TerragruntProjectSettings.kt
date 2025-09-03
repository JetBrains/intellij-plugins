// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.terragrunt.runtime

import com.intellij.openapi.components.*
import com.intellij.openapi.project.Project
import com.intellij.util.xmlb.XmlSerializerUtil
import org.intellij.terraform.runtime.TfToolSettings

@Service(Service.Level.PROJECT)
@State(name = "TerragruntProjectSettings", storages = [Storage("terragrunt.xml")])
internal class TerragruntProjectSettings : PersistentStateComponent<TerragruntProjectSettings>, TfToolSettings {

  @Volatile
  override var toolPath: String = ""
    set(value) {
      field = value.trim()
    }

  var isFormattedBeforeCommit: Boolean = false

  override fun getState(): TerragruntProjectSettings = this

  override fun loadState(state: TerragruntProjectSettings) {
    XmlSerializerUtil.copyBean(state, this)
  }

  companion object {
    fun getInstance(project: Project): TerragruntProjectSettings = project.service()
  }
}