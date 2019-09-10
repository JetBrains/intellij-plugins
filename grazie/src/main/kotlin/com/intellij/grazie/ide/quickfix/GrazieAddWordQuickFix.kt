// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.grazie.ide.quickfix

import com.intellij.codeInsight.intention.PriorityAction
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Iconable
import icons.SpellcheckerIcons
import com.intellij.grazie.GrazieConfig
import com.intellij.grazie.grammar.Typo
import com.intellij.grazie.ide.ui.components.dsl.msg
import javax.swing.Icon

class GrazieAddWordQuickFix(private val typo: Typo) : LocalQuickFix, Iconable, PriorityAction {
  override fun getFamilyName() = msg("grazie.quickfix.addword.family")

  override fun getName() = msg("grazie.quickfix.addword.text", typo.word)

  override fun getIcon(flags: Int): Icon = SpellcheckerIcons.Dictionary

  override fun getPriority() = PriorityAction.Priority.NORMAL

  override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
    GrazieConfig.update {
      it.update(userWords = it.userWords + typo.word.toLowerCase())
    }
  }
}
