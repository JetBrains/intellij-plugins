package org.jetbrains.qodana.staticAnalysis.profile

import com.intellij.openapi.util.JDOMUtil
import org.intellij.lang.annotations.Language
import org.jdom.Element
import org.jetbrains.qodana.license.QodanaLicenseType
import org.jetbrains.qodana.staticAnalysis.QodanaTestCase
import org.jetbrains.qodana.staticAnalysis.inspections.config.InspectScope
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaConfig
import org.jetbrains.qodana.staticAnalysis.newProfileWithInspections
import org.junit.Test
import java.nio.file.Paths

class QodanaProfileTest : QodanaTestCase() {

  @Test
  fun `copy structured inspection options`() {
    val xml = """
      <profile version="1.0">
        <option name="myName" value="foo" />
        <inspection_tool class="SuspiciousPackagePrivateAccess" enabled="true" level="ERROR" enabled_by_default="true">
          <scope name="Tests" level="WARNING" enabled="false" />
          <option name="MODULES_SETS_LOADED_TOGETHER">
            <modules-set>
              <module name="intellij.css.impl" />
              <module name="intellij.css.psi" />
              <module name="intellij.css" />
              <module name="intellij.css.analysis" />
            </modules-set>
            <modules-set>
              <module name="intellij.spring.core" />
              <module name="intellij.spring" />
            </modules-set>
          </option>
        </inspection_tool>
      </profile>
    """.trimIndent()

    val effectiveProfile = parseXmlProfile(xml)

    assertEquals(xml, writeProfile(effectiveProfile))
  }

  @Test
  fun `copy unknown scope name`() {
    val xml = """
      <profile version="1.0">
        <option name="myName" value="foo" />
        <inspection_tool class="SuspiciousPackagePrivateAccess" enabled="true" level="ERROR" enabled_by_default="true">
          <scope name="SomeUnknownName" level="WARNING" enabled="false" />
        </inspection_tool>
      </profile>
    """.trimIndent()

    val effectiveProfile = parseXmlProfile(xml)

    assertEquals(xml, writeProfile(effectiveProfile))
  }

  @Test
  fun `copy simple inspection options`() {
    val xml = """
      <profile version="1.0">
        <option name="myName" value="foo" />
        <inspection_tool class="NullableProblems" enabled="true" level="WARNING" enabled_by_default="true">
          <option name="REPORT_NULLABLE_METHOD_OVERRIDES_NOTNULL" value="true" />
          <option name="REPORT_NOT_ANNOTATED_METHOD_OVERRIDES_NOTNULL" value="false" />
          <option name="REPORT_NOTNULL_PARAMETER_OVERRIDES_NULLABLE" value="true" />
          <option name="REPORT_NOT_ANNOTATED_PARAMETER_OVERRIDES_NOTNULL" value="false" />
          <option name="REPORT_NOT_ANNOTATED_GETTER" value="true" />
          <option name="IGNORE_EXTERNAL_SUPER_NOTNULL" value="true" />
          <option name="REPORT_NOT_ANNOTATED_SETTER_PARAMETER" value="true" />
          <option name="REPORT_ANNOTATION_NOT_PROPAGATED_TO_OVERRIDERS" value="true" />
          <option name="REPORT_NULLS_PASSED_TO_NON_ANNOTATED_METHOD" value="true" />
        </inspection_tool>
      </profile>
    """.trimIndent()

    val effectiveProfile = parseXmlProfile(xml)

    assertEquals(xml, writeProfile(effectiveProfile))
  }

  @Test
  fun `copy structural search inspection`() {
    @Language("XML")
    val xml = """
      <profile version="1.0">
        <option name="myName" value="foo" />
        <inspection_tool class="SSBasedInspection" enabled="true" level="WARNING" enabled_by_default="true">
          <searchConfiguration
              name="@HardwareAgentRequired should be applied only for performance tests"
              text="@com.intellij.idea.HardwareAgentRequired&#10;class ${'$'}class${'$'} {&#10;  &#10;}"
              recursive="true" caseInsensitive="true" type="JAVA" pattern_context="default">
            <constraint name="__context__" within="" contains="" />
            <constraint name="class" regexp=".*Performance.*" negateName="true" within="" contains="" />
          </searchConfiguration>
        </inspection_tool>
      </profile>
    """.trimIndent()
      // language= (to suppress the wrong XML error 'Top level element is not completed'; see IDEA-296469)
      .replace(Regex("\n +(\\w+)"), " $1") // join the attributes into a single line

    val effectiveProfile = parseXmlProfile(xml)

    assertEquals(2, effectiveProfile.getAllEnabledInspectionTools(project).size)
    assertEquals(xml, writeProfile(effectiveProfile))
  }

