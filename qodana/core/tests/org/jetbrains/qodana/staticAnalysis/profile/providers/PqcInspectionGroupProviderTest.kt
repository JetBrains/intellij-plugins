package org.jetbrains.qodana.staticAnalysis.profile.providers

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ex.InspectionToolWrapper
import com.intellij.codeInspection.ex.LocalInspectionToolWrapper
import com.intellij.testFramework.ApplicationRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.ClassRule
import org.junit.Test

class PqcInspectionGroupProviderTest {
  companion object {
    @JvmField
    @ClassRule
    val appRule = ApplicationRule()
  }

  private val provider = PqcInspectionGroupProvider()

  private fun tool(shortName: String): InspectionToolWrapper<*, *> =
    LocalInspectionToolWrapper(object : LocalInspectionTool() {
      override fun getShortName(): String = shortName
      override fun getDisplayName(): String = shortName
      override fun getGroupDisplayName(): String = "PQC test"
    })

  @Test
  fun `all pqc groups from the bundled yaml resolve`() {
    val ids = listOf(
      "PqcLevelIndependentInspections", "PqcNotCompliantRules",
      "PqcLevel1Rules", "PqcLevel2Rules", "PqcLevel3Rules", "PqcLevel5Rules",
      "PqcMinLevel1", "PqcMinLevel2", "PqcMinLevel3", "PqcMinLevel4", "PqcMinLevel5",
      "AllPqcInspections",
    )
    for (id in ids) {
      assertThat(provider.findGroup(id)).describedAs(id).isNotNull
    }
    assertThat(provider.findGroup("NoSuchGroup")).isNull()
  }

  @Test
  fun `PqcMinLevel5 flags everything below NIST level 5 but not the level-5-compliant algorithms`() {
    val minLevel5 = provider.findGroup("PqcMinLevel5")!!

    // below level 5 -> flagged (transitively: LevelIndependent + NotCompliant + Level1 + Level2 + Level3)
    assertThat(minLevel5.includesInspection(tool("CryptoProviderSpecification"))).isTrue                  // level-independent
    assertThat(minLevel5.includesInspection(tool("JavaKotlinUsingCryptographyCipherRC4"))).isTrue         // not-compliant
    assertThat(minLevel5.includesInspection(tool("JavaKotlinUsingCryptographyCipherAES128"))).isTrue      // Level1
    assertThat(minLevel5.includesInspection(tool("JavaKotlinUsingCryptographyMessageDigestSHAKE128"))).isTrue // Level2
    assertThat(minLevel5.includesInspection(tool("JavaKotlinUsingCryptographySignatureMLDSA65"))).isTrue  // Level3

    // level-5-compliant -> NOT flagged by PqcMinLevel5
    assertThat(minLevel5.includesInspection(tool("JavaKotlinUsingCryptographyCipherAES256"))).isFalse
    assertThat(minLevel5.includesInspection(tool("JavaKotlinUsingCryptographyKEMMLKEM1024"))).isFalse
    assertThat(minLevel5.includesInspection(tool("JavaKotlinUsingCryptographySignatureMLDSA87"))).isFalse

    // unrelated inspection
    assertThat(minLevel5.includesInspection(tool("SomeUnrelatedInspection"))).isFalse
  }

  @Test
  fun `AllPqcInspections additionally flags the level-5-compliant algorithms`() {
    val all = provider.findGroup("AllPqcInspections")!!
    assertThat(all.includesInspection(tool("JavaKotlinUsingCryptographyCipherAES256"))).isTrue
    assertThat(all.includesInspection(tool("JavaKotlinUsingCryptographyKEMMLKEM1024"))).isTrue
    assertThat(all.includesInspection(tool("JavaKotlinUsingCryptographyCipherRC4"))).isTrue // still includes below-level ones
  }

  @Test
  fun `PqcMinLevel1 is only level-independent plus not-compliant`() {
    val minLevel1 = provider.findGroup("PqcMinLevel1")!!
    assertThat(minLevel1.includesInspection(tool("JavaKotlinUsingCryptographyCipherRC4"))).isTrue
    // Level1 rules are NOT part of MinLevel1 (which means "below level 1")
    assertThat(minLevel1.includesInspection(tool("JavaKotlinUsingCryptographyCipherAES128"))).isFalse
  }
}
