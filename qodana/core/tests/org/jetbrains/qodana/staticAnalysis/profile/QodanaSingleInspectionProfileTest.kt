package org.jetbrains.qodana.staticAnalysis.profile

import com.intellij.codeInsight.daemon.impl.HighlightVisitorBasedInspection
import org.jetbrains.qodana.license.QodanaLicenseType
import org.jetbrains.qodana.staticAnalysis.QodanaTestCase
import org.jetbrains.qodana.staticAnalysis.inspections.config.InspectScope
import org.junit.Test

class QodanaSingleInspectionProfileTest : QodanaTestCase() {
  @Test
  fun `generate profile`() {
    val profile = QodanaInspectionProfileProvider.runProviders("qodana.single:unused", project)

    val tools = profile!!.getAllEnabledInspectionTools(project)

    assertEquals(1, tools.size)
    assertEquals("unused", tools[0].shortName)
    assertEquals(1, tools[0].tools.size)
  }

  @Test
  fun exclude() {
    val profile = QodanaInspectionProfileProvider.runProviders("qodana.single:unused", project)
    val config = qodanaConfig { copy(exclude = listOf(InspectScope("unused", listOf("path")))) }
    val qodanaProfile = QodanaProfile(MainInspectionGroup(profile!!).applyConfig(config, project, false), emptyList(), project,
                                      config.license.type)

    val effectiveProfile = qodanaProfile.effectiveProfile

    assertEquals("""
      <profile version="1.0">
        <option name="myName" value="qodana.effective.profile" />
        <inspection_tool class="unused" enabled="true" level="WARNING" enabled_by_default="true">
          <scope name="qodana.yaml.exclude.unused" level="INFORMATION" enabled="false" checkParameterExcludingHierarchy="false">
            <option name="LOCAL_VARIABLE" value="true" />
            <option name="FIELD" value="true" />
            <option name="METHOD" value="true" />
            <option name="CLASS" value="true" />
            <option name="PARAMETER" value="true" />
            <option name="REPORT_PARAMETER_FOR_PUBLIC_METHODS" value="true" />
            <option name="ADD_MAINS_TO_ENTRIES" value="true" />
            <option name="ADD_APPLET_TO_ENTRIES" value="true" />
            <option name="ADD_SERVLET_TO_ENTRIES" value="true" />
            <option name="ADD_NONJAVA_TO_ENTRIES" value="true" />
          </scope>
        </inspection_tool>
      </profile>
    """.trimIndent(), writeProfile(effectiveProfile))
  }

  @Test
  fun `include and exclude`() {
    val profile = QodanaInspectionProfileProvider.runProviders("qodana.single:unused", project)
    val config = qodanaConfig {
      copy(
        exclude = listOf(InspectScope("unused", listOf("path"))),
        include = listOf(InspectScope(HighlightVisitorBasedInspection.SHORT_NAME, listOf("path")))
      )
    }
    val qodanaProfile = QodanaProfile(MainInspectionGroup(profile!!).applyConfig(config, project, false), emptyList(), project,
                                      config.license.type)

    val effectiveProfile = qodanaProfile.effectiveProfile

    assertEquals("""
      <profile version="1.0">
        <option name="myName" value="qodana.effective.profile" />
        <inspection_tool class="Annotator" enabled="true" level="ERROR" enabled_by_default="false">
          <scope name="qodana.yaml.include.Annotator" level="ERROR" enabled="true" />
        </inspection_tool>
        <inspection_tool class="unused" enabled="true" level="WARNING" enabled_by_default="true">
          <scope name="qodana.yaml.exclude.unused" level="INFORMATION" enabled="false" checkParameterExcludingHierarchy="false">
            <option name="LOCAL_VARIABLE" value="true" />
            <option name="FIELD" value="true" />
            <option name="METHOD" value="true" />
            <option name="CLASS" value="true" />
            <option name="PARAMETER" value="true" />
            <option name="REPORT_PARAMETER_FOR_PUBLIC_METHODS" value="true" />
            <option name="ADD_MAINS_TO_ENTRIES" value="true" />
            <option name="ADD_APPLET_TO_ENTRIES" value="true" />
            <option name="ADD_SERVLET_TO_ENTRIES" value="true" />
            <option name="ADD_NONJAVA_TO_ENTRIES" value="true" />
          </scope>
        </inspection_tool>
      </profile>
    """.trimIndent(), writeProfile(effectiveProfile))
  }

