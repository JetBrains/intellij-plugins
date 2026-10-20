package org.jetbrains.astro.codeInsight

import com.intellij.lang.javascript.completion.JSLookupPriority
import com.intellij.openapi.util.registry.RegistryManager
import com.intellij.testFramework.junit5.TestApplication
import org.jetbrains.astro.AstroCodeInsightTestCase
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@TestApplication
class AstroCompletionTest : AstroCodeInsightTestCase("codeInsight/completion", useLsp = true) {
  @BeforeEach
  fun increaseServiceItemsLimit() {
    RegistryManager.getInstance().get("typescript.service.completion.serviceItemsLimit").setValue(2000, myFixture.testRootDisposable)
  }

  @Test
  fun testHtmlElements() = astro.doLookupTest()
  @Test
  fun testHtmlAttributes() = astro.doLookupTest()
  @Test
  fun testCharEntities() = astro.doLookupTest()

  @Test
  fun testScriptTagAttributes() =
    astro.doLookupTest {
      it.priority > 4
    }

  @Test
  fun testStyleTagAttributes() =
    astro.doLookupTest {
      it.priority > 4
    }

  @Test
  fun testImportedComponent() =
    astro.doLookupTest(additionalFiles = listOf("component.astro"))

  @Test
  fun testFrontmatterKeywords() =
    astro.doLookupTest(additionalFiles = listOf("component.astro")) {
      it.priority.toInt() == JSLookupPriority.KEYWORDS_PRIORITY.priorityValue
      || it.priority.toInt() == JSLookupPriority.NON_CONTEXT_KEYWORDS_PRIORITY.priorityValue
    }

  @Test
  fun testPropsInterface() =
    astro.doLookupTest()

  @Test
  fun testBaseOnType() =
    astro.doLookupTest()

  @Test
  fun testTemplateLookupRoot() =
    astro.doLookupTest()

  @Test
  fun testTemplateLookupNestedHtml() =
    astro.doLookupTest {
      it.priority > 0
    }

  @Test
  fun testAstroComponentProps() =
    astro.doLookupTest(additionalFiles = listOf("component.astro"))

  @Test
  fun testAstroComponentProps2() =
    astro.doLookupTest(additionalFiles = listOf("component.astro"))

  @Test
  fun testAstroDirectives() =
    astro.doLookupTest(additionalFiles = listOf("react-component.tsx")) {
      it.priority > 4
    }

  @Test
  fun testAstroDirectives2() =
    astro.doLookupTest(additionalFiles = listOf("react-component.tsx"))

  @Test
  fun testAliasedComponentImport() =
    astro.doConfiguredTest(dir = true, configureFileName = "src/layout/App.astro") {
      completeBasic()
      checkResultByFile("${astro.testName}/src/layout/App.after.astro")
    }

  @Test
  fun testAwait() = astro.doEditorTypingTest(
    checkResult = false,
  ) {
    type("awa")
    checkLookupItems(
      lookupItemFilter = { it.lookupString == "await" }
    )
  }

  @Test
  fun testReactComponent() = astro.doLookupTest(
    dir = true,
    configureFileName = "index.astro"
  )

  @Test
  fun testReactComponentProps() = astro.doLookupTest(
    dir = true,
    configureFileName = "index.astro"
  )

  @Test
  fun testAstroComponent() = astro.doLookupTest(
    dir = true,
    configureFileName = "index.astro"
  )

  @Test
  fun testAstroComponentFrontmatter() = astro.doLookupTest(
    dir = true,
    configureFileName = "index.astro"
  )

  @Test
  fun testAstroLibComponent() = astro.doLookupTest()

  @Test
  fun testTemplatePartCompletionReload() = astro.doEditorTypingTest(
    dir = true,
    configureFileName = "index.astro",
    checkResult = false,
  ) {
    type("<")
    checkLookupItems(
      lookupItemFilter = run {
        var accepted = 0
        { _ ->
          accepted++ < 100
        }
      }
    )
    type("MyReac")
    checkLookupItems()
  }

  @Test
  fun testCssClass() = astro.doLookupTest(
    dir = true,
    configureFileName = "index.astro",
    additionalFiles = listOf("global.css", "foo.astro")
  )

  @Test
  fun testEmmetAbbreviation() = astro.doLookupTest()

  @Test
  fun testSlotNameAttribute() = astro.doLookupTest() {
    it.priority > 4 || it.lookupString.startsWith("s")
  }

  // WEB-59265 only enabled completion at root level and nested in HTML but not as children of components.
  // This needs a fix before it can be enabled again.
  //fun testTemplateLookupNestedComponent() =
  //  doLookupTest(additionalFiles = listOf("component.astro"))
}