package org.intellij.terraform.runtime

import com.intellij.execution.RunManager
import com.intellij.execution.RunnerAndConfigurationSettings
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.vfs.toNioPathOrNull
import com.intellij.ui.IconManager

internal class TfRunLineMarkerContributorTest : BaseRunConfigurationTest() {

  fun testSimpleLineMarker() {
    myFixture.configureFromExistingVirtualFile(myFixture.copyFileToProject("simple.tf", "src/simple.tf"))
    val file = myFixture.file
    val info = file.findElementAt(myFixture.caretOffset)?.let { TfRunLineMarkerContributor().getInfo(it) }
    if (info == null) {
      fail("Info of RunLineMarker not should be empty")
      return
    }

    val warnedRun = IconManager.getInstance().createLayered(AllIcons.RunConfigurations.TestState.Run, AllIcons.Nodes.WarningMark)
    assertEquals(warnedRun, info.icon)
    runActionsAndCheckNames(info.actions)
  }

  fun testLineMarkerWithComment() {
    myFixture.configureFromExistingVirtualFile(myFixture.copyFileToProject("with_comment.tf", "src/with_comment.tf"))
    val file = myFixture.file
    val info = file.findElementAt(myFixture.caretOffset)?.let { TfRunLineMarkerContributor().getInfo(it) }
    if (info == null) {
      fail("Info of RunLineMarker not should be empty")
      return
    }

    val warnedRun = IconManager.getInstance().createLayered(AllIcons.RunConfigurations.TestState.Run, AllIcons.Nodes.WarningMark)
    assertEquals(warnedRun, info.icon)
    testRunConfigActions(info.actions)
    runActionsAndCheckNames(info.actions)

    val gutter = myFixture.findGutter("with_comment.tf")
    assertNotNull(gutter)
    assertEquals(warnedRun, gutter?.icon)
  }

  fun testLineMarkerWithWholeCommented() {
    val gutter = myFixture.findGutter("whole_commented.tf")
    assertNull(gutter)

    val gutters = myFixture.findAllGutters("whole_commented.tf")
    assertEmpty(gutters)
  }

  fun testNotDuplicatedRunConfig() {
    myFixture.configureFromExistingVirtualFile(myFixture.copyFileToProject("with_duplicated.tf", "src/with_duplicated.tf"))
    val file = myFixture.file
    val info = file.findElementAt(myFixture.caretOffset)?.let { TfRunLineMarkerContributor().getInfo(it) }
    if (info == null) {
      fail("Info of RunLineMarker not should be empty")
      return
    }

    val actions = info.actions
    val runnedAction = myFixture.testAction(actions.first())
    assertEquals("Init src", runnedAction.text)

    myFixture.type(" ")
    val updatedGutter = file.findElementAt(myFixture.caretOffset)?.let { TfRunLineMarkerContributor().getInfo(it) }
    if (updatedGutter == null) {
      fail("Info of RunLineMarker not should be empty")
      return
    }
    assertEquals(actions.size, updatedGutter.actions.size)
    val runManager = RunManager.getInstance(project)
    runManager.allSettings.forEach { runManager.removeConfiguration(it) }
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