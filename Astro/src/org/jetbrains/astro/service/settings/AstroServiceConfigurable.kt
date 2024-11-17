// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.service.settings

import com.intellij.lang.typescript.lsp.bind
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.UiDslUnnamedConfigurable
import com.intellij.openapi.project.Project
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.Panel
import com.intellij.ui.dsl.builder.bind
import org.jetbrains.astro.AstroBundle
import org.jetbrains.astro.service.AstroLspServerLoader

class AstroServiceConfigurable(val project: Project) : UiDslUnnamedConfigurable.Simple(), Configurable {
  private val settings = getAstroServiceSettings(project)

  override fun Panel.createContent() {
    group(AstroBundle.message("astro.service.configurable.service.group")) {
      row(AstroBundle.message("astro.service.configurable.service.languageServerPackage")) {
        cell(AstroLspServerLoader.createNodePackageField(project))
          .align(AlignX.FILL)
          .bind(settings::lspServerPackageRef)
      }

      buttonsGroup {
        row {
          radioButton(AstroBundle.message("astro.service.configurable.service.disabled"), AstroServiceMode.DISABLED)
            .comment(AstroBundle.message("astro.service.configurable.service.disabled.help"))
        }
        row {
          radioButton(AstroBundle.message("astro.service.configurable.service.lsp"), AstroServiceMode.ENABLED)
            .comment(AstroBundle.message("astro.service.configurable.service.lsp.help"))
        }
      }.apply {
        bind(settings::serviceMode)
      }
    }
  }

  override fun getDisplayName() = AstroBundle.message("astro.service.configurable.title")

  override fun getHelpTopic() = "settings.astroservice"
}