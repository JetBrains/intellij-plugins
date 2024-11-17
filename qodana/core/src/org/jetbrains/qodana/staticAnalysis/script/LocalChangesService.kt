package org.jetbrains.qodana.staticAnalysis.script

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import git4idea.config.GitSaveChangesPolicy
import java.util.concurrent.atomic.AtomicBoolean

/*
 Designated to track local changes script run and customize vcs save changes policy.
 Used by Rider to execute local changes with custom hooks.
 */
@Service(Service.Level.PROJECT)
class LocalChangesService(private val project: Project) {
  val isIncrementalAnalysis = AtomicBoolean()
  val useStash = AtomicBoolean()

  companion object {
    fun getInstance(project: Project): LocalChangesService = project.service()
  }

  fun getGitPolicy() = if (useStash.get()) {
    GitSaveChangesPolicy.STASH
  }
  else {
    GitSaveChangesPolicy.SHELVE
  }
}