  @Test
  fun `include and exclude 2`() {
    val profile = QodanaInspectionProfileProvider.runProviders("qodana.single:unused", project)
    val config = qodanaConfig {
      copy(
        exclude = listOf(InspectScope("unused", listOf("path"))),
        include = listOf(InspectScope(HighlightVisitorBasedInspection.SHORT_NAME))
      )
    }
    val qodanaProfile = QodanaProfile(MainInspectionGroup(profile!!).applyConfig(config, project, false), emptyList(), project,
                                      config.license.type)

    val effectiveProfile = qodanaProfile.effectiveProfile

    assertEquals("""
      <profile version="1.0">
        <option name="myName" value="qodana.effective.profile" />
        <inspection_tool class="Annotator" enabled="true" level="ERROR" enabled_by_default="true" />
        <inspection_tool class="unused" enabled="true" level="WARNING" enabled_by_default="true">
          <scope name="qodana.yaml.exclude.unused" level="INFORMATION" enabled="false" checkParameterExcludingHierarchy="false">
            <option name="LOCAL_VARIABLE" value="true" />
            <option name="FIELD" value="true" />
            <option name="METHOD" value="true" />
            <option name="CLASS" value="true" />
            <option name="PARAMETER" value="true" />
            <option name="REPORT_PARAMETER_FOR_PUBLIC_METHODS" value="true" />
            <option name="ADD_MAINS_TO_ENTRIES" value="true" />
            <option name="ADD_APPLET_TO_ENTRIES" value="true" />
            <option name="ADD_SERVLET_TO_ENTRIES" value="true" />
            <option name="ADD_NONJAVA_TO_ENTRIES" value="true" />
          </scope>
        </inspection_tool>
      </profile>
    """.trimIndent(), writeProfile(effectiveProfile))
  }

  @Test
  fun `include and exclude 3`() {
    val profile = QodanaInspectionProfileProvider.runProviders("qodana.single:unused", project)
    val config = qodanaConfig {
      copy(
        exclude = listOf(InspectScope("unused", listOf("path")), InspectScope("All", listOf("exclPath"))),
        include = listOf(InspectScope(HighlightVisitorBasedInspection.SHORT_NAME))
      )
    }
    val qodanaProfile = QodanaProfile(MainInspectionGroup(profile!!).applyConfig(config, project, false), emptyList(), project,
                                      config.license.type)

    val effectiveProfile = qodanaProfile.effectiveProfile

    assertEquals("""
<profile version="1.0">
  <option name="myName" value="qodana.effective.profile" />
  <inspection_tool class="Annotator" enabled="true" level="ERROR" enabled_by_default="true">
    <scope name="qodana.yaml.exclude.All" level="INFORMATION" enabled="false" />
  </inspection_tool>
  <inspection_tool class="unused" enabled="true" level="WARNING" enabled_by_default="true">
    <scope name="qodana.yaml.exclude.All" level="INFORMATION" enabled="false" checkParameterExcludingHierarchy="false">
      <option name="LOCAL_VARIABLE" value="true" />
      <option name="FIELD" value="true" />
      <option name="METHOD" value="true" />
      <option name="CLASS" value="true" />
      <option name="PARAMETER" value="true" />
      <option name="REPORT_PARAMETER_FOR_PUBLIC_METHODS" value="true" />
      <option name="ADD_MAINS_TO_ENTRIES" value="true" />
      <option name="ADD_APPLET_TO_ENTRIES" value="true" />
      <option name="ADD_SERVLET_TO_ENTRIES" value="true" />
      <option name="ADD_NONJAVA_TO_ENTRIES" value="true" />
    </scope>
    <scope name="qodana.yaml.exclude.unused" level="INFORMATION" enabled="false" checkParameterExcludingHierarchy="false">
      <option name="LOCAL_VARIABLE" value="true" />
      <option name="FIELD" value="true" />
      <option name="METHOD" value="true" />
      <option name="CLASS" value="true" />
      <option name="PARAMETER" value="true" />
      <option name="REPORT_PARAMETER_FOR_PUBLIC_METHODS" value="true" />
      <option name="ADD_MAINS_TO_ENTRIES" value="true" />
      <option name="ADD_APPLET_TO_ENTRIES" value="true" />
      <option name="ADD_SERVLET_TO_ENTRIES" value="true" />
      <option name="ADD_NONJAVA_TO_ENTRIES" value="true" />
    </scope>
  </inspection_tool>
</profile>
    """.trimIndent(), writeProfile(effectiveProfile))
  }

