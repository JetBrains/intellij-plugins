package org.jetbrains.qodana.staticAnalysis.sarif

import com.intellij.codeHighlighting.HighlightDisplayLevel
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ex.LocalInspectionToolWrapper
import com.jetbrains.qodana.sarif.model.Tool
import junit.framework.TestCase
import org.jetbrains.qodana.license.QodanaLicenseType
import org.jetbrains.qodana.staticAnalysis.QodanaTestCase
import org.jetbrains.qodana.staticAnalysis.newProfileWithInspections
import org.jetbrains.qodana.staticAnalysis.profile.DerivedInspection
import org.jetbrains.qodana.staticAnalysis.profile.MainInspectionGroup
import org.jetbrains.qodana.staticAnalysis.profile.QodanaProfile
import org.junit.Test
import kotlinx.coroutines.test.runTest as coroutineTest

class CreateRuleTest : QodanaTestCase() {

  @Test
  fun `derived rule is added beside its parent and inherits its metadata`() = coroutineTest {
    val inspectionId = "UNUSED_IMPORT"
    val inspectionProfile = newProfileWithInspections(inspectionId)
    val qodanaProfile = QodanaProfile(
      MainInspectionGroup(inspectionProfile),
      emptyList(),
      project,
      QodanaLicenseType.ULTIMATE_PLUS,
    )
    val tool = Tool()
    fillComponents(tool, qodanaProfile)

    val components = listOfNotNull(tool.driver) + tool.extensions
    val parentComponent = components.single { component -> component.rules.any { it.id == inspectionId } }
    val parentRule = parentComponent.rules.single { it.id == inspectionId }

    appendDerivedComponents(
      tool,
      qodanaProfile,
      listOf(DerivedInspection("derived_unused_import", inspectionId, "Derived unused import")),
    )

    val derivedComponent = components.single { component -> component.rules.any { it.id == "derived_unused_import" } }
    val derivedRule = derivedComponent.rules.single { it.id == "derived_unused_import" }
    assertSame(parentComponent, derivedComponent)
    assertEquals("Derived unused import", derivedRule.shortDescription.text)
    assertEquals(parentRule.fullDescription, derivedRule.fullDescription)
    assertEquals(parentRule.defaultConfiguration, derivedRule.defaultConfiguration)
    assertEquals(parentRule.relationships, derivedRule.relationships)
  }

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
