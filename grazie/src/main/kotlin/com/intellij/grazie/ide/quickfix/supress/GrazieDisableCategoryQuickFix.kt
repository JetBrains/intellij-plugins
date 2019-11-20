// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.grazie.ide.quickfix.supress

import com.intellij.codeInsight.intention.LowPriorityAction
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.grazie.GrazieConfig
import com.intellij.grazie.ide.ui.components.dsl.msg
import com.intellij.grazie.ide.ui.components.rules.ComparableCategory
import com.intellij.grazie.jlanguage.Lang
import com.intellij.grazie.jlanguage.LangTool
import com.intellij.icons.AllIcons
import com.intellij.openapi.command.undo.BasicUndoableAction
import com.intellij.openapi.command.undo.UndoManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Iconable
import org.languagetool.rules.Category
import javax.swing.Icon

class GrazieDisableCategoryQuickFix(private val lang: Lang, private val category: Category) : LocalQuickFix, Iconable, LowPriorityAction {
  override fun getFamilyName(): String = msg("grazie.quickfix.suppress.category.family")

  override fun getIcon(flags: Int): Icon = AllIcons.Actions.Cancel

  override fun getName() = msg("grazie.quickfix.suppress.category.text", category.name)

  override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
    val action = object : BasicUndoableAction(descriptor.psiElement?.containingFile?.virtualFile) {
      private val toDisable = LangTool.getTool(lang).allActiveRules.filter {
        ComparableCategory(it.category) == ComparableCategory(category)
      }.distinctBy { it.id }

      override fun redo() {
        GrazieConfig.update { state ->
          state.update(
            userEnabledRules = state.userEnabledRules - toDisable.map { it.id },
            userDisabledRules = state.userDisabledRules + toDisable.map { it.id }
          )
        }
      }

      override fun undo() {
        GrazieConfig.update { state ->
          state.update(
            userEnabledRules = state.userEnabledRules + toDisable.map { it.id },
            userDisabledRules = state.userDisabledRules - toDisable.map { it.id }
          )
        }
      }
    }

    action.redo()
    UndoManager.getInstance(project).undoableActionPerformed(action)
  }
}

