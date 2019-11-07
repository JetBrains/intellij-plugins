// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.grazie.ide.quickfix.supress

import com.intellij.codeInsight.intention.PriorityAction
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.grazie.GrazieConfig
import com.intellij.grazie.ide.ui.components.dsl.msg
import com.intellij.icons.AllIcons
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Iconable
import org.languagetool.rules.Rule
import javax.swing.Icon

class GrazieDisableRuleQuickFix(private val rule: Rule) : LocalQuickFix, Iconable, PriorityAction {
  //Should have priority more than category suppress
  override fun getPriority() = PriorityAction.Priority.NORMAL

  override fun getFamilyName(): String = msg("grazie.quickfix.suppress.rule.family")

  override fun getIcon(flags: Int): Icon = AllIcons.Actions.Cancel

  override fun getName() = msg("grazie.quickfix.suppress.rule.text", rule.description)

  override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
    GrazieConfig.update { state ->
      state.update(
        userEnabledRules = state.userEnabledRules - rule.id,
        userDisabledRules = state.userDisabledRules + rule.id
      )
    }
  }
}

