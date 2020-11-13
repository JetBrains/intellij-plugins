package org.jetbrains.vuejs.lang

import com.intellij.codeInsight.documentation.DocumentationManager
import com.intellij.lang.documentation.ExternalDocumentationProvider
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import junit.framework.TestCase

class VueDocumentationTest : BasePlatformTestCase() {
  override fun getTestDataPath(): String = getVueTestDataPath() + "/"

  fun testDocumentationFromDefinitions() {
    myFixture.configureVueDependencies(VueTestModule.VUE_2_5_3)
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
    TestCase.assertTrue(inlineDoc!!.trim().contains("Fictive mixins comment"))
  }

  fun testTSLibraryElement() {
    createPackageJsonWithVueDependency(myFixture, "")
    myFixture.configureByText("testDocumentationFromDefinitions.vue", """
<script>
  const foo: Promise
  foo.th<caret>en()
</script>
""")
    val element = myFixture.file.findElementAt(myFixture.caretOffset)
    val elementAtCaret = myFixture.elementAtCaret
    val documentationProvider = DocumentationManager.getProviderFromElement(elementAtCaret, element)
    documentationProvider as ExternalDocumentationProvider
    val urls = documentationProvider.getUrlFor(elementAtCaret, element)
    TestCase.assertNotNull(urls)
    TestCase.assertNull("$urls", documentationProvider.fetchExternalDocumentation(project, elementAtCaret, urls, false))
  }

}
