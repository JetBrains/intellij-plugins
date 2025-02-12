// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
@file:Suppress("DialogTitleCapitalization")

package org.angular2.options

import com.intellij.lang.javascript.JavaScriptBundle
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.UiDslUnnamedConfigurable
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBRadioButton
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.layout.not
import org.angular2.lang.Angular2Bundle
import org.jetbrains.annotations.Nls

class AngularConfigurable(project: Project) : UiDslUnnamedConfigurable.Simple(), Configurable, Configurable.Beta {
  private val settings = getAngularSettings(project)

  override fun Panel.createContent() {
    group(Angular2Bundle.message("angular.configurable.service.group")) {
      lateinit var rbDisabled: Cell<JBRadioButton>

      buttonsGroup {
        row {
          rbDisabled = radioButton(Angular2Bundle.message("angular.configurable.service.disabled"), AngularServiceSettings.DISABLED)
            .comment(Angular2Bundle.message("angular.configurable.service.disabled.help"))
        }
        row {
          radioButton(Angular2Bundle.message("angular.configurable.service.auto"), AngularServiceSettings.AUTO)
            .comment(Angular2Bundle.message("angular.configurable.service.auto.help"))
        }
      }.bind(settings::serviceType)

      separator()

      row {
        checkBox(JavaScriptBundle.message("typescript.compiler.configurable.options.use.servicePoweredTypeEngine"))
          .applyToComponent {
            toolTipText = JavaScriptBundle.message("typescript.compiler.configurable.options.use.servicePoweredTypeEngine.comment")
          }
          .enabledIf(rbDisabled.selected.not())
          .bindSelected(settings::useTypesFromServer)
      }
    }
  }

  override fun getHelpTopic(): String = "settings.angular"

  override fun getDisplayName(): @Nls String = Angular2Bundle.message("angular.configurable.name.angular.template")
}