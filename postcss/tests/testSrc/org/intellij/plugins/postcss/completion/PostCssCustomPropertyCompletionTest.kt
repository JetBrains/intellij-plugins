package org.intellij.plugins.postcss.completion

import com.intellij.platform.testFramework.junit5.codeInsight.fixture.sharedCodeInsightProjectFixture
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.junit5.TestApplication
import org.intellij.plugins.postcss.postCssFixture
import org.junit.jupiter.api.Test

@TestApplication
@TestDataPath($$"$PROJECT_ROOT/contrib/postcss/tests/testData/completion/customProperty")
class PostCssCustomPropertyCompletionTest {
  companion object {
    private val projectFixture = sharedCodeInsightProjectFixture()
  }

  private val postCss by postCssFixture(projectFixture)

  @Test
  fun testCustomPropertyCompletionItemsSet() {
    postCss.doTestCompletionVariants("initial-value", "inherits", "syntax")
  }
}
