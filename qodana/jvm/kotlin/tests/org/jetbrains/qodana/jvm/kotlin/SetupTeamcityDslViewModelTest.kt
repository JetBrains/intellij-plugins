package org.jetbrains.qodana.jvm.kotlin

import com.intellij.codeInsight.daemon.impl.DaemonCodeAnalyzerImpl
import com.intellij.openapi.application.invokeAndWaitIfNeeded
import com.intellij.openapi.application.writeIntentReadAction
import com.intellij.psi.PsiDocumentManager
import com.intellij.testFramework.EditorTestUtil
import com.intellij.testFramework.TestDataPath
import kotlinx.coroutines.cancel
import org.jetbrains.qodana.QodanaPluginHeavyTestBase
import org.jetbrains.qodana.runDispatchingOnUi
import org.jetbrains.qodana.ui.ProjectVcsDataProviderMock
import org.jetbrains.qodana.ui.ci.providers.teamcity.SetupTeamcityDslViewModel
import kotlin.io.path.Path
import kotlin.io.path.pathString

@TestDataPath("\$CONTENT_ROOT/test-data/SetupTeamcityDslViewModelTest")
class SetupTeamcityDslViewModelTest : QodanaPluginHeavyTestBase() {

  override fun getBasePath(): String = Path(super.getBasePath(), "SetupTeamcityDslViewModelTest").pathString

  override fun setUp() {
    super.setUp()
    setUpProject()
  }

  private fun setUpProject() {
    invokeAndWaitIfNeeded {
      copyProjectTestData(getTestName(true).trim())
    }

  }

  override fun tearDown() {
    try {
      scope.cancel()
      EditorTestUtil.releaseAllEditors()
    }
    catch (e: Throwable) {
      addSuppressedException(e)
    }
    finally {
      super.tearDown()
    }
  }

  override fun runInDispatchThread(): Boolean = false

  fun `test highlighting disabled`() = runDispatchingOnUi {
    val path = Path(myFixture.tempDirFixture.tempDirPath)
    val viewModel = SetupTeamcityDslViewModel(path, myFixture.project, scope, ProjectVcsDataProviderMock())

    val editor = viewModel.configEditorDeferred.await()
    //maybe readaction
    val psiFile = writeIntentReadAction { PsiDocumentManager.getInstance(project).getPsiFile(editor.document) }
    assertNotNull(psiFile)

    assertFalse(DaemonCodeAnalyzerImpl.getInstance(project).isHighlightingAvailable(psiFile!!))
  }
}