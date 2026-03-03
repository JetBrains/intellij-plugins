// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
@file:Suppress("DialogTitleCapitalization")

package org.jetbrains.vuejs.options

import com.intellij.lang.typescript.lsp.bind
import com.intellij.lang.typescript.lsp.createBundledNodePackageField
import com.intellij.lang.typescript.lsp.createNodePackageField
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.UiDslUnnamedConfigurable
import com.intellij.openapi.project.Project
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.Panel
import com.intellij.ui.dsl.builder.SegmentedButton
import com.intellij.ui.dsl.builder.bind
import com.intellij.ui.dsl.builder.selected
import com.intellij.ui.dsl.builder.toMutableProperty
import com.intellij.ui.layout.ComponentPredicate
import org.jetbrains.vuejs.VueBundle
import org.jetbrains.vuejs.lang.typescript.service.getVueTSPluginNodePackages
import org.jetbrains.vuejs.lang.typescript.service.lsp.VueLspServerLoader

class VueConfigurable(private val project: Project) : UiDslUnnamedConfigurable.Simple(), Configurable {
  private val settings = VueSettings.instance(project)

  override fun Panel.createContent() {
    group(VueBundle.message("vue.configurable.service.group")) {
      lateinit var manualModeSelected: ComponentPredicate

      buttonsGroup {
        row {
          radioButton(VueBundle.message("vue.configurable.service.disabled"), VueLSMode.DISABLED)
            .comment(VueBundle.message("vue.configurable.service.disabled.help"))
        }
        row {
          radioButton(VueBundle.message("vue.configurable.service.auto"), VueLSMode.AUTO)
            .comment(VueBundle.message("vue.configurable.service.auto.help"))
        }
        row {
          radioButton(VueBundle.message("vue.configurable.service.manual"), VueLSMode.MANUAL)
            .comment(VueBundle.message("vue.configurable.service.manual.help"))
            .also { manualModeSelected = it.selected }
        }

        indent {
          lateinit var mode: SegmentedButton<VueSettings.ManualMode>

          row {
            mode = segmentedButton(
              items = VueSettings.ManualMode.entries,
              renderer = {
                text = it.displayName
              },
            ).bind(settings.manualSettings::mode.toMutableProperty())
          }.apply {
            rowComment(VueBundle.message("vue.configurable.service.manual.description"))
          }

          row(VueBundle.message("vue.configurable.service.languageServerPackage")) {
            cell(
              createNodePackageField(project, VueLspServerLoader.packageDescriptor)
            ).align(AlignX.FILL)
              .bind(settings.manualSettings::lspServerPackageRef)
          }.visibleIf(mode.isSelected(
            VueSettings.ManualMode.ONLY_LSP_SERVER,
          ))

          row(VueBundle.message("vue.configurable.service.typescriptPluginPackage")) {
            cell(
              createBundledNodePackageField(
                project = project,
                packages = getVueTSPluginNodePackages(project),
              )
            ).align(AlignX.FILL)
              .bind(settings.manualSettings::tsPluginPackageRef)
          }.visibleIf(mode.isSelected(
            VueSettings.ManualMode.ONLY_TS_PLUGIN,
          ))

        }.visibleIf(manualModeSelected)

      }.bind(settings::serviceType)
    }
  }

  override fun getHelpTopic(): String = "settings.vue"

  override fun getDisplayName(): String = VueBundle.message("vue.configurable.title")
}

private fun SegmentedButton<VueSettings.ManualMode>.isSelected(
  vararg modes: VueSettings.ManualMode,
): ComponentPredicate {
  return object : ComponentPredicate() {
    override fun invoke(): Boolean {
      return modes.contains(selectedItem)
    }

    override fun addListener(listener: (Boolean) -> Unit) {
      whenItemSelected { listener(invoke()) }
    }
  }
}