// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.grazie.ide

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.grazie.GrazieConfig
import com.intellij.grazie.grammar.GrammarChecker
import com.intellij.grazie.grammar.Typo
import com.intellij.grazie.ide.language.LanguageGrammarChecking
import com.intellij.grazie.ide.msg.GrazieStateLifecycle
import com.intellij.grazie.utils.isInjectedFragment
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor

class GrazieInspection : LocalInspectionTool() {
  companion object : GrazieStateLifecycle {
    override fun init(state: GrazieConfig.State, project: Project) {
      ProjectManager.getInstance().openProjects.forEach {
        DaemonCodeAnalyzer.getInstance(it).restart()
      }
    }

    override fun update(prevState: GrazieConfig.State, newState: GrazieConfig.State, project: Project) {
      if (prevState != newState) init(newState, project)
    }
  }

  override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
    return object : PsiElementVisitor() {
      override fun visitElement(element: PsiElement) {
        if (element.isInjectedFragment()) return

        val typos = HashSet<Typo>()
        for (strategy in LanguageGrammarChecking.allForLanguageOrAny(element.language).filter { it.isMyContextRoot(element) }) {
          typos.addAll(GrammarChecker.check(element, strategy))
        }

        typos.map { GrazieProblemDescriptor(it, isOnTheFly) }.forEach {
          holder.registerProblem(it)
        }

        super.visitElement(element)
      }
    }
  }

  override fun getDisplayName() = "Grazie proofreading inspection"
}
