// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.grazie.ide.quickfix

import com.intellij.codeInsight.intention.PriorityAction
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.grazie.GrazieConfig
import com.intellij.grazie.grammar.Typo
import com.intellij.grazie.ide.ui.components.dsl.msg
import com.intellij.icons.AllIcons
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Iconable
import javax.swing.Icon

class GrazieDisableRuleQuickFix(private val typo: Typo) : LocalQuickFix, Iconable, PriorityAction {
  override fun getFamilyName(): String = msg("grazie.quickfix.disablerule.family")

  override fun getName() = msg("grazie.quickfix.disablerule.text", "'${typo.info.rule.description}'")

  override fun getIcon(flags: Int): Icon = AllIcons.Actions.Cancel

  override fun getPriority() = PriorityAction.Priority.LOW

  override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
    GrazieConfig.update {
      it.update(userEnabledRules = it.userEnabledRules - typo.info.rule.id, userDisabledRules = it.userDisabledRules + typo.info.rule.id)
    }
  }
}

