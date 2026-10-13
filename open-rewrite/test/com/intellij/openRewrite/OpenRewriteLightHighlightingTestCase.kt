package com.intellij.openRewrite

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.EDT
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.DependencyScope
import com.intellij.openapi.roots.ModuleRootModificationUtil
import com.intellij.pom.java.LanguageLevel
import com.intellij.testFramework.IdeaTestUtil
import com.intellij.testFramework.IndexingTestUtil
import com.intellij.testFramework.common.timeoutRunBlocking
import com.intellij.testFramework.fixtures.JavaCodeInsightTestFixture
import com.intellij.testFramework.fixtures.MavenDependencyUtil
import com.intellij.testFramework.javaCodeInsightFixture
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.testFramework.junit5.TestDisposable
import com.intellij.testFramework.junit5.fixture.moduleFixture
import com.intellij.testFramework.junit5.fixture.projectFixture
import com.intellij.testFramework.junit5.fixture.tempPathFixture
import com.intellij.testFramework.runInEdtAndWait
import com.intellij.testFramework.setUpJdk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.junit.jupiter.api.BeforeEach

private const val OPEN_REWRITE_VERSION = "8.16.0"
private const val OPEN_REWRITE_CORE = "org.openrewrite:rewrite-core:$OPEN_REWRITE_VERSION"

@TestApplication
abstract class OpenRewriteLightHighlightingTestCase {
  companion object {
    private val projectFixture = projectFixture(openAfterCreation = true)
  }

  private val tempDirFixture = tempPathFixture()
  private val moduleFixture = projectFixture.moduleFixture(tempDirFixture, addPathToSourceRoot = true)
  protected val myFixture: JavaCodeInsightTestFixture by javaCodeInsightFixture(projectFixture, tempDirFixture)

  @TestDisposable
  protected lateinit var testRootDisposable: Disposable

  protected val project: Project
    get() = projectFixture.get()

  protected val module: Module
    get() = moduleFixture.get()

  @BeforeEach
  fun setUpOpenRewriteModule() {
    runInEdtAndWait {
      setUpJdk(LanguageLevel.JDK_1_7, project, module, testRootDisposable)
    }
    IdeaTestUtil.setModuleLanguageLevel(module, LanguageLevel.HIGHEST, testRootDisposable)
    ModuleRootModificationUtil.updateModel(module) { model ->
      MavenDependencyUtil.addFromMaven(model, OPEN_REWRITE_CORE, false, DependencyScope.COMPILE)
    }
    IndexingTestUtil.waitUntilIndexesAreReady(project)
  }

  /**
   * Runs [block] on the EDT. Use it only for the editor, completion, and highlighting calls of the fixture.
   */
  protected fun onEdt(block: () -> Unit): Unit = timeoutRunBlocking {
    withContext(Dispatchers.EDT) {
      block()
    }
  }
}