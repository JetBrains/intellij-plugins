package org.intellij.terraform.runtime

import com.intellij.execution.RunManager
import com.intellij.execution.RunnerAndConfigurationSettings
import com.intellij.execution.lineMarker.RunLineMarkerContributor.Info
import com.intellij.icons.AllIcons
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.ui.IconManager
import org.intellij.terraform.TerraformTestUtils
import org.intellij.terraform.install.TfToolType

class TfRunLineMarkerContributorTest : BasePlatformTestCase() {
  override fun getTestDataPath(): String = TerraformTestUtils.getTestDataPath() + "/runtime"

  override fun setUp() {
    super.setUp()
    TerraformProjectSettings.getInstance(myFixture.project).toolPath = TfToolType.TERRAFORM.getBinaryName()
  }

  fun testSimpleLineMarker() {
    val file = myFixture.configureByFile("simple.tf")
    val info = file.findElementAt(myFixture.caretOffset)?.let { TfRunLineMarkerContributor().getInfo(it) }
    if (info == null) {
      fail("Info of RunLineMarker not should be empty")
      return
    }

    val warnedRun = IconManager.getInstance().createLayered(AllIcons.RunConfigurations.TestState.Run, AllIcons.Nodes.WarningMark)
    assertEquals(warnedRun, info.icon)
    checkActionNames(info)
  }

  fun testLineMarkerWithComment() {
    val file = myFixture.configureByFile("with_comment.tf")
    val info = file.findElementAt(myFixture.caretOffset)?.let { TfRunLineMarkerContributor().getInfo(it) }
    if (info == null) {
      fail("Info of RunLineMarker not should be empty")
      return
    }

    val warnedRun = IconManager.getInstance().createLayered(AllIcons.RunConfigurations.TestState.Run, AllIcons.Nodes.WarningMark)
    assertEquals(warnedRun, info.icon)
    testRunConfigActions(info)
    checkActionNames(info)

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

  private fun checkActionNames(info: Info) {
    val actions = info.actions
    assertEquals(actions?.last()?.templateText, "Edit Configurations…")

    val templateActions = actions?.take(5)
    val presentations = templateActions?.map { myFixture.testAction(it) }
    assertEquals(presentations?.size, runTemplateConfigActionsName.size)

    runTemplateConfigActionsName.forEachIndexed { index, name ->
      assertEquals(name, presentations?.get(index)?.text)
    }
  }

  private fun testRunConfigActions(info: Info) {
    val actions = info.actions
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
    assertInstanceOf(configuration, TerraformRunConfiguration::class.java)
    configuration as TerraformRunConfiguration

    assertEquals(mainCommand.command, configuration.programParameters)
    assertEquals("/src", configuration.workingDirectory)
    assertTrue(configuration.envs.isEmpty())
  }
}

private val runTemplateConfigActionsName = listOf("Init src", "Validate src", "Plan src", "Apply src", "Destroy src")