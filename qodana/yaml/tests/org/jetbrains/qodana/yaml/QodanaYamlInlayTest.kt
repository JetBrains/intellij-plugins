package org.jetbrains.qodana.yaml

import com.intellij.openapi.Disposable
import com.intellij.platform.testFramework.junit5.codeInsight.fixture.codeInsightFixture
import com.intellij.platform.testFramework.junit5.codeInsight.fixture.codeInsightProjectFixture
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.testFramework.junit5.TestDisposable
import com.intellij.testFramework.utils.inlays.declarative.DeclarativeInlayHintsProviderTestUtil.doTestProvider
import org.junit.jupiter.api.Test

@TestApplication
class QodanaYamlInlayTest {
  private val projectFixture = codeInsightProjectFixture()
  private val myFixture by codeInsightFixture(projectFixture)

  @Suppress("UNCHECKED_CAST")
  private val provider = QodanaYamlInspectionHintProvider()

  @Test
  fun `test inlay`(@TestDisposable disposable: Disposable) {
    setupMockProfiles(myFixture.project, disposable)
    doTestProvider(myFixture, "qodana.yaml", """
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