  @Test
  fun `include and exclude 4`() {
    val profile = QodanaInspectionProfileProvider.runProviders("qodana.single:unused", project)
    val config = qodanaConfig {
      copy(
        exclude = listOf(InspectScope("All", listOf("exclPath"))),
        include = listOf(InspectScope(HighlightVisitorBasedInspection.SHORT_NAME, listOf("path")))
      )
    }
    val qodanaProfile = QodanaProfile(MainInspectionGroup(profile!!).applyConfig(config, project, false), emptyList(), project,
                                      config.license.type)

    val effectiveProfile = qodanaProfile.effectiveProfile

    assertEquals("""
<profile version="1.0">
  <option name="myName" value="qodana.effective.profile" />
  <inspection_tool class="Annotator" enabled="true" level="ERROR" enabled_by_default="false">
    <scope name="qodana.yaml.include.Annotator" level="ERROR" enabled="true" />
    <scope name="qodana.yaml.exclude.All" level="INFORMATION" enabled="false" />
  </inspection_tool>
  <inspection_tool class="unused" enabled="true" level="WARNING" enabled_by_default="true">
    <scope name="qodana.yaml.exclude.All" level="INFORMATION" enabled="false" checkParameterExcludingHierarchy="false">
      <option name="LOCAL_VARIABLE" value="true" />
      <option name="FIELD" value="true" />
      <option name="METHOD" value="true" />
      <option name="CLASS" value="true" />
      <option name="PARAMETER" value="true" />
      <option name="REPORT_PARAMETER_FOR_PUBLIC_METHODS" value="true" />
      <option name="ADD_MAINS_TO_ENTRIES" value="true" />
      <option name="ADD_APPLET_TO_ENTRIES" value="true" />
      <option name="ADD_SERVLET_TO_ENTRIES" value="true" />
      <option name="ADD_NONJAVA_TO_ENTRIES" value="true" />
    </scope>
  </inspection_tool>
</profile>
""".trimIndent(), writeProfile(effectiveProfile))
  }

  @Test
  fun `exclude 2`() {
    val profile = QodanaInspectionProfileProvider.runProviders("qodana.single:RedundantThrows", project)
    val config = qodanaConfig("""
      version: "1.0"
      exclude:
        - name: All
          paths:
            - src/com/maddyhome/idea/vim/regexp/RegExp.java
        - name: RedundantThrows
          paths:
            - src/com/maddyhome/idea/vim/ex/vimscript/VimScriptCommandHandler.java
    """.trimIndent())
    val qodanaProfile = QodanaProfile(MainInspectionGroup(profile!!).applyConfig(config, project, false), emptyList(), project,
                                      QodanaLicenseType.ULTIMATE_PLUS)

    val effectiveProfile = qodanaProfile.effectiveProfile


    assertEquals("""
      <profile version="1.0">
        <option name="myName" value="qodana.effective.profile" />
        <inspection_tool class="RedundantThrows" enabled="true" level="WARNING" enabled_by_default="true">
          <scope name="qodana.yaml.exclude.RedundantThrows" level="INFORMATION" enabled="false" />
          <scope name="qodana.yaml.exclude.All" level="INFORMATION" enabled="false" />
        </inspection_tool>
      </profile>
    """.trimIndent(), writeProfile(effectiveProfile))

    //val scopeName = effectiveProfile.getTools("RedundantThrows", project).nonDefaultTools!![0].scopeName
    //val scope = NamedScopeManager.getScope(project, scopeName)
    //assertEquals("Scope 'qodana.yaml.exclude.RedundantThrows'; set:src/com/maddyhome/idea/vim/ex/vimscript/VimScriptCommandHandler.java", scope.toString())
  }
}