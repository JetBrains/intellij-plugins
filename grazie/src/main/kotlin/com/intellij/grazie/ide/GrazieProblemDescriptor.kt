// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.grazie.ide

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptorBase
import com.intellij.grazie.grammar.Typo
import com.intellij.grazie.ide.fus.GrazieFUCounterCollector
import com.intellij.grazie.ide.quickfix.GrazieAddWordQuickFix
import com.intellij.grazie.ide.quickfix.GrazieDisableRuleQuickFix
import com.intellij.grazie.ide.quickfix.GrazieRenameTypoQuickFix
import com.intellij.grazie.ide.quickfix.GrazieReplaceTypoQuickFix
import com.intellij.grazie.ide.ui.components.dsl.msg
import com.intellij.grazie.utils.*
import com.intellij.openapi.application.ApplicationManager
import kotlinx.html.*
import com.intellij.grazie.utils.buildList

class GrazieProblemDescriptor(val fix: Typo, isOnTheFly: Boolean) : ProblemDescriptorBase(
  fix.location.element!!, fix.location.element!!,
  fix.toDescriptionTemplate(isOnTheFly),
  fix.toFixes(isOnTheFly).toTypedArray(),
  fix.info.category.highlight, false,
  fix.toSelectionRange(), true, isOnTheFly
) {
  companion object {
    private fun Typo.toFixes(isOnTheFly: Boolean) = buildList<LocalQuickFix> {
      if (info.rule.isDictionaryBasedSpellingRule) {
        add(GrazieAddWordQuickFix(this@toFixes))
      }

      if (fixes.isNotEmpty() && isOnTheFly) {
        if (location.shouldUseRename) {
          add(GrazieRenameTypoQuickFix(this@toFixes))
        }
        else {
          GrazieFUCounterCollector.typoFound(this@toFixes)
          add(GrazieReplaceTypoQuickFix(this@toFixes))
        }
      }

      // disable spelling rule will not affect anything
      if (!isSpellingTypo) {
        add(GrazieDisableRuleQuickFix(this@toFixes))
      }
    }

    private fun Typo.toDescriptionTemplate(isOnTheFly: Boolean): String {
      if (ApplicationManager.getApplication().isUnitTestMode) return info.rule.id
      return html {
        if (isSpellingTypo) {
          +info.rule.toDescriptionSanitized()
        }
        else {
          if (fixes.isNotEmpty()) {
            p {
              style = "padding-bottom: 10px;"
              +"$word &rarr; ${fixes.take(3).joinToString(separator = "/")}"
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

              if (it.corrections.any { !it.isNullOrBlank() }) {
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

  override fun getProblemGroup() = fix.info.category
}
