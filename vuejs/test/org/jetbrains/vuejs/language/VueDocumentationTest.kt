package org.jetbrains.vuejs.language

import com.intellij.codeInsight.documentation.DocumentationManager
import com.intellij.openapi.application.PathManager
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase
import junit.framework.TestCase

/**
 * @author Irina.Chernushina on 11/15/2017.
 */
class VueDocumentationTest : LightPlatformCodeInsightFixtureTestCase() {
  override fun getTestDataPath(): String = PathManager.getHomePath() + "/contrib/vuejs/testData/"

  fun testDocumentationFromDefinitions() {
    createPackageJsonWithVueDependency(myFixture)
    myFixture.copyDirectoryToProject("./types/node_modules", "./node_modules")
    myFixture.configureByText("testDocumentationFromDefinitions.vue", """
<script>
  export default {
    <caret>mixins: []
  }
</script>
""")
    val elementAtCaret = myFixture.elementAtCaret
    val documentationProvider = DocumentationManager.getProviderFromElement(elementAtCaret)
    val inlineDoc = documentationProvider.generateDoc(elementAtCaret, elementAtCaret)

    TestCase.assertNotNull(inlineDoc)
    TestCase.assertTrue(inlineDoc!!.trim().startsWith("Fictive mixins comment"))
  }
}