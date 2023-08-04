// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
@file:Suppress("DialogTitleCapitalization")

package org.jetbrains.vuejs.options

import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterManager
import com.intellij.javascript.nodejs.util.NodePackageField
import com.intellij.lang.typescript.lsp.JSExternalDefinitionsNodeDescriptor
import com.intellij.lang.typescript.lsp.JSExternalDefinitionsPackageResolver
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.UiDslUnnamedConfigurable
import com.intellij.openapi.project.Project
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.Panel
import com.intellij.ui.dsl.builder.bind
import com.intellij.ui.dsl.builder.toMutableProperty
import org.jetbrains.vuejs.VueBundle
import org.jetbrains.vuejs.lang.typescript.service.volar.volarLspServerPackageDescriptor

class VueConfigurable(private val project: Project) : UiDslUnnamedConfigurable.Simple(), Configurable {
  private val settings = getVueSettings(project)

  override fun Panel.createContent() {
    group(VueBundle.message("vue.configurable.service.group")) {
      row(VueBundle.message("vue.configurable.service.volar.package")) {
        val volarNodeDescriptor = JSExternalDefinitionsNodeDescriptor(volarLspServerPackageDescriptor.serverPackageName)
        val packageField = NodePackageField(project,
                                            volarNodeDescriptor,
                                            { NodeJsInterpreterManager.getInstance(project).interpreter },
                                            JSExternalDefinitionsPackageResolver(project, volarNodeDescriptor))

        cell(packageField)
          .align(AlignX.FILL)
          .bind({ it.selectedRef },
                { nodePackageField, nodePackageRef -> nodePackageField.selectedRef = nodePackageRef },
                settings::packageRef.toMutableProperty())
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


  override fun getDisplayName() = VueBundle.message("vue.configurable.title")
}