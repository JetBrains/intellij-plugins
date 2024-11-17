package org.jetbrains.qodana.yaml

import com.intellij.testFramework.utils.inlays.declarative.DeclarativeInlayHintsProviderTestCase
import org.junit.Test

class QodanaYamlInlayTest : DeclarativeInlayHintsProviderTestCase() {
  @Suppress("UNCHECKED_CAST")
  private val provider = QodanaYamlInspectionHintProvider()

  @Test
  fun `test inlay`() {
    setupMockProfiles(project, testRootDisposable)
    doTestProvider("qodana.yaml", """
      profile:
        name: test.qodana.starter
      include:
        - name: StartFromMeOne<#  Kotlin/Start #> 
        - name: StartFromMeTwo<#  Java/Start #> 
        - name: StartFromMeThree<#  Maldives/Start #> 
        - name: Recommended15<#  Groups/Some/Group15 #>
      exclude:
        - name: Sanity1<#  Group/Sanity #>
    """.trimIndent(), provider)
  }
}