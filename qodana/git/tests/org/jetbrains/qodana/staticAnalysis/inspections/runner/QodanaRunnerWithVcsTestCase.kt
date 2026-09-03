package org.jetbrains.qodana.staticAnalysis.inspections.runner

import com.intellij.openapi.components.service
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vcs.Ignored
import com.intellij.openapi.vcs.ProjectLevelVcsManager
import com.intellij.openapi.vcs.VcsIgnoreChecker
import com.intellij.openapi.vcs.changes.VcsIgnoreManagerImpl
import com.intellij.openapi.vcs.impl.ProjectLevelVcsManagerImpl
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.project.stateStore
import git4idea.GitVcs
import git4idea.repo.GitRepositoryFiles
import git4idea.test.createRepository
import org.jetbrains.qodana.staticAnalysis.testFramework.QodanaRunnerTestCase
import java.io.File
import java.nio.file.Path

abstract class QodanaRunnerWithVcsTestCase : QodanaRunnerTestCase() {
  private lateinit var gitIgnoreChecker: VcsIgnoreChecker
  protected open val makeInitialCommit: Boolean = true

  private val projectPath: String
    get() = FileUtil.toSystemIndependentName(project.stateStore.projectBasePath.toString())

  override fun setUp() {
    super.setUp()
    createRepository(project, projectPath, makeInitialCommit)

    (project.service<ProjectLevelVcsManager>() as ProjectLevelVcsManagerImpl).waitForInitialized()

    gitIgnoreChecker = VcsIgnoreManagerImpl.EP_NAME.extensionList.find { it.supportedVcs == GitVcs.getKey() }
                       ?: throw IllegalStateException("Cannot find registered GitRootChecker")
  }

  protected fun createGitIgnoreFile(text: String) {
    val file = File("$projectPath/${GitRepositoryFiles.GITIGNORE}")
    file.writeText(text)
    LocalFileSystem.getInstance().refreshIoFiles(listOf(file))
  }

  protected fun fileIsIgnored(file: Path): Boolean =
    gitIgnoreChecker.isIgnored(getOrCreateProjectBaseDir(), file) is Ignored
}
