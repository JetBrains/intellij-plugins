package com.intellij.openRewrite.recipe

import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.ProjectScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import org.jetbrains.annotations.TestOnly

@TestOnly
internal class OpenRewriteTestRecipeService(private val project: Project, coroutineScope: CoroutineScope) :
  OpenRewriteRecipeService(project, coroutineScope) {
  override fun getRecipeScope(): GlobalSearchScope = ProjectScope.getAllScope(project)

  override fun reload(modalityState: ModalityState?): Job? = null
}