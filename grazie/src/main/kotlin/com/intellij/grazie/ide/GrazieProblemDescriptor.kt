// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.grazie.ide

import com.intellij.codeInspection.*
import com.intellij.grazie.grammar.Typo
import com.intellij.grazie.ide.fus.GrazieFUCounterCollector
import com.intellij.grazie.ide.quickfix.GrazieReplaceTypoQuickFix
import com.intellij.grazie.ide.quickfix.supress.GrazieDisableCategoryQuickFix
import com.intellij.grazie.ide.quickfix.supress.GrazieDisableRuleQuickFix
import com.intellij.grazie.ide.ui.components.dsl.msg
import com.intellij.grazie.utils.*
import com.intellij.openapi.application.ApplicationManager
import kotlinx.html.*

class GrazieProblemDescriptor(fix: Typo, isOnTheFly: Boolean) : ProblemDescriptorBase(
  fix.location.element!!, fix.location.element!!,
  fix.toDescriptionTemplate(isOnTheFly),
  fix.toFixes(isOnTheFly).toTypedArray(),
  ProblemHighlightType.GENERIC_ERROR_OR_WARNING, false,
  fix.toSelectionRange(), true, isOnTheFly
) {
  companion object {
    private fun Typo.toFixes(isOnTheFly: Boolean) = buildList<LocalQuickFix> {
      if (isOnTheFly && !ApplicationManager.getApplication().isUnitTestMode) {
        if (fixes.isNotEmpty()) {
          GrazieFUCounterCollector.typoFound(this@toFixes)
          add(GrazieReplaceTypoQuickFix(this@toFixes))
        }

        add(GrazieDisableRuleQuickFix(this@toFixes.info.rule))
        add(GrazieDisableCategoryQuickFix(this@toFixes.info.lang, this@toFixes.info.rule.category))
      }
    }

    private fun Typo.toDescriptionTemplate(isOnTheFly: Boolean): String {
      if (ApplicationManager.getApplication().isUnitTestMode) return info.rule.id
      return html {
        if (!location.errorText.isNullOrBlank() && fixes.isNotEmpty() && fixes.none { it.isBlank() }) {
          p {
            style = "padding-bottom: 10px;"
            +"${location.errorText} &rarr; ${fixes.take(3).joinToString(separator = "/")}"
            if (!isOnTheFly) nbsp()
          }
        }

        p {
          info.incorrectExample?.let {
            style = "padding-bottom: 8px;"
          }

          +info.rule.toDescriptionSanitized()
          if (!isOnTheFly) nbsp()
        }

        table {
          cellpading = "0"
          cellspacing = "0"

          info.incorrectExample?.let {
            tr {
              td {
                valign = "top"
                style = "padding-right: 5px; color: gray; vertical-align: top;"
                +msg("grazie.ui.settings.rules.rule.incorrect")
                if (!isOnTheFly) nbsp()
              }
              td {
                style = "width: 100%;"
                toIncorrectHtml(it)
                if (!isOnTheFly) nbsp()
              }
            }

            if (it.corrections.any { correction -> !correction.isNullOrBlank() }) {
              tr {
                td {
                  valign = "top"
                  style = "padding-top: 5px; padding-right: 5px; color: gray; vertical-align: top;"
                  +msg("grazie.ui.settings.rules.rule.correct")
                  if (!isOnTheFly) nbsp()
                }
                td {
                  style = "padding-top: 5px; width: 100%;"
                  toCorrectHtml(it)
                  if (!isOnTheFly) nbsp()
                }
              }
            }
          }
        }
      }
    }
  }
}
