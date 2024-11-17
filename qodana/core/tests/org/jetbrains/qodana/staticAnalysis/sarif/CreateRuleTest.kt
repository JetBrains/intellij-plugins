package org.jetbrains.qodana.staticAnalysis.sarif

import com.intellij.codeHighlighting.HighlightDisplayLevel
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ex.LocalInspectionToolWrapper
import junit.framework.TestCase
import org.jetbrains.qodana.staticAnalysis.QodanaTestCase
import org.jetbrains.qodana.staticAnalysis.newProfileWithInspections
import org.junit.Test
import kotlinx.coroutines.test.runTest as coroutineTest

class CreateRuleTest : QodanaTestCase() {

  @Test
  fun `default configuration enabled`() = coroutineTest {
      val inspectionsNames = arrayOf("UNUSED_IMPORT", "HardCodedStringLiteral", "JavaReflectionInvocation")
      val profile = newProfileWithInspections(*inspectionsNames)

      val rules = inspectionsNames.map {
        val tool = profile.getToolsOrNull(it, project)!!
        createRule(tool, 1, "")
      }

      rules.forEach {
        assertTrue(it.defaultConfiguration.enabled)
      }
  }

  @Test
  fun `exclusion scopes`() = coroutineTest {
      val inspectionName = "UNUSED_IMPORT"
      val profile = newProfileWithInspections(inspectionName)
      val tools = profile.getToolsOrNull(inspectionName, project)!!

      tools.addTool("disabled scope", tools.defaultState.tool, false, HighlightDisplayLevel.DO_NOT_SHOW)

      val rule = createRule(tools, 1, "")
      assertTrue(rule.defaultConfiguration.enabled)
  }

  @Test
  fun `enabled with scopes`() = coroutineTest {
    val inspectionName = "UNUSED_IMPORT"
    val profile = newProfileWithInspections(inspectionName)
    val tools = profile.getToolsOrNull(inspectionName, project)!!

    tools.addTool("enabled scope", tools.defaultState.tool, true, HighlightDisplayLevel.WARNING)
    tools.addTool("another scope", tools.defaultState.tool, true, HighlightDisplayLevel.ERROR)
      val rule = createRule(tools, 1, "")
      assertTrue(rule.defaultConfiguration.enabled)
  }

  @Test
  fun `default disabled with enabled scopes`() = coroutineTest {
      val inspectionName = "UNUSED_IMPORT"
      val profile = newProfileWithInspections(inspectionName)
      val tools = profile.getToolsOrNull(inspectionName, project)!!
      tools.defaultState.isEnabled = false

      tools.addTool("enabled scope", tools.defaultState.tool, true, HighlightDisplayLevel.WARNING)
      val rule = createRule(tools, 1, "")
      assertTrue(rule.defaultConfiguration.enabled)
  }

  @Test
  fun `whole inspection disabled`() = coroutineTest {
      val inspectionName = "UNUSED_IMPORT"
      val profile = newProfileWithInspections(inspectionName)
      val tools = profile.getToolsOrNull(inspectionName, project)!!
      tools.addTool("enabled scope", tools.defaultState.tool, true, HighlightDisplayLevel.WARNING)

      tools.isEnabled = false
      tools.defaultState.isEnabled = false

      val rule = createRule(tools, 1, "")

      assertFalse(rule.defaultConfiguration.enabled)
  }

  @Test
  fun `tool without description produces default description`() = coroutineTest {
    val inspectionName = "UNUSED_IMPORT"
    val tool = object : LocalInspectionTool() {
      override fun getShortName(): String = "no-description"
    }
    val profile = newProfileWithInspections(inspectionName)
    val tools = profile.getToolsOrNull(inspectionName, project)!!
    tools.addTool("enabled scope", LocalInspectionToolWrapper(tool), true, HighlightDisplayLevel.WARNING)

    val rule = createRule(tools, 1, "")
    TestCase.assertEquals("No description available", rule.fullDescription?.text)
    TestCase.assertEquals("No description available", rule.fullDescription?.markdown)
  }
}
