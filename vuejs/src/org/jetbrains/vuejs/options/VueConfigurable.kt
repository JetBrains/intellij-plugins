// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
@file:Suppress("DialogTitleCapitalization")

package org.jetbrains.vuejs.options

import com.intellij.lang.typescript.lsp.bind
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.UiDslUnnamedConfigurable
import com.intellij.openapi.project.Project
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.Panel
import com.intellij.ui.dsl.builder.bind
import org.jetbrains.vuejs.VueBundle
import org.jetbrains.vuejs.lang.typescript.service.volar.VolarExecutableDownloader

class VueConfigurable(private val project: Project) : UiDslUnnamedConfigurable.Simple(), Configurable {
  private val settings = getVueSettings(project)

  override fun Panel.createContent() {
    group(VueBundle.message("vue.configurable.service.group")) {
      row(VueBundle.message("vue.configurable.service.volar.package")) {
        cell(VolarExecutableDownloader.createNodePackageField(project))
          .align(AlignX.FILL)
          .bind(settings::packageRef)
      }

      buttonsGroup {
        row {
          radioButton(VueBundle.message("vue.configurable.service.disabled"), VueServiceSettings.DISABLED)
            .comment(VueBundle.message("vue.configurable.service.disabled.help"))
        }
        row {
          radioButton(VueBundle.message("vue.configurable.service.auto"), VueServiceSettings.AUTO)
            .comment(VueBundle.message("vue.configurable.service.auto.help"))
        }
        row {
          radioButton(VueBundle.message("vue.configurable.service.volar"), VueServiceSettings.VOLAR)
            .comment(VueBundle.message("vue.configurable.service.volar.help"))
        }
        row {
          radioButton(VueBundle.message("vue.configurable.service.ts"), VueServiceSettings.TS_SERVICE)
            .comment(VueBundle.message("vue.configurable.service.ts.help"))
        }
      }.bind(settings::serviceType)
    }
  }

  override fun getHelpTopic(): String = "settings.vue"

  override fun getDisplayName() = VueBundle.message("vue.configurable.title")
}