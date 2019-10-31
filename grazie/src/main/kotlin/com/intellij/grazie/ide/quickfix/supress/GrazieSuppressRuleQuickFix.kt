// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.grazie.ide.quickfix.supress

import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.SuppressQuickFix
import com.intellij.grazie.GrazieConfig
import com.intellij.grazie.grammar.Typo
import com.intellij.grazie.ide.ui.components.dsl.msg
import com.intellij.grazie.jlanguage.Lang
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import org.languagetool.rules.Rule

class GrazieSuppressRuleQuickFix(private val lang: Lang, private val rule: Rule) : SuppressQuickFix {
  override fun isAvailable(project: Project, context: PsiElement) = context.isValid

  override fun isSuppressAll(): Boolean = false

  override fun getFamilyName(): String = msg("grazie.quickfix.disable.rule.family")

  override fun getName() = msg("grazie.quickfix.disable.rule.text", "'${rule.description}'")

  override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
    GrazieConfig.update { state ->
      state.update(
        userEnabledRules = state.userEnabledRules - rule.id,
        userDisabledRules = state.userDisabledRules + rule.id
      )
    }
  }
}

