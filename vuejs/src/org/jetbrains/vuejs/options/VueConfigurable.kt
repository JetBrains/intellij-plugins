// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.options

import com.intellij.openapi.application.ApplicationBundle
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.UiDslUnnamedConfigurable
import com.intellij.openapi.project.Project
import com.intellij.ui.dsl.builder.Panel
import com.intellij.ui.dsl.builder.bind
import org.jetbrains.vuejs.VueBundle

class VueConfigurable(project: Project) : UiDslUnnamedConfigurable.Simple(), Configurable {
  private val settings = getVueSettings(project)

  override fun Panel.createContent() {
    group(VueBundle.message("vue.configurable.service.group")) {
      buttonsGroup {
        row {
          radioButton(VueBundle.message("vue.configurable.service.disabled"), VueServiceSettings.DISABLED)
          contextHelp(VueBundle.message("vue.configurable.service.disabled.help"))
        }
        row {
          radioButton(VueBundle.message("vue.configurable.service.auto"), VueServiceSettings.AUTO)
          contextHelp(VueBundle.message("vue.configurable.service.auto.help"))
        }
        row {
          radioButton(VueBundle.message("vue.configurable.service.volar"), VueServiceSettings.VOLAR)
          contextHelp(VueBundle.message("vue.configurable.service.volar.help"))
        }
        row {
          radioButton(VueBundle.message("vue.configurable.service.ts"), VueServiceSettings.TS_SERVICE)
          contextHelp(VueBundle.message("vue.configurable.service.ts.help"))
        }
      }.bind(settings::serviceType)
    }
  }



  override fun getDisplayName() = VueBundle.message("vue.configurable.title")
}