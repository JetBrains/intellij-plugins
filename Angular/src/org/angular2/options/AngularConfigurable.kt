// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
@file:Suppress("DialogTitleCapitalization")

package org.angular2.options

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.UiDslUnnamedConfigurable
import com.intellij.openapi.project.Project
import com.intellij.ui.dsl.builder.Panel
import com.intellij.ui.dsl.builder.bind
import org.angular2.lang.Angular2Bundle

class AngularConfigurable(project: Project) : UiDslUnnamedConfigurable.Simple(), Configurable {
  private val settings = getAngularSettings(project)

  override fun Panel.createContent() {
    group(Angular2Bundle.message("angular.configurable.service.group")) {
      buttonsGroup {
        row {
          radioButton(Angular2Bundle.message("angular.configurable.service.disabled"), AngularServiceSettings.DISABLED)
            .comment(Angular2Bundle.message("angular.configurable.service.disabled.help"))
        }
        row {
          radioButton(Angular2Bundle.message("angular.configurable.service.auto"), AngularServiceSettings.AUTO)
            .comment(Angular2Bundle.message("angular.configurable.service.auto.help"))
        }
      }.bind(settings::serviceType)
    }
  }

  override fun getHelpTopic(): String = "settings.angular"

  override fun getDisplayName() = Angular2Bundle.message("angular.configurable.name.angular.template")
}