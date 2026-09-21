// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
@file:Suppress("DialogTitleCapitalization")

package org.jetbrains.vuejs.options

import com.intellij.lang.javascript.JavaScriptBundle
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.UiDslUnnamedConfigurable
import com.intellij.openapi.project.Project
import com.intellij.ui.dsl.builder.Panel
import com.intellij.ui.dsl.builder.bind
import com.intellij.ui.dsl.builder.bindSelected
import com.intellij.ui.dsl.builder.selected
import com.intellij.ui.layout.ComponentPredicate
import org.jetbrains.vuejs.VueBundle

class VueConfigurable(private val project: Project) : UiDslUnnamedConfigurable.Simple(), Configurable {
  private val settings = VueSettings.instance(project)

  override fun Panel.createContent() {
    group(VueBundle.message("vue.configurable.service.group")) {
      lateinit var autoModeSelected: ComponentPredicate

      buttonsGroup {
        row {
          radioButton(VueBundle.message("vue.configurable.service.disabled"), VueLSMode.DISABLED)
            .comment(VueBundle.message("vue.configurable.service.disabled.help"))
        }
        row {
          radioButton(VueBundle.message("vue.configurable.service.auto"), VueLSMode.AUTO)
            .comment(VueBundle.message("vue.configurable.service.auto.help"))
            .also { autoModeSelected = it.selected }
        }
      }.bind(settings::serviceType)

      separator()

      row {
        checkBox(JavaScriptBundle.message("typescript.compiler.configurable.options.use.servicePoweredTypeEngine"))
          .applyToComponent {
            toolTipText = JavaScriptBundle.message("typescript.compiler.configurable.options.use.servicePoweredTypeEngine.comment")
          }
          .enabledIf(autoModeSelected)
          .bindSelected(
            { settings.useTypesFromServer },
            { settings.useServicePoweredTypesManualOverride = it }
          )
      }
    }

    /**
     * We don't restart services in [VueSettings]; the configurable restarts them on apply instead.
     */
    var lastAppliedState = settings.state

    onApply {
      val current = settings.state
      if (current != lastAppliedState) {
        restartVueServicesAsync(project)
      }
      lastAppliedState = current
    }
    onReset {
      lastAppliedState = settings.state
    }
  }

  override fun getHelpTopic(): String = "settings.vue"

  override fun getDisplayName(): String = VueBundle.message("vue.configurable.title")
}