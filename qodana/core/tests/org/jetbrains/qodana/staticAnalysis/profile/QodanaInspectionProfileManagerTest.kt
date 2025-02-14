package org.jetbrains.qodana.staticAnalysis.profile

import com.intellij.codeInspection.ex.InspectionProfileImpl
import com.intellij.codeInspection.ex.InspectionToolRegistrar
import com.intellij.codeInspection.ex.InspectionToolWrapper
import com.intellij.openapi.util.JDOMUtil
import com.intellij.profile.codeInspection.BaseInspectionProfileManager
import com.intellij.profile.codeInspection.InspectionProfileManager
import org.assertj.core.api.Assertions.assertThat
import org.intellij.lang.annotations.Language
import org.jetbrains.qodana.staticAnalysis.ConfigTester
import org.jetbrains.qodana.staticAnalysis.QodanaTestCase
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaException
import org.jetbrains.qodana.staticAnalysis.inspections.runner.externalTools.ExternalInspectionDescriptor
import org.jetbrains.qodana.staticAnalysis.profile.providers.QODANA_EMPTY_PROFILE_NAME
import org.jetbrains.qodana.staticAnalysis.registerDynamicExternalInspectionsInTests
import org.jetbrains.qodana.staticAnalysis.testFramework.reinstantiateInspectionRelatedServices
import org.junit.Ignore
import org.junit.Test

class QodanaInspectionProfileManagerTest : QodanaTestCase() {
  private val qodanaApplicationInspectionProfileManager: QodanaApplicationInspectionProfileManager
    get() = QodanaApplicationInspectionProfileManager.getInstance()

  private val configTester = ConfigTester()

  private lateinit var dynamicExternalInspections: List<ExternalInspectionDescriptor>

  override fun setUp() {
    super.setUp()
    reinstantiateInspectionRelatedServices(project, testRootDisposable)
    dynamicExternalInspections = registerDynamicExternalInspectionsInTests(configTester, testRootDisposable).describeTools(project)
  }

  private fun createQodanaToolsAssertingDynamicExternalTools(): List<InspectionToolWrapper<*, *>> {
    val qodanaTools = QodanaToolRegistrar.getInstance().createTools()
    val qodanaToolsIds = qodanaTools.map { it.shortName }.toSet()

    dynamicExternalInspections.forEach { dynamicInspection ->
      assertThat(dynamicInspection.shortName).isIn(qodanaToolsIds)
    }

    return qodanaTools
  }

  @Test
  fun `empty qodana profile is locked and has all tools disabled`() {
    val qodanaTools = createQodanaToolsAssertingDynamicExternalTools()

    val emptyProfile = qodanaApplicationInspectionProfileManager.createQodanaEmptyProfile()
    val toolsInEmptyProfile = emptyProfile.tools

    assertThat(emptyProfile.name).isEqualTo(QODANA_EMPTY_PROFILE_NAME)
    assertThat(emptyProfile.isProfileLocked).isTrue

    assertThat(toolsInEmptyProfile.map { it.shortName }.toSet())
      .isEqualTo(qodanaTools.map { it.shortName }.toSet())

    assertThat(toolsInEmptyProfile.all { !it.isEnabled }).isTrue
  }

  @Test
  fun `base qodana profile is not locked and has tools enabled if they're enabled by default`() {
    val qodanaTools = createQodanaToolsAssertingDynamicExternalTools()

    val baseProfile = qodanaApplicationInspectionProfileManager.createQodanaBaseProfile()
    val toolsInBaseProfile = baseProfile.tools

    assertThat(baseProfile.name).isEqualTo(QODANA_BASE_PROFILE_NAME)
    assertThat(baseProfile.isProfileLocked).isFalse

    assertThat(toolsInBaseProfile.map { it.shortName }.toSet())
      .isEqualTo(qodanaTools.map { it.shortName }.toSet())

    val qodanaToolsById = qodanaTools.associateBy { it.shortName }
    assertThat(toolsInBaseProfile.all { it.isEnabled == qodanaToolsById[it.shortName]!!.isEnabledByDefault }).isTrue
  }

  @Test
  fun `profile based on empty qodana profile is not locked and has all tools disabled`() {
    val qodanaTools = createQodanaToolsAssertingDynamicExternalTools()

    val profileBasedOnEmpty = QodanaInspectionProfile.newWithDisabledTools("foo", qodanaApplicationInspectionProfileManager)
    val toolsInProfileBasedOnEmptyProfile = profileBasedOnEmpty.tools

    assertThat(profileBasedOnEmpty.name).isEqualTo("foo")
    assertThat(profileBasedOnEmpty.isProfileLocked).isFalse

    assertThat(toolsInProfileBasedOnEmptyProfile.map { it.shortName }.toSet())
      .isEqualTo(qodanaTools.map { it.shortName }.toSet())

    assertThat(toolsInProfileBasedOnEmptyProfile.all { !it.isEnabled }).isTrue
  }

  @Test
  fun `profile based on base qodana profile is not locked and has tools enabled if they're enabled by default`() {
    val qodanaTools = createQodanaToolsAssertingDynamicExternalTools()

    val profileBasedOnBase = QodanaInspectionProfile.newWithEnabledByDefaultTools("foo", qodanaApplicationInspectionProfileManager)
    val toolsInProfileBasedOnBaseProfile = profileBasedOnBase.tools

    assertThat(profileBasedOnBase.name).isEqualTo("foo")
    assertThat(profileBasedOnBase.isProfileLocked).isFalse

    assertThat(toolsInProfileBasedOnBaseProfile.map { it.shortName }.toSet())
      .isEqualTo(qodanaTools.map { it.shortName }.toSet())

    val qodanaToolsById = qodanaTools.associateBy { it.shortName }
    assertThat(toolsInProfileBasedOnBaseProfile.all { it.isEnabled == qodanaToolsById[it.shortName]!!.isEnabledByDefault }).isTrue
  }

  @Test
  fun `profile read from not locked xml profile has tools enabled if they're enabled by default`() {
    val qodanaTools = createQodanaToolsAssertingDynamicExternalTools()
    val toolNotEnabledByDefault = qodanaTools.first { !it.isEnabledByDefault }

    @Language("XML")
    val xml = """
        <profile version="1.0">
            <option name="myName" value="foo"/>
            <inspection_tool class="${toolNotEnabledByDefault.shortName}" enabled="true" level="WARNING" enabled_by_default="false"/>
        </profile>
      """.trimIndent()

    val element = JDOMUtil.load(xml)
    val profileFromXml = QodanaInspectionProfile.newFromXml(element, name = null, qodanaApplicationInspectionProfileManager)
    val toolsInProfileFromXml = profileFromXml.tools

    assertThat(profileFromXml.name).isEqualTo("foo")
    assertThat(profileFromXml.isProfileLocked).isFalse

    assertThat(toolsInProfileFromXml.map { it.shortName }.toSet())
      .isEqualTo(qodanaTools.map { it.shortName }.toSet())

    val qodanaToolsById = qodanaTools.associateBy { it.shortName }
    assertThat(toolsInProfileFromXml
                 .filterNot { it.shortName == toolNotEnabledByDefault.shortName }
                 .all { it.isEnabled == qodanaToolsById[it.shortName]!!.isEnabledByDefault }).isTrue
    assertThat(toolsInProfileFromXml.first { it.shortName == toolNotEnabledByDefault.shortName }.isEnabled).isTrue
  }

  @Test
  fun `profile read from locked xml profile has only tools enabled in read xml profile`() {
    val qodanaTools = createQodanaToolsAssertingDynamicExternalTools()
    val toolNotEnabledByDefault = qodanaTools.first { !it.isEnabledByDefault }

    @Language("XML")
    val xml = """
        <profile version="1.0" is_locked="true">
            <option name="myName" value="foo"/>
            <inspection_tool class="${toolNotEnabledByDefault.shortName}" enabled="true" level="WARNING" enabled_by_default="false"/>
        </profile>
      """.trimIndent()

    val element = JDOMUtil.load(xml)
    val profileFromXml = QodanaInspectionProfile.newFromXml(element, name = null, qodanaApplicationInspectionProfileManager)
    val toolsInProfileFromXml = profileFromXml.tools

    assertThat(profileFromXml.name).isEqualTo("foo")
    assertThat(profileFromXml.isProfileLocked).isTrue

    assertThat(toolsInProfileFromXml.map { it.shortName }.toSet())
      .isEqualTo(qodanaTools.map { it.shortName }.toSet())

    assertThat(toolsInProfileFromXml
                 .filterNot { it.shortName == toolNotEnabledByDefault.shortName }
                 .all { !it.isEnabled }).isTrue
    assertThat(toolsInProfileFromXml.first { it.shortName == toolNotEnabledByDefault.shortName }.isEnabled).isTrue
  }

  @Test
  @Ignore
  fun `successfully clone from profile with qodana registrar and manager`() {
    val sourceInspectionProfile = InspectionProfileImpl(
      "",
      QodanaToolRegistrar.getInstance(),
      qodanaApplicationInspectionProfileManager
    )
    val clonedQodanaInspectionProfile = QodanaInspectionProfile.clone(
      sourceInspectionProfile,
      "",
      qodanaApplicationInspectionProfileManager
    )
    assertThat(clonedQodanaInspectionProfile.tools.toSet()).isEqualTo(sourceInspectionProfile.tools.toSet())
  }

  @Test
  fun `fail clone from profile with not qodana registrar`() {
    val inspectionProfile = InspectionProfileImpl(
      "inspectionProfileImpl",
      InspectionToolRegistrar.getInstance(),
      qodanaApplicationInspectionProfileManager
    )
    assertThrows(QodanaException::class.java) { QodanaInspectionProfile.clone (inspectionProfile, "", qodanaApplicationInspectionProfileManager) }
  }

  @Test
  fun `fail clone from profile with not qodana profile manager`() {
    val inspectionProfile = InspectionProfileImpl(
      "inspectionProfileImpl",
      QodanaToolRegistrar.getInstance(),
      InspectionProfileManager.getInstance(project) as BaseInspectionProfileManager
    )
    assertThrows(QodanaException::class.java) { QodanaInspectionProfile.clone (inspectionProfile, "", qodanaApplicationInspectionProfileManager) }
  }

  @Test
  fun `serialized profile with base null profile doesn't preserve modifications`() {
    val profileWithNullBaseProfile = QodanaInspectionProfile(
      "foo",
      qodanaApplicationInspectionProfileManager,
      baseProfile = null
    )

    val notEnabledTool = profileWithNullBaseProfile.tools.first { !it.isEnabled }
    notEnabledTool.isEnabled = true

    @Language("XML")
    val expectedXmlWithoutChangedTool = """
      <profile version="1.0">
        <option name="myName" value="foo" />
      </profile>
    """.trimIndent()
    assertThat(writeProfile(profileWithNullBaseProfile)).isEqualTo(expectedXmlWithoutChangedTool)
  }

  @Test
  fun `serialized empty qodana profile doesn't preserve modifications (because it has null as a base)`() {
    val emptyProfile = qodanaApplicationInspectionProfileManager.createQodanaEmptyProfile()

    val notEnabledTool = emptyProfile.tools.first { !it.isEnabled }
    notEnabledTool.isEnabled = true

    @Language("XML")
    val expectedXmlWithoutChangedTool = """
      <profile version="1.0" is_locked="true">
        <option name="myName" value="$QODANA_EMPTY_PROFILE_NAME" />
      </profile>
    """.trimIndent()
    assertThat(writeProfile(emptyProfile)).isEqualTo(expectedXmlWithoutChangedTool)
  }

  @Test
  fun `serialized base qodana profile doesn't preserve modifications (because it has null as a base)`() {
    val baseProfile = qodanaApplicationInspectionProfileManager.createQodanaBaseProfile()

    val notEnabledTool = baseProfile.tools.first { !it.isEnabled }
    notEnabledTool.isEnabled = true

    @Language("XML")
    val expectedXmlWithoutChangedTool = """
      <profile version="1.0">
        <option name="myName" value="$QODANA_BASE_PROFILE_NAME" />
      </profile>
    """.trimIndent()
    assertThat(writeProfile(baseProfile)).isEqualTo(expectedXmlWithoutChangedTool)
  }

  @Test
  fun `serialized profile based on empty qodana profile preserves modifications`() {
    val profileBasedOnEmptyProfile = QodanaInspectionProfile.newWithDisabledTools("foo", qodanaApplicationInspectionProfileManager)

    val notEnabledTool = profileBasedOnEmptyProfile.tools.first { !it.isEnabled }
    notEnabledTool.isEnabled = true

    @Language("XML")
    val expectedXmlWithoutChangedTool = """
      <profile version="1.0">
        <option name="myName" value="foo" />
        <inspection_tool class="${notEnabledTool.shortName}" enabled="true" level="${notEnabledTool.level.name}" enabled_by_default="false" />
      </profile>
    """.trimIndent()
    assertThat(writeProfile(profileBasedOnEmptyProfile)).isEqualTo(expectedXmlWithoutChangedTool)
  }

  @Test
  fun `serialized profile based on base qodana profile preserves modifications`() {
    val profileBasedOnBaseProfile = QodanaInspectionProfile.newWithEnabledByDefaultTools("foo", qodanaApplicationInspectionProfileManager)

    val notEnabledTool = profileBasedOnBaseProfile.tools.first { !it.isEnabled }
    notEnabledTool.isEnabled = true

    @Language("XML")
    val expectedXmlWithoutChangedTool = """
      <profile version="1.0">
        <option name="myName" value="foo" />
        <inspection_tool class="${notEnabledTool.shortName}" enabled="true" level="${notEnabledTool.level.name}" enabled_by_default="false" />
      </profile>
    """.trimIndent()
    assertThat(writeProfile(profileBasedOnBaseProfile)).isEqualTo(expectedXmlWithoutChangedTool)
  }
}