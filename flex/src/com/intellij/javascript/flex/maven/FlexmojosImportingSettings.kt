// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.javascript.flex.maven

import com.intellij.lang.javascript.flex.FlexBundle
import com.intellij.lang.javascript.flex.build.FlexCompilerProjectConfiguration
import com.intellij.openapi.options.UiDslUnnamedConfigurable
import com.intellij.openapi.options.UnnamedConfigurable
import com.intellij.openapi.project.Project
import com.intellij.ui.dsl.builder.Panel
import com.intellij.ui.dsl.builder.bindSelected
import org.jetbrains.idea.maven.project.AdditionalMavenImportingSettings

internal class FlexmojosImportingSettings : AdditionalMavenImportingSettings {

  override fun createConfigurable(project: Project): UnnamedConfigurable = FlexmojosImportingConfigurable(project)

  private class FlexmojosImportingConfigurable(project: Project) : UiDslUnnamedConfigurable.Simple() {

    private val config = FlexCompilerProjectConfiguration.getInstance(project)

    override fun Panel.createContent() {
      group(FlexBundle.message("flexmojos.settings.title")) {
        row {
          checkBox(FlexBundle.message("flexmojos.settings.generate.configs"))
            .bindSelected(config::GENERATE_FLEXMOJOS_CONFIGS)
        }
      }
    }
  }
}
