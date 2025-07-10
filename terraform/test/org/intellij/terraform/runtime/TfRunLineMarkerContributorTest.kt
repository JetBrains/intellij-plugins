// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.runtime

import com.intellij.execution.RunManager
import com.intellij.execution.RunnerAndConfigurationSettings
import com.intellij.execution.lineMarker.RunLineMarkerContributor.Info
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.vfs.toNioPathOrNull
import com.intellij.testFramework.TestModeFlags
import com.intellij.ui.IconManager
import javax.swing.Icon

internal class TfRunLineMarkerContributorTest : TfBaseRunConfigurationTest() {

  private val warnedRunIcon: Icon =
    IconManager.getInstance().createLayered(AllIcons.RunConfigurations.TestState.Run, AllIcons.Nodes.WarningMark)

  override fun setUp() {
    super.setUp()
    TestModeFlags.set(TF_RUN_MOCK, true, testRootDisposable)
  }

  fun testSimpleLineMarker() {
    val info = assertRunLineMarkerIcon("simple.tf")
    runActionsAndCheckNames(info?.actions)
  }

  fun testLineMarkerWithComment() {
    val info = assertRunLineMarkerIcon("with_comment.tf")

    testRunConfigActions(info?.actions)
    runActionsAndCheckNames(info?.actions)

    val gutter = myFixture.findGutter("with_comment.tf")
    assertNotNull(gutter)
    assertEquals(warnedRunIcon, gutter?.icon)
  }

  fun testLineMarkerWithWholeCommented() {
    val gutter = myFixture.findGutter("whole_commented.tf")
    assertNull(gutter)

    val gutters = myFixture.findAllGutters("whole_commented.tf")
    assertEmpty(gutters)
  }

  fun testNotDuplicatedRunConfig() {
    val info = assertRunLineMarkerIcon("with_duplicated.tf")
    runActionsAndCheckNames(info?.actions)

    myFixture.type(" ")
    val updatedGutter = myFixture.file.findElementAt(myFixture.caretOffset)?.let { TfRunLineMarkerContributor().getInfo(it) }
    if (updatedGutter == null) {
      fail("Info of RunLineMarker not should be empty")
      return
    }
    assertEquals(info?.actions?.size, updatedGutter.actions.size)
  }

  private fun assertRunLineMarkerIcon(fileName: String): Info? {
    myFixture.configureFromExistingVirtualFile(myFixture.copyFileToProject(fileName, "src/$fileName"))
    val file = myFixture.file
    val info = file.findElementAt(myFixture.caretOffset)?.let { TfRunLineMarkerContributor().getInfo(it) }
    if (info == null) {
      fail("Info of RunLineMarker not should be empty")
    }

    assertEquals(warnedRunIcon, info?.icon)
    return info
  }

  private fun runActionsAndCheckNames(actions: Array<AnAction>?) {
    assertEquals(actions?.last()?.templateText, "Edit Configurations…")

    val templateActions = actions?.take(5)
    val presentations = templateActions?.map { myFixture.testAction(it) }
    assertEquals(presentations?.size, runTemplateConfigActionsName.size)

    runTemplateConfigActionsName.forEachIndexed { index, name ->
      assertEquals(name, presentations?.get(index)?.text)
    }
  }

  private fun testRunConfigActions(actions: Array<AnAction>?) {
    actions ?: return
    val runManager = RunManager.getInstance(project)
    assertEmpty(runManager.allSettings)

    val initAction = myFixture.testAction(actions.first())
    assertNull(initAction.icon)
    assertEquals("Init src", initAction.text)
    testTfConfiguration(runManager.allSettings.first(), TfCommand.INIT)
    assertEquals(1, runManager.allSettings.size)

    val applyAction = myFixture.testAction(actions[3])
    assertNull(applyAction.icon)
    assertEquals(applyAction.text, "Apply src")
    testTfConfiguration(runManager.allSettings.first(), TfCommand.APPLY)
    assertEquals(2, runManager.allSettings.size)
  }

  private fun testTfConfiguration(settings: RunnerAndConfigurationSettings, mainCommand: TfCommand) {
    val configuration = settings.configuration
    assertInstanceOf(configuration, TfRunConfiguration::class.java)
    configuration as TfRunConfiguration

    assertEquals(mainCommand.command, configuration.programParameters)
    assertEquals("${myFixture.file.virtualFile.parent.toNioPathOrNull()}", configuration.workingDirectory)
    assertTrue(configuration.envs.isEmpty())
  }
}

private val runTemplateConfigActionsName = listOf("Init src", "Validate src", "Plan src", "Apply src", "Destroy src")