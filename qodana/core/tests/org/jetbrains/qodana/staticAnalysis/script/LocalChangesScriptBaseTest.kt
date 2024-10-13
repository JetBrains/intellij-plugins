package org.jetbrains.qodana.staticAnalysis.script

import com.intellij.openapi.application.invokeAndWaitIfNeeded
import com.intellij.openapi.vcs.ProjectLevelVcsManager
import com.intellij.openapi.vcs.VcsDirectoryMapping
import com.intellij.openapi.vcs.changes.ChangeListManagerImpl
import com.intellij.openapi.vcs.changes.VcsDirtyScopeManager
import com.intellij.psi.PsiDocumentManager
import com.intellij.testFramework.HeavyTestHelper
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaRunnerTestCase
import java.nio.file.Paths
import kotlin.io.path.exists

abstract class LocalChangesScriptBaseTest: QodanaRunnerTestCase() {
  override fun createTestStructure() {
    invokeAndWaitIfNeeded {
      super.createTestStructure()
      val updatePath = getTestDataPath("update")
      if (!updatePath.exists()) return@invokeAndWaitIfNeeded
      // init repo
      run("git", "-c", "init.defaultBranch=main", "init")
      run("git", "config", "user.email", "\"test@test.com\"")
      run("git", "config", "user.name", "\"LocalChangesScriptTest\"")
      run("git", "config", "core.autocrlf", "input")
      run("git", "add", "--all", ".")
      run("git", "commit", "-m", "init", ".")
      // make changes
      val dir = Paths.get(project.basePath!!, "test-module")
      HeavyTestHelper.createTestProjectStructure(myModule, updatePath.toString(), dir, true)
      PsiDocumentManager.getInstance(myProject).commitAllDocuments()
      // attach repo to project
      val vcsManager = ProjectLevelVcsManager.getInstance(project)
      vcsManager.directoryMappings = listOf(VcsDirectoryMapping(project.basePath!!, "Git"))
      ChangeListManagerImpl.getInstanceImpl(project).waitUntilRefreshed()
      // await for the distribution
      val dirtyScopeManager = VcsDirtyScopeManager.getInstance(project)
      val changeListManager = ChangeListManagerImpl.getInstanceImpl(project)
      dirtyScopeManager.markEverythingDirty()
      changeListManager.waitUntilRefreshed()
    }
  }

  override fun tearDown() {
    invokeAndWaitIfNeeded {
      super.tearDown()
    }
  }

  override fun runInDispatchThread() = false
}