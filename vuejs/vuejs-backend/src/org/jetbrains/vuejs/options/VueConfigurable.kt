// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
@file:Suppress("DialogTitleCapitalization")

package org.jetbrains.vuejs.options

import com.intellij.lang.typescript.lsp.bind
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.UiDslUnnamedConfigurable
import com.intellij.openapi.project.Project
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.dsl.listCellRenderer.textListCellRenderer
import com.intellij.ui.layout.ValueComponentPredicate
import com.intellij.ui.layout.not
import org.jetbrains.vuejs.VueBundle
import org.jetbrains.vuejs.lang.typescript.service.lsp.VueLspServerLoader

class VueConfigurable(private val project: Project) : UiDslUnnamedConfigurable.Simple(), Configurable {
  private val settings = getVueSettings(project)

  override fun Panel.createContent() {
    group(VueBundle.message("vue.configurable.service.group")) {
      val tsPluginPreviewDisabled = ValueComponentPredicate(!getVueSettings(project).tsPluginPreviewEnabled)

      row(VueBundle.message("vue.configurable.service.languageServerPackage")) {
        cell(VueLspServerLoader.createNodePackageField(project))
          .align(AlignX.FILL)
          .bind(settings::packageRef)
      }.enabledIf(tsPluginPreviewDisabled)
      buttonsGroup {
        row {
          radioButton(VueBundle.message("vue.configurable.service.disabled"), VueServiceSettings.DISABLED)
            .comment(VueBundle.message("vue.configurable.service.disabled.help"))
        }
        row {
          radioButton(VueBundle.message("vue.configurable.service.auto"), VueServiceSettings.AUTO)
            .comment(VueBundle.message("vue.configurable.service.auto.help"))
        }
      }
        .bind(settings::serviceType)
        .enabledIf(tsPluginPreviewDisabled)

      separator()

      row {
        checkBox(VueBundle.message("vue.configurable.service.alpha.preview.label"))
          .gap(RightGap.SMALL)
          .comment(VueBundle.message("vue.configurable.service.alpha.preview.comment"))
          .bindSelected(settings::tsPluginPreviewEnabled)
          .applyToComponent {
            toolTipText = VueBundle.message("vue.configurable.service.alpha.preview.tooltip")
          }
          .onChanged { tsPluginPreviewDisabled.set(!it.isSelected) }
      }

      indent {
        row {
          label(VueBundle.message("vue.configurable.service.typescriptPluginBundleVersion"))
            .comment(VueBundle.message("vue.configurable.service.vue2SupportNote"))

          comboBox(
            items = VueTSPluginVersion.entries,
            renderer = textListCellRenderer("") { it.versionString }
          )
            .enabledIf(tsPluginPreviewDisabled.not())
            .bindItem(settings::tsPluginVersion.toNullableProperty())
        }
      }
    }
  }

  override fun getHelpTopic(): String = "settings.vue"

  override fun getDisplayName(): String = VueBundle.message("vue.configurable.title")
}