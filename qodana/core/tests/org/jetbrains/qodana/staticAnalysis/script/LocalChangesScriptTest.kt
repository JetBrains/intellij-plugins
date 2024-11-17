package org.jetbrains.qodana.staticAnalysis.script

import com.intellij.ide.CommandLineInspectionProjectConfigurator
import com.intellij.openapi.application.runWriteActionAndWait
import com.intellij.openapi.application.writeAction
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.platform.backend.observation.ActivityTracker
import com.intellij.testFramework.TestLoggerFactory.TestLoggerAssertionError
import com.intellij.testFramework.VfsTestUtil
import com.intellij.testFramework.rethrowLoggedErrorsIn
import git4idea.config.GitSaveChangesPolicy
import junit.framework.TestCase
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaScriptConfig
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaException
import org.jetbrains.qodana.staticAnalysis.markGenFolderAsGeneratedSources
import org.junit.Test
import org.junit.jupiter.api.assertThrows

class LocalChangesScriptTest : LocalChangesScriptBaseTest() {
  /** The project is not under version control, so there cannot be any local changes. */
  @Test
  fun `project without VCS`() {
    updateQodanaConfig {
      it.copy(
        script = QodanaScriptConfig("local-changes"),
      )
    }

    rethrowLoggedErrorsIn {
      val exception = assertThrows<TestLoggerAssertionError> {
        runAnalysis()
      }
      assertThat(exception).hasCause(QodanaException("Cannot initialize VCS mapping"))
    }
  }

  /** The project is under version control, but there are no local changes. Everything is already committed. */
  @Test
  fun `no local changes`() {
    updateQodanaConfig {
      it.copy(
        script = QodanaScriptConfig("local-changes"),
      )
    }

    runAnalysis()
    assertSarifResults()
  }

  /** There are local changes in the project, but they don't affect the location of the problems that are found. */
  @Test
  fun `local changes, same location`() {
    updateQodanaConfig {
      it.copy(
        script = QodanaScriptConfig("local-changes"),
      )
    }

    runAnalysis()
    assertSarifResults()
  }

  /** Testing potential update by configurators files of the project.*/
  @Test
  fun `local changes with updating files on configure stage`() {
    val configurator = addCorruptingConfigurator {
      val file = VfsUtil.findFile(qodanaConfig.projectPath.resolve("test-module/A.java"), false)!!
      val text = VfsUtil.loadText(file)
      VfsUtil.saveText(file, "import unused;\n$text")
    }
    updateQodanaConfig {
      it.copy(
        script = QodanaScriptConfig("local-changes"),
      )
    }

    runAnalysis()
    TestCase.assertEquals(configurator.expectedInvocationCount, configurator.invocationCount)
    assertSarifResults()
  }

  /** Testing potential update by configurators files of the project.*/
  @Test
  fun `local changes with creating conflicting file on configure stage`() {
    val configurator = addCorruptingConfigurator {
      val root = VfsUtil.findFile(qodanaConfig.projectPath, false)!!
      VfsTestUtil.createFile(root, "test-module/B.java", "awful text")
    }
    updateQodanaConfig {
      it.copy(
        script = QodanaScriptConfig("local-changes"),
      )
    }

    runAnalysis()
    TestCase.assertEquals(configurator.expectedInvocationCount, configurator.invocationCount)
    assertSarifResults()
  }

  private fun addCorruptingConfigurator(action: () -> Unit): Corruptor =
      CorruptingConfigurator(action)
        .also { CommandLineInspectionProjectConfigurator.EP_NAME.point.registerExtension(it, testRootDisposable) }

  /**
   * There are local changes in the project that move the problems to other physical locations.
   * There are no essential changes though, so there is nothing to report.
   */
  @Test
  fun `local changes, different location`() {
    updateQodanaConfig {
      it.copy(
        script = QodanaScriptConfig("local-changes"),
      )
    }

    runAnalysis()
    assertSarifResults()
  }

  /**
   * There are local changes that add a new problem and fix an existing problem.
   * Only the newly added problem is reported.
   */
  @Test
  fun `local changes, absent and new`() {
    assertEquals(GitSaveChangesPolicy.SHELVE, project.service<LocalChangesService>().getGitPolicy())
    updateQodanaConfig {
      it.copy(
        script = QodanaScriptConfig("local-changes"),
        includeAbsent = false,
      )
    }

    runAnalysis()
    assertSarifResults()
  }

  /*
  Do the same as the test above, but with stash mode
   */
  @Test
  fun `local changes, absent and new, use stash`() {
    project.service<LocalChangesService>().useStash.set(true)
    assertEquals(GitSaveChangesPolicy.STASH, project.service<LocalChangesService>().getGitPolicy())
    updateQodanaConfig {
      it.copy(
        script = QodanaScriptConfig("local-changes"),
        includeAbsent = false,
      )
    }

    runAnalysis()
    assertSarifResults()
  }

  @Test
  fun `changes in generated files, do not analyze`() {
    runBlocking {
      markGenFolderAsGeneratedSources(module)
      updateQodanaConfig {
        it.copy(
          script = QodanaScriptConfig("local-changes"),
          includeAbsent = false,
        )
      }

      runAnalysis()
      assertSarifResults()
    }
  }

  /**
   * There are local changes that add a new problem and fix an existing problem.
   * Source dir is set and points to the directory of the changes.
   * Only the newly added problem is reported.
   */
  @Test
  fun `local changes, in source directory`() {
    assertEquals(GitSaveChangesPolicy.SHELVE, project.service<LocalChangesService>().getGitPolicy())
    updateQodanaConfig {
      it.copy(
        script = QodanaScriptConfig("local-changes"),
        includeAbsent = false,
        sourceDirectory = "test-module"
      )
    }
    runAnalysis()
    assertSarifResults()
  }

  /**
   * There are local changes that add a new problem and fix an existing problem.
   * Source dir is set and points NOT to the directory of the changes.
   * No problems get reported.
   */
  @Test
  fun `local changes, outside source directory`() {
    assertEquals(GitSaveChangesPolicy.SHELVE, project.service<LocalChangesService>().getGitPolicy())
    updateQodanaConfig {
      it.copy(
        script = QodanaScriptConfig("local-changes"),
        includeAbsent = false,
        sourceDirectory = "test-module/other-folder"
      )
    }
    runAnalysis()
    assertSarifResults()
  }

  private abstract class Corruptor {
    abstract val expectedInvocationCount: Int
    var invocationCount = 0
      protected set
  }

  private class CorruptingConfigurator(private val applyCorruption: () -> Unit) : Corruptor(), CommandLineInspectionProjectConfigurator {
    override val expectedInvocationCount: Int = 2
    private val corruptOnInvocation = 1

    override fun getName() = "test"
    override fun getDescription() = ""
    override fun configureProject(project: Project, context: CommandLineInspectionProjectConfigurator.ConfiguratorContext) {
      if (++invocationCount != corruptOnInvocation) return
      runWriteActionAndWait { applyCorruption() }
    }
  }

  private class CorruptingTracker(private val applyCorruption: () -> Unit) : Corruptor(), ActivityTracker {
    // First invocation is in QodanaTestManager, 2nd, 3rd, and 4th in LocalChanges#before, 5th in LocalChanges#after
    override val expectedInvocationCount: Int = 4
    private val corruptOnInvocation = 2

    private val done = CompletableDeferred<Unit>()

    override val presentableName: String = "test"

    override suspend fun isInProgress(project: Project): Boolean =
      if (++invocationCount == corruptOnInvocation) {
        writeAction {
          applyCorruption()
          done.complete(Unit)
        }
        true
      }
      else {
        false // Returning false will skip awaitConfiguration for this iteration. Otherwise, we would wait forever on the deferred
      }

    override suspend fun awaitConfiguration(project: Project) = done.await()
  }
}
