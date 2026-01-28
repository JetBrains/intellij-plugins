package org.jetbrains.astro.codeInsight

import com.intellij.lang.javascript.completion.JSLookupPriority
import com.intellij.openapi.util.registry.RegistryManager
import org.jetbrains.astro.AstroCodeInsightTestCase

class AstroCompletionTest : AstroCodeInsightTestCase("codeInsight/completion", useLsp = true) {
  override fun setUp() {
    super.setUp()
    RegistryManager.getInstance().get("typescript.service.completion.serviceItemsLimit").setValue(2000, myFixture.testRootDisposable)
  }

  fun testHtmlElements() = doLookupTest()
  fun testHtmlAttributes() = doLookupTest()
  fun testCharEntities() = doLookupTest()

  fun testScriptTagAttributes() =
    doLookupTest {
      it.priority > 0
    }

  fun testStyleTagAttributes() =
    doLookupTest {
      it.priority > 0
    }

  fun testImportedComponent() =
    doLookupTest(additionalFiles = listOf("component.astro"))

  fun testFrontmatterKeywords() =
    doLookupTest(additionalFiles = listOf("component.astro")) {
      it.priority.toInt() == JSLookupPriority.KEYWORDS_PRIORITY.priorityValue
      || it.priority.toInt() == JSLookupPriority.NON_CONTEXT_KEYWORDS_PRIORITY.priorityValue
    }

  fun testPropsInterface() =
    doLookupTest()

  fun testBaseOnType() =
    doLookupTest()

  fun testTemplateLookupRoot() =
    doLookupTest()

  fun testTemplateLookupNestedHtml() =
    doLookupTest {
      it.priority > 0
    }

  fun testAstroComponentProps() =
    doLookupTest(additionalFiles = listOf("component.astro"))

  fun testAstroComponentProps2() =
    doLookupTest(additionalFiles = listOf("component.astro"))

  fun testAstroDirectives() =
    doLookupTest(additionalFiles = listOf("react-component.tsx")) {
      it.priority > 0
    }

  fun testAstroDirectives2() =
    doLookupTest(additionalFiles = listOf("react-component.tsx"))

  fun testAliasedComponentImport() =
    doConfiguredTest(dir = true, configureFileName = "src/layout/App.astro") {
      completeBasic()
      checkResultByFile("$testName/src/layout/App.after.astro")
    }

  fun testAwait() = doLookupTest(
    lookupItemFilter = run {
      var accepted = 0
      { _ ->
        accepted++ < 100
      }
    }
  )

  fun testReactComponent() = doLookupTest(
    dir = true,
    configureFileName = "index.astro"
  )

  fun testReactDuplicateComponent() = doLookupTest(
    dir = true,
    configureFileName = "index.astro"
  )

  fun testAstroComponent() = doLookupTest(
    dir = true,
    configureFileName = "index.astro"
  )

  fun testAstroComponentFrontmatter() = doLookupTest(
    dir = true,
    configureFileName = "index.astro"
  )

  fun testAstroLibComponent() = doLookupTest()

  fun testTemplatePartCompletionReload() = doCompletionAutoPopupTest(
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

  fun testCssClass() = doLookupTest(
    dir = true,
    configureFileName = "index.astro",
    additionalFiles = listOf("global.css", "foo.astro")
  )

  fun testEmmetAbbreviation() = doLookupTest()

  fun testSlotNameAttribute() = doLookupTest()

  // WEB-59265 only enabled completion at root level and nested in HTML but not as children of components.
  // This needs a fix before it can be enabled again.
  //fun testTemplateLookupNestedComponent() =
  //  doLookupTest(additionalFiles = listOf("component.astro"))
}