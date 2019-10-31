// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.grazie.ide.quickfix.supress

import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.SuppressQuickFix
import com.intellij.grazie.GrazieConfig
import com.intellij.grazie.ide.ui.components.dsl.msg
import com.intellij.grazie.jlanguage.Lang
import com.intellij.grazie.jlanguage.LangTool
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import org.languagetool.rules.Category
import org.languagetool.rules.Rule

class GrazieSuppressCategoryQuickFix(private val lang: Lang, private val category: Category) : SuppressQuickFix {
  override fun isAvailable(project: Project, context: PsiElement): Boolean = true

  override fun isSuppressAll(): Boolean = false

  override fun getFamilyName(): String = msg("grazie.quickfix.disable.category.family")

  override fun getName() = msg("grazie.quickfix.disable.category.text", "'${category.name}'")

  override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
    GrazieConfig.update { state ->
      val toDisable = with(LangTool.getTool(lang)) {
        val activeRules = allActiveRules.toSet()

        fun Rule.isActive() = (id in state.userEnabledRules && id !in state.userDisabledRules)
          || (id !in state.userDisabledRules && id !in state.userEnabledRules && this in activeRules)

        allRules.filter { it.category == category && it.isActive() }.distinctBy { it.id }
      }

      state.update(
        userEnabledRules = state.userEnabledRules - toDisable.map { it.id },
        userDisabledRules = state.userDisabledRules + toDisable.map { it.id }
      )
    }
  }
}

