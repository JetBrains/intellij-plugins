package org.jetbrains.qodana.staticAnalysis.sarif

import com.jetbrains.qodana.sarif.model.ReportingDescriptor
import com.jetbrains.qodana.sarif.model.Tool
import com.jetbrains.qodana.sarif.model.ToolComponent
import org.jetbrains.qodana.staticAnalysis.profile.DerivedInspection
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class AppendDerivedComponentsTest {
  @Test
  fun `derived inspection is added to the parent extension`() {
    val parent = ReportingDescriptor("ParentInspection")
    val driver = ToolComponent("Qodana").withRules(mutableListOf())
    val extension = ToolComponent("Plugin").withRules(mutableListOf(parent))
    val tool = Tool(driver).withExtensions(setOf(extension))
    val inspection = DerivedInspection("DerivedInspection", parent.id, "Derived inspection", "Derived description")

    appendDerivedComponents(tool, listOf(inspection))

    assertEquals(listOf(parent.id, inspection.id), extension.rules.map { it.id })
    assertEquals(parent.id, extension.rules.last().properties?.get("parentRuleId"))
    assertEquals(emptyList<ReportingDescriptor>(), driver.rules)
  }

  @Test
  fun `derived inspection is added to driver without extensions`() {
    val parent = ReportingDescriptor("ParentInspection")
    val driver = ToolComponent("Qodana").withRules(mutableListOf(parent))
    val tool = Tool(driver)
    val inspection = DerivedInspection("DerivedInspection", parent.id, "Derived inspection", "Derived description")
    assertNull(tool.extensions)

    appendDerivedComponents(tool, listOf(inspection))

    assertEquals(listOf(parent.id, inspection.id), driver.rules.map { it.id })
    val derivedRule = driver.rules.last()
    assertEquals(inspection.displayName, derivedRule.shortDescription.text)
    assertEquals(inspection.description, derivedRule.fullDescription.text)
    assertEquals(parent.id, derivedRule.properties?.get("parentRuleId"))
  }

  @Test
  fun `no errors and empty results without extensions`() {
    val driver = ToolComponent("Qodana").withRules(mutableListOf())
    val tool = Tool(driver)
    // extensions are null to mimic PHP setup (QodanaPhpIntegrationTest.testPhpVersionMigration)
    assertNull(tool.extensions)

    appendDerivedComponents(tool, emptyList())

    assertEquals(emptyList<ReportingDescriptor>(), driver.rules)
  }
}
