// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.prisma.ide.ui

import com.intellij.lang.typescript.lsp.bind
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.UiDslUnnamedConfigurable
import com.intellij.openapi.project.Project
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.Panel
import com.intellij.ui.dsl.builder.bind
import org.intellij.prisma.PrismaBundle
import org.intellij.prisma.ide.lsp.PrismaLspServerLoader
import org.intellij.prisma.ide.lsp.PrismaServiceMode
import org.intellij.prisma.ide.lsp.PrismaServiceSettings

class PrismaSettingsConfigurable(val project: Project) : UiDslUnnamedConfigurable.Simple(), Configurable {
  private val settings = PrismaServiceSettings.getInstance(project)

  override fun Panel.createContent() {
    group(PrismaBundle.message("prisma.settings.service.configurable.service.group")) {
      row(PrismaBundle.message("prisma.settings.service.configurable.service.languageServerPackage")) {
        cell(PrismaLspServerLoader.createNodePackageField(project))
          .align(AlignX.FILL)
          .bind(settings::lspServerPackageRef)
      }

      buttonsGroup {
        row {
          radioButton(PrismaBundle.message("prisma.settings.service.configurable.service.disabled"), PrismaServiceMode.DISABLED)
            .comment(PrismaBundle.message("prisma.settings.service.configurable.service.disabled.help"))
        }
        row {
          radioButton(PrismaBundle.message("prisma.settings.service.configurable.service.lsp"), PrismaServiceMode.ENABLED)
            .comment(PrismaBundle.message("prisma.settings.service.configurable.service.lsp.help"))
        }
      }.apply {
        bind(settings::serviceMode)
      }
    }
  }

  override fun getDisplayName() = PrismaBundle.message("prisma.settings.configurable.title")
}