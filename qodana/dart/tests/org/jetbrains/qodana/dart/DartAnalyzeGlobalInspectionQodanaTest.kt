package org.jetbrains.qodana.dart

import com.intellij.codeInspection.GlobalInspectionTool
import com.intellij.codeInspection.LocalInspectionEP
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.openapi.extensions.DefaultPluginDescriptor
import com.intellij.openapi.extensions.PluginId
import com.jetbrains.qodana.sarif.baseline.BaselineCalculation
import com.jetbrains.qodana.sarif.model.Level
import org.jetbrains.qodana.staticAnalysis.inspections.config.InspectScope
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaProfileConfig
import org.jetbrains.qodana.staticAnalysis.profile.DerivedInspection
import org.jetbrains.qodana.staticAnalysis.profile.DerivedInspectionProvider
import org.jetbrains.qodana.staticAnalysis.profile.DetectedProblemGroup
import org.jetbrains.qodana.staticAnalysis.testFramework.reinstantiateInspectionRelatedServices
import org.junit.Test

internal class DartAnalyzeGlobalInspectionQodanaTest : DartAnalyzeProjectQodanaTestCase() {
  override val analyzeResponses: Map<String, RecordedDartAnalyzeResponse> = mapOf(
    "" to readDartAnalyzeTestData("single-uncategorized.json"),
  )

  @Test
  fun `reports a Dart diagnostic through SARIF`() {
    runAnalysis()

    val ruleId = "undefined_identifier"
    val result = manager.sarifRun.results.single { it.ruleId == ruleId }
    assertEquals(ruleId, result.ruleId)
    assertEquals(DART_ANALYZE_SHORT_NAME, result.properties?.get("parentRuleId"))
    assertEquals(Level.ERROR, result.level)
    assertTrue(sarifRulesById().contains(result.ruleId))
    assertNotNull(result.partialFingerprints?.get(BaselineCalculation.EQUAL_INDICATOR, 1))
    assertNotNull(result.partialFingerprints?.get(BaselineCalculation.EQUAL_INDICATOR, 2))
    assertEquals(
      "[undefined_identifier] Undefined name 'missing'. Try correcting the name to one that is defined, or defining the name.",
      result.message.text,
    )
    val location = result.singleLocation().physicalLocation
    assertEquals("lib/main.dart", location.artifactLocation.uri)
    assertEquals(1, location.region.startLine)
    assertEquals(15, location.region.startColumn)
  }

  @Test
  fun `derived inspection reports a Dart rule ID and parent`() {
    runAnalysis()

    val derivedRuleId = "undefined_identifier"
    val derivedResult = manager.sarifRun.results.single { it.ruleId == derivedRuleId }
    val derivedRuleIds = manager.sarifRun.results.mapTo(mutableSetOf()) { it.ruleId }
    val rulesWithDerivedInspection = sarifRulesById()
    val derivedRule = checkNotNull(rulesWithDerivedInspection[derivedResult.ruleId])
    val parentRule = checkNotNull(rulesWithDerivedInspection[DART_ANALYZE_SHORT_NAME])
    val driver = manager.sarifRun.tool.driver
    val taxonomyRelationship = derivedRule.relationships.single()
    val taxonomyReference = taxonomyRelationship.target
    val taxonomy = driver.taxa[taxonomyReference.index]
    val parentTaxonomyReference = taxonomy.relationships.single().target
    assertEquals("Dart/Dart analyze", taxonomy.id)
    assertEquals("Dart analyze", taxonomy.name)
    assertEquals(parentRule.relationships, derivedRule.relationships)
    assertEquals(setOf("superset"), taxonomyRelationship.kinds)
    assertEquals(taxonomy.id, taxonomyReference.id)
    assertEquals(driver.name, taxonomyReference.toolComponent.name)
    assertEquals(driver.taxa[parentTaxonomyReference.index].id, parentTaxonomyReference.id)
    assertEquals(driver.name, parentTaxonomyReference.toolComponent.name)
    derivedRuleIds.forEach { ruleId ->
      assertEquals(derivedRule.relationships, checkNotNull(rulesWithDerivedInspection[ruleId]).relationships)
    }
    disableDerivedInspections()

    runAnalysis()

    val genericResult = manager.sarifRun.results.single { it.level == Level.ERROR }
    val rulesWithoutDerivedInspection = sarifRulesById()
    val genericRule = checkNotNull(rulesWithoutDerivedInspection[genericResult.ruleId])
    assertEquals(DART_ANALYZE_SHORT_NAME, genericResult.ruleId)
    assertEquals(DART_ANALYZE_SHORT_NAME, derivedResult.properties?.get("parentRuleId"))
    assertEquals(DART_ANALYZE_SHORT_NAME, derivedRule.properties?.get("parentRuleId"))
    assertNull(genericResult.properties?.get("parentRuleId"))
    assertEquals(rulesWithoutDerivedInspection.keys + derivedRuleIds, rulesWithDerivedInspection.keys)
    assertEquals("undefined_identifier", derivedRule.shortDescription.text)
    assertEquals("Reports Dart analyzer diagnostic \"undefined_identifier\".", derivedRule.fullDescription.text)
    assertTrue(genericRule.fullDescription.text != derivedRule.fullDescription.text)
    assertEquals(genericRule.defaultConfiguration, derivedRule.defaultConfiguration)
    assertEquals(genericRule.relationships, derivedRule.relationships)
    assertEquals(genericResult.level, derivedResult.level)
    assertEquals(genericResult.message, derivedResult.message)
    assertEquals(genericResult.locations, derivedResult.locations)
    assertEquals(genericResult.properties?.get("ideaSeverity"), derivedResult.properties?.get("ideaSeverity"))
    assertEquals(genericResult.properties?.get("qodanaSeverity"), derivedResult.properties?.get("qodanaSeverity"))
    assertEquals(genericResult.properties?.tags, derivedResult.properties?.tags)
    assertNotNull(genericResult.partialFingerprints?.get(BaselineCalculation.EQUAL_INDICATOR, 1))
    assertNotNull(derivedResult.partialFingerprints?.get(BaselineCalculation.EQUAL_INDICATOR, 1))
    assertNotNull(genericResult.partialFingerprints?.get(BaselineCalculation.EQUAL_INDICATOR, 2))
    assertNotNull(derivedResult.partialFingerprints?.get(BaselineCalculation.EQUAL_INDICATOR, 2))
  }

  @Test
  fun `a colliding unrelated inspection does not receive Dart diagnostics`() {
    // fallback to batch inspection in case of ID mismatch.
    // Might be more common in poly linter over time. think of the solution
    // unfortunately, it can change fingerprint, after a change in other poly linters' inspections triggers the fallback
    registerGlobalTool(CustomOtherTool())
    registerDiagnosticInspection("undefined_identifier", parentToolId = "someCustomOtherTool")

    runAnalysis()

    val result = manager.sarifRun.results.single { it.level == Level.ERROR }
    assertEquals(DART_ANALYZE_SHORT_NAME, result.ruleId)
    assertNull(result.properties?.get("parentRuleId"))
    assertNull(sarifRulesById().getValue("undefined_identifier").properties?.get("parentRuleId"))
  }

  @Test
  fun `a normal inspection linked to Dart analyze receives its diagnostics`() {
    registerDiagnosticInspection("undefined_identifier", parentToolId = DART_ANALYZE_SHORT_NAME)

    runAnalysis()

    val result = manager.sarifRun.results.single { it.level == Level.ERROR }
    assertEquals("undefined_identifier", result.ruleId)
    assertNull(sarifRulesById().getValue(result.ruleId).properties?.get("parentRuleId"))
  }

  @Test
  fun `ignores a derived inspection provider for another parent`() {
    DerivedInspectionProvider.EP_NAME.point.registerExtension(
      object : DerivedInspectionProvider {
        override fun isApplicableTo(parentInspectionId: String): Boolean = parentInspectionId == "OtherInspection"

        override fun describeDerivedInspections(
          project: com.intellij.openapi.project.Project?,
          inspections: Collection<DetectedProblemGroup>,
        ): List<DerivedInspection> = listOf(DerivedInspection("unexpected", DART_ANALYZE_SHORT_NAME, "Unexpected"))
      },
      testRootDisposable,
    )

    runAnalysis()

    assertFalse(sarifRulesById().containsKey("unexpected"))
  }

  @Test
  fun `Dart diagnostics preserve the severity of each diagnostic type`() {
    runAnalysis()

    assertEquals(Level.ERROR, manager.sarifRun.results.single { it.ruleId == "undefined_identifier" }.level)
    assertEquals(Level.WARNING, manager.sarifRun.results.single { it.ruleId == "unused_local_variable" }.level)
  }

  @Test
  fun `analyzes the project directory`() {
    runAnalysis()

    assertRecordedDartExecutions("analyze" to "")
  }

  @Test
  fun `analyzes with the single Dart inspection profile`() {
    updateQodanaConfig { it.copy(profile = QodanaProfileConfig.named("qodana.single:${DartAnalyzeGlobalInspection.SHORT_NAME}")) }
    runAnalysis()

    assertNotNull(manager.sarifRun.results.singleOrNull { it.ruleId == "undefined_identifier" })
  }

  private fun sarifRulesById() =
    (manager.sarifRun.tool.driver.rules + manager.sarifRun.tool.extensions.flatMap { it.rules }).associateBy { it.id }

  private fun registerDiagnosticInspection(@Suppress("SameParameterValue") inspectionId: String, parentToolId: String) {
    val inspection = object : LocalInspectionEP() {
      override fun instantiateTool(): LocalInspectionTool = object : LocalInspectionTool() {
        override fun getShortName(): String = inspectionId
        override fun getDisplayName(): String = "Undefined identifier"
        override fun getGroupDisplayName(): String = "Test inspections"
        override fun getStaticDescription(): String = "Reports undefined identifiers."
        override fun getMainToolId(): String = parentToolId
      }
    }
    inspection.implementationClass = LocalInspectionTool::class.java.name
    inspection.shortName = inspectionId
    inspection.displayName = "Undefined identifier"
    inspection.groupDisplayName = "Test inspections"
    inspection.level = "WARNING"
    inspection.enabledByDefault = true
    inspection.pluginDescriptor = DefaultPluginDescriptor(PluginId.getId("qodanaTest"), javaClass.classLoader)
    LocalInspectionEP.LOCAL_INSPECTION.point.registerExtension(inspection, testRootDisposable)
    reinstantiateInspectionRelatedServices(project, testRootDisposable)
    updateQodanaConfig { it.copy(include = listOf(InspectScope(inspectionId))) }
  }
}

internal class CustomOtherTool : GlobalInspectionTool() {
  override fun getShortName(): String = "someCustomOtherTool"
  override fun getDisplayName(): String = "Custom other tool"
  override fun getGroupDisplayName(): String = "Test inspections"
  override fun getStaticDescription(): String = "Runs an unrelated analysis tool."
}
