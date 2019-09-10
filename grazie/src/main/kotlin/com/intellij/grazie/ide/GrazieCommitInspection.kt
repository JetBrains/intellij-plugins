// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.grazie.ide

import com.intellij.codeHighlighting.HighlightDisplayLevel
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.codeInspection.ex.LocalInspectionToolWrapper
import com.intellij.openapi.project.Project
import com.intellij.vcs.commit.message.BaseCommitMessageInspection
import com.intellij.vcs.commit.message.CommitMessageInspectionProfile
import com.intellij.grazie.GrazieConfig
import com.intellij.grazie.ide.msg.GrazieStateLifecycle

class GrazieCommitInspection : BaseCommitMessageInspection() {
  companion object : GrazieStateLifecycle {
    private val grazie: LocalInspectionTool by lazy { GrazieInspection() }

    override fun init(state: GrazieConfig.State, project: Project) {
      with(CommitMessageInspectionProfile.getInstance(project)) {
        if (state.enabledCommitIntegration) {
          addTool(project, LocalInspectionToolWrapper(GrazieCommitInspection()), emptyMap())
          setToolEnabled("GraziCommit", true, project)
        }
        else {
          if (getToolsOrNull("GraziCommit", project) != null) setToolEnabled("GraziCommit", false, project)
          //TODO-tanvd how to remove tool?
        }
      }
    }

    override fun update(prevState: GrazieConfig.State, newState: GrazieConfig.State, project: Project) {
      if (prevState.enabledCommitIntegration == newState.enabledCommitIntegration) return

      init(newState, project)
    }
  }

  override fun getDefaultLevel(): HighlightDisplayLevel = HighlightDisplayLevel.find("TYPO") ?: HighlightDisplayLevel.WARNING

  override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean) = grazie.buildVisitor(holder, isOnTheFly)

  override fun getDisplayName() = "Grazie proofreading inspection for VCS"
}
