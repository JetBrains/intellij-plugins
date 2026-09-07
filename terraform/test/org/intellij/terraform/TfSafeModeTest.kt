// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform

import com.intellij.ide.trustedProjects.TrustedProjects
import com.intellij.openapi.actionSystem.ex.ActionUtil
import com.intellij.openapi.application.EDT
import com.intellij.openapi.application.writeIntentReadAction
import com.intellij.testFramework.TrustedProjectsTestUtil
import com.intellij.testFramework.common.runAll
import com.intellij.testFramework.common.timeoutRunBlocking
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.testFramework.fixtures.TempDirTestFixture
import com.intellij.testFramework.fixtures.impl.TempDirTestFixtureImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.intellij.terraform.config.actions.TfExternalToolsAction
import org.intellij.terraform.config.util.TfCommandLineServiceMock
import org.intellij.terraform.runtime.TfProjectSettings
import java.io.File
import java.nio.file.Files
import java.nio.file.Path

internal class TfSafeModeTest : BasePlatformTestCase() {
  private lateinit var toolPath: Path

  override fun runInDispatchThread(): Boolean = false

  override fun createTempDirTestFixture(): TempDirTestFixture = TempDirTestFixtureImpl()

  override fun setUp() {
    super.setUp()
    toolPath = Files.createTempFile("terraform-test", null)
    File(toolPath.toString()).setExecutable(true)
    TrustedProjectsTestUtil.enableTrustedProjectsCheck(testRootDisposable)
    TrustedProjects.setProjectTrusted(project, false)
    TfProjectSettings.getInstance(project).toolPath = executablePath()
    TfCommandLineServiceMock.instance.clear()
  }

  override fun tearDown() {
    runAll(
      { TrustedProjects.setProjectTrusted(project, true) },
      { TfCommandLineServiceMock.instance.throwErrorsIfAny() },
      { if (::toolPath.isInitialized) Files.deleteIfExists(toolPath) },
      { super.tearDown() },
    )
  }

  fun testFormatActionDoesNotRunInUntrustedProject() = timeoutRunBlocking {
    configureFormatAction()

    invokeFormatAction()
    TfExternalToolsAction.awaitTfExternalToolsActions(project)

    assertTrue(TfCommandLineServiceMock.instance.requestsToVerify().isEmpty())
  }

  fun testFormatActionRunsInTrustedProject() = timeoutRunBlocking {
    val command = configureFormatAction()
    TrustedProjects.setProjectTrusted(project, true)

    invokeFormatAction()
    TfExternalToolsAction.awaitTfExternalToolsActions(project)

    assertEquals(listOf(command), TfCommandLineServiceMock.instance.requestsToVerify())
  }

  private fun configureFormatAction(): String {
    val file = myFixture.tempDirFixture.createFile("main.tf", "resource \"local_file\" \"test\" {}")
    myFixture.configureFromExistingVirtualFile(file)
    val command = "${executablePath()} fmt ${file.path}"
    TfCommandLineServiceMock.instance.mockCommandLine(command, "", testRootDisposable)
    return command
  }

  private suspend fun invokeFormatAction() {
    val action = ActionUtil.getAction("TfFmtFileAction") ?: throw AssertionError("TfFmtFileAction is not registered")
    withContext(Dispatchers.EDT) {
      writeIntentReadAction {
        myFixture.testAction(action)
      }
    }
  }

  private fun executablePath(): String = toolPath.toString()
}
