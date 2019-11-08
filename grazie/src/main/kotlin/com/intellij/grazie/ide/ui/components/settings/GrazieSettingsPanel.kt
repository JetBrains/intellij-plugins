// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.grazie.ide.ui.components.settings

import com.intellij.grazie.GrazieConfig
import com.intellij.grazie.ide.ui.components.dsl.border
import com.intellij.grazie.ide.ui.components.dsl.msg
import com.intellij.grazie.ide.ui.components.dsl.padding
import com.intellij.grazie.ide.ui.components.dsl.panel
import com.intellij.grazie.jlanguage.Lang
import com.intellij.grazie.remote.GrazieRemote
import com.intellij.ide.plugins.newui.VerticalLayout
import com.intellij.openapi.Disposable
import com.intellij.openapi.options.ConfigurableUi
import com.intellij.openapi.project.guessCurrentProject
import com.intellij.ui.layout.migLayout.*
import com.intellij.util.ui.JBUI
import net.miginfocom.layout.AC
import net.miginfocom.layout.CC
import net.miginfocom.swing.MigLayout
import javax.swing.JComponent

class GrazieSettingsPanel : ConfigurableUi<GrazieConfig>, Disposable {
  private val vcs = GrazieCommitComponent()
  private val native = GrazieNativeLanguageComponent(::download)
  private val description = GrazieRuleDescriptionComponent()
  private val rules = GrazieRulesComponent(description.listener)
  private val languages = GrazieLanguagesComponent(::download, rules::addLang, rules::removeLang)

  private fun download(lang: Lang): Boolean {
    val isSucceed = GrazieRemote.download(lang, guessCurrentProject(vcs.component))
    if (isSucceed) update()
    return isSucceed
  }

  private fun update() {
    native.update()
    languages.update()
    rules.reset()
  }

  fun showOption(option: String?) = Runnable { rules.filter(option ?: "") }

  override fun isModified(settings: GrazieConfig) = rules.isModified
    .or(settings.state.enabledCommitIntegration != vcs.isCommitIntegrationEnabled)
    .or(settings.state.nativeLanguage != native.language)
    .or(settings.state.enabledLanguages != languages.values)

  override fun apply(settings: GrazieConfig) {
    GrazieConfig.update { state ->
      val enabledLanguages = state.enabledLanguages.toMutableSet()
      val userDisabledRules = state.userDisabledRules.toMutableSet()
      val userEnabledRules = state.userEnabledRules.toMutableSet()

      val chosenEnabledLanguages = languages.values
      Lang.values().forEach {
        if (chosenEnabledLanguages.contains(it)) {
          enabledLanguages.add(it)
        } else {
          enabledLanguages.remove(it)
        }
      }

      val (enabledRules, disabledRules) = rules.state

      enabledRules.forEach { id ->
        userDisabledRules.remove(id)
        userEnabledRules.add(id)
      }

      disabledRules.forEach { id ->
        userDisabledRules.add(id)
        userEnabledRules.remove(id)
      }

      state.update(
        enabledLanguages = enabledLanguages,
        userEnabledRules = userEnabledRules,
        userDisabledRules = userDisabledRules,
        nativeLanguage = native.language,
        enabledCommitIntegration = vcs.isCommitIntegrationEnabled
      )
    }

    rules.reset()
  }

  override fun reset(settings: GrazieConfig) {
    native.language = settings.state.nativeLanguage
    vcs.isCommitIntegrationEnabled = settings.state.enabledCommitIntegration
    languages.reset(settings.state.enabledLanguages.sortedWith(Comparator.comparing(Lang::displayName)))
    rules.reset()

    update()
  }

  override fun getComponent(): JComponent {
    return panel(MigLayout(createLayoutConstraints(), AC().grow(), AC().index(1).grow())) {
      panel(MigLayout(createLayoutConstraints(), AC().grow()), constraint = CC().growX().wrap()) {
        border = border(msg("grazie.ui.settings.languages.text"), false, JBUI.insetsBottom(10), false)

        add(languages.component,
          CC().growX().maxHeight("").width("45%").minWidth("250px").minHeight("120px").maxHeight("120px").alignY("top"))

        panel(VerticalLayout(0), CC().grow().width("55%").minWidth("250px").alignY("top")) {
          border = padding(JBUI.insetsLeft(20))
          add(native.component)
          add(vcs.component)
        }

        update()
      }

      panel(MigLayout(createLayoutConstraints(), AC().grow(), AC().grow()), constraint = CC().grow()) {
        border = border(msg("grazie.ui.settings.rules.configuration.text"), false, JBUI.emptyInsets())

        add(rules.component, CC().grow().width("45%").minWidth("250px"))
        add(description.component, CC().grow().width("55%"))
      }
    }
  }

  override fun dispose() = rules.dispose()
}
