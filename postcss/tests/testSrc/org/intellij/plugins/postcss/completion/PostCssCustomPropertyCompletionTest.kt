package org.intellij.plugins.postcss.completion

class PostCssCustomPropertyCompletionTest : PostCssCompletionTestBase() {
  override fun getTestDataSubdir() = "customProperty"

  fun testCustomPropertyCompletionItemsSet() {
    doTestCompletionVariants("initial-value", "inherits", "syntax")
  }
}