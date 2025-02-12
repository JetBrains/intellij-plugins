// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
@file:Suppress("DialogTitleCapitalization")

package org.jetbrains.vuejs.options

import com.intellij.icons.AllIcons
import com.intellij.lang.javascript.JavaScriptBundle
import com.intellij.lang.typescript.lsp.bind
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.UiDslUnnamedConfigurable
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBRadioButton
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.layout.not
import org.jetbrains.vuejs.VueBundle
import org.jetbrains.vuejs.lang.typescript.service.VueLspServerLoader

class VueConfigurable(private val project: Project) : UiDslUnnamedConfigurable.Simple(), Configurable {
  private val settings = getVueSettings(project)

  override fun Panel.createContent() {
    group(VueBundle.message("vue.configurable.service.group")) {
      row(VueBundle.message("vue.configurable.service.languageServerPackage")) {
        cell(VueLspServerLoader.createNodePackageField(project))
          .align(AlignX.FILL)
          .bind(settings::packageRef)
      }

      lateinit var radioButtonDisabled: Cell<JBRadioButton>
      buttonsGroup {
        row {
          radioButton(VueBundle.message("vue.configurable.service.disabled"), VueServiceSettings.DISABLED)
            .comment(VueBundle.message("vue.configurable.service.disabled.help"))
            .also { radioButtonDisabled = it }
        }
        row {
          radioButton(VueBundle.message("vue.configurable.service.auto"), VueServiceSettings.AUTO)
            .comment(VueBundle.message("vue.configurable.service.auto.help"))
        }
        row {
          radioButton(VueBundle.message("vue.configurable.service.ts"), VueServiceSettings.TS_SERVICE)
            .comment(VueBundle.message("vue.configurable.service.ts.help"))
        }
      }.bind(settings::serviceType)

      separator()

      row {
        checkBox(JavaScriptBundle.message("typescript.compiler.configurable.options.use.servicePoweredTypeEngine"))
          .applyToComponent {
            toolTipText = JavaScriptBundle.message("typescript.compiler.configurable.options.use.servicePoweredTypeEngine.comment")
          }
          .bindSelected(settings::useTypesFromServer)
          .gap(RightGap.SMALL)
        icon(AllIcons.General.Alpha)
      }.enabledIf(radioButtonDisabled.selected.not())
    }
  }

  override fun getHelpTopic(): String = "settings.vue"

  override fun getDisplayName(): String = VueBundle.message("vue.configurable.title")
}