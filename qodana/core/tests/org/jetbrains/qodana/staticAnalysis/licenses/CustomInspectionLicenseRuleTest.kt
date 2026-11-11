package org.jetbrains.qodana.staticAnalysis.licenses

import com.intellij.codeInspection.ex.InspectionToolWrapper
import com.intellij.openapi.util.JDOMUtil
import com.intellij.testFramework.ExtensionTestUtil
import org.jetbrains.qodana.license.CustomInspectionLicenseRule
import org.jetbrains.qodana.license.QodanaLicenseType
import org.jetbrains.qodana.staticAnalysis.QodanaTestCase
import org.jetbrains.qodana.staticAnalysis.profile.MainInspectionGroup
import org.jetbrains.qodana.staticAnalysis.profile.QodanaInspectionProfile
import org.jetbrains.qodana.staticAnalysis.profile.QodanaInspectionProfileManager
import org.jetbrains.qodana.staticAnalysis.profile.QodanaProfile
import org.junit.Test

private const val INSPECTION = "Annotator"

class CustomInspectionLicenseRuleTest : QodanaTestCase() {
  @Test
  fun `inspection without rule is available with community`() {
    useRule(ultimate = false, ultimatePlus = false)

    assertTrue(isEnabled(QodanaLicenseType.COMMUNITY))
  }

  @Test
  fun `inspection that requires ultimate`() {
    useRule(ultimate = true, ultimatePlus = false)

    assertFalse(isEnabled(QodanaLicenseType.NONE))
    assertFalse(isEnabled(QodanaLicenseType.COMMUNITY))
    assertTrue(isEnabled(QodanaLicenseType.ULTIMATE))
    assertTrue(isEnabled(QodanaLicenseType.ULTIMATE_PLUS))
    assertTrue(isEnabled(QodanaLicenseType.PREMIUM))
  }

  @Test
  fun `inspection that requires ultimate plus`() {
    useRule(ultimate = false, ultimatePlus = true)

    assertFalse(isEnabled(QodanaLicenseType.COMMUNITY))
    assertFalse(isEnabled(QodanaLicenseType.ULTIMATE))
    assertTrue(isEnabled(QodanaLicenseType.ULTIMATE_PLUS))
    assertTrue(isEnabled(QodanaLicenseType.PREMIUM))
  }

  private fun useRule(ultimate: Boolean, ultimatePlus: Boolean) {
    val rule = object : CustomInspectionLicenseRule {
      override fun requiresUltimatePlus(wrapper: InspectionToolWrapper<*, *>): Boolean = ultimatePlus && wrapper.shortName == INSPECTION

      override fun requiresUltimate(wrapper: InspectionToolWrapper<*, *>): Boolean = ultimate && wrapper.shortName == INSPECTION
    }
    ExtensionTestUtil.maskExtensions(CustomInspectionLicenseRule.EP_NAME, listOf(rule), testRootDisposable)
  }

  private fun isEnabled(licenseType: QodanaLicenseType): Boolean {
    val xml = """
      <profile version="1.0">
        <option name="myName" value="foo" />
        <inspection_tool class="$INSPECTION" enabled="true" level="WARNING" enabled_by_default="true"/>
      </profile>
    """.trimIndent()
    val inspectionProfile = QodanaInspectionProfile.newFromXml(
      JDOMUtil.load(xml), name = null, profileManager = QodanaInspectionProfileManager.getInstance(project)
    )
    inspectionProfile.lockProfile(true)
    val profile = QodanaProfile(MainInspectionGroup(inspectionProfile), emptyList(), project, licenseType)
    return profile.effectiveProfile.getTools(INSPECTION, project).isEnabled
  }
}
