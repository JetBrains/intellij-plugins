package org.intellij.plugin.mdx

import com.intellij.testFramework.TestDataPath
import org.junit.jupiter.api.Test

@TestDataPath($$"$PROJECT_ROOT/contrib/mdx/testData/emmet")
class MdxEmmetTest : MdxTestBase() {

  @Test
  fun testTemplates() = doEmmetTest()

  @Test
  fun testTagNameInference() {
    doEmmetTest("TagNameInference", "TagNameInference_after")
    doEmmetTest("TagNameInferenceInsideExistingTag", "TagNameInferenceInsideExistingTag_after")
  }

  @Test
  fun testReactClassAttributes() {
    doEmmetTest("ReactClassAttribute", "ReactClassAttribute_after")
    doEmmetTest("ReactClassNameAttribute", "ReactClassNameAttribute_after")
  }

  @Test
  fun testDoubleBracket() = doEmmetTest()
}