  @Test
  fun testOptions() {
    @Language("XML")
    val xml = """
      <profile version="1.0" is_locked="true">
        <option name="myName" value="foo.base"    />
        <inspection_tool class="NullableProblems" enabled="true" level="WARNING" enabled_by_default="true">
          <option name="REPORT_NULLABLE_METHOD_OVERRIDES_NOTNULL" value="true" />
          <option name="REPORT_NOT_ANNOTATED_METHOD_OVERRIDES_NOTNULL" value="false" />
          <option name="REPORT_NOTNULL_PARAMETER_OVERRIDES_NULLABLE" value="true" />
          <option name="REPORT_NOT_ANNOTATED_PARAMETER_OVERRIDES_NOTNULL" value="false" />
          <option name="REPORT_NOT_ANNOTATED_GETTER" value="true" />
          <option name="IGNORE_EXTERNAL_SUPER_NOTNULL" value="true" />
          <option name="REPORT_NOT_ANNOTATED_SETTER_PARAMETER" value="true" />
          <option name="REPORT_ANNOTATION_NOT_PROPAGATED_TO_OVERRIDERS" value="true" />
          <option name="REPORT_NULLS_PASSED_TO_NON_ANNOTATED_METHOD" value="true" />
        </inspection_tool>
      </profile>""".trimIndent()

    @Language("XML")
    val result = """
      <inspection_tool enabled="true" level="WARNING" enabled_by_default="true">
        <scope name="qodana.yaml.exclude.embedded.default" level="INFORMATION" enabled="false">
          <option name="REPORT_NULLABLE_METHOD_OVERRIDES_NOTNULL" value="true" />
          <option name="REPORT_NOT_ANNOTATED_METHOD_OVERRIDES_NOTNULL" value="false" />
          <option name="REPORT_NOTNULL_PARAMETER_OVERRIDES_NULLABLE" value="true" />
          <option name="REPORT_NOT_ANNOTATED_PARAMETER_OVERRIDES_NOTNULL" value="false" />
          <option name="REPORT_NOT_ANNOTATED_GETTER" value="true" />
          <option name="IGNORE_EXTERNAL_SUPER_NOTNULL" value="true" />
          <option name="REPORT_NOT_ANNOTATED_SETTER_PARAMETER" value="true" />
          <option name="REPORT_ANNOTATION_NOT_PROPAGATED_TO_OVERRIDERS" value="true" />
          <option name="REPORT_NULLS_PASSED_TO_NON_ANNOTATED_METHOD" value="true" />
        </scope>
        <option name="REPORT_NULLABLE_METHOD_OVERRIDES_NOTNULL" value="true" />
        <option name="REPORT_NOT_ANNOTATED_METHOD_OVERRIDES_NOTNULL" value="false" />
        <option name="REPORT_NOTNULL_PARAMETER_OVERRIDES_NULLABLE" value="true" />
        <option name="REPORT_NOT_ANNOTATED_PARAMETER_OVERRIDES_NOTNULL" value="false" />
        <option name="REPORT_NOT_ANNOTATED_GETTER" value="true" />
        <option name="IGNORE_EXTERNAL_SUPER_NOTNULL" value="true" />
        <option name="REPORT_NOT_ANNOTATED_SETTER_PARAMETER" value="true" />
        <option name="REPORT_ANNOTATION_NOT_PROPAGATED_TO_OVERRIDERS" value="true" />
        <option name="REPORT_NULLS_PASSED_TO_NON_ANNOTATED_METHOD" value="true" />
      </inspection_tool>""".trimIndent()

    val base = QodanaInspectionProfile.newFromXml(JDOMUtil.load(xml), "foo", QodanaInspectionProfileManager.getInstance(project))
    val config = QodanaConfig.fromYaml(Paths.get(project.basePath!!), Paths.get("unused"))
    val tools = MainInspectionGroup(base).applyConfig(config, project,
                                                      true).profile.getToolsOrNull("NullableProblems", project)
    val toolElem = Element("inspection_tool")
    tools!!.writeExternal(toolElem)
    assertEquals(result, JDOMUtil.writeElement(toolElem))
  }

  @Test
  fun `include a custom inspection`() {
    val profileManager = QodanaInspectionProfileManager.getInstance(project)
    val config = qodanaConfig { copy(include = listOf(InspectScope("unused"))) }

    val profile = QodanaInspectionProfile.newWithDisabledTools("", profileManager)
    val mainGroup = MainInspectionGroup(profile).applyConfig(config, project, false)
    val qodanaProfile = QodanaProfile(mainGroup, emptyList(), project, QodanaLicenseType.ULTIMATE_PLUS)

    val effectiveProfile = qodanaProfile.effectiveProfile

    val enabled = effectiveProfile.getAllEnabledInspectionTools(project)
    assertEquals(1, enabled.size)
    assertEquals("unused", enabled[0].shortName)
    assertEquals(mainGroup, qodanaProfile.idToEffectiveGroup["unused"])
  }

  @Test
  fun `disabled inspection without enabled scopes`() {
    val inspectionName = "UNUSED_IMPORT"
    val profile = newProfileWithInspections(inspectionName)
      .also { it.getToolDefaultState(inspectionName, project).isEnabled = false }

    val config = qodanaConfig { /* default */ this }
    val mainGroup = MainInspectionGroup(profile).applyConfig(config, project, false)
    val sanityGroup = SanityInspectionGroup("", profile).applyConfig(config, project, false)
    val promoGroup = PromoInspectionGroup("", profile).applyConfig(config, project, false)

    assertFalse(mainGroup.profile.getTools(inspectionName, project).isEnabled)
    assertFalse(sanityGroup.profile.getTools(inspectionName, project).isEnabled)
    assertFalse(promoGroup.profile.getTools(inspectionName, project).isEnabled)
  }

  @Test
  fun `enabled inspection with default scope`() {
    val inspectionName = "UNUSED_IMPORT"
    val profile = newProfileWithInspections(inspectionName)

    val config = qodanaConfig { /* default */ this }
    val mainGroup = MainInspectionGroup(profile).applyConfig(config, project, false)
    val sanityGroup = SanityInspectionGroup("", profile).applyConfig(config, project, false)
    val promoGroup = PromoInspectionGroup("", profile).applyConfig(config, project, false)

    assertTrue(mainGroup.profile.getTools(inspectionName, project).isEnabled)
    assertTrue(sanityGroup.profile.getTools(inspectionName, project).isEnabled)
    assertTrue(promoGroup.profile.getTools(inspectionName, project).isEnabled)
  }
}