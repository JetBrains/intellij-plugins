package org.jetbrains.astro.service

import com.intellij.openapi.util.registry.RegistryManager
import org.jetbrains.astro.AstroLspTestCase
import org.jetbrains.astro.AstroTestModule


class AstroServiceCompletionTest : AstroLspTestCase("/service/completion/") {
  override fun setUp() {
    super.setUp()
    RegistryManager.getInstance().get("typescript.service.completion.serviceItemsLimit").setValue(2000, myFixture.testRootDisposable)
  }

  fun testAwait() = doLookupTest(
    AstroTestModule.ASTRO_5_14_4,
    lookupItemFilter = run {
      var accepted = 0
      { _ ->
        accepted++ < 100
      }
    }
  )

  fun testReactComponent() = doLookupTest(
    AstroTestModule.ASTRO_5_14_4,
    dir = true,
    configureFileName = "index.astro"
  )

  fun testAstroComponent() = doLookupTest(
    AstroTestModule.ASTRO_5_14_4,
    dir = true,
    configureFileName = "index.astro"
  )

  fun testAstroComponentFrontmatter() = doLookupTest(
    AstroTestModule.ASTRO_5_14_4,
    dir = true,
    configureFileName = "index.astro"
  )

  fun testAstroLibComponent() = doLookupTest(
    AstroTestModule.ASTRO_5_14_4,
  )

  fun testTemplatePartCompletionReload() = doCompletionAutoPopupTest(
    AstroTestModule.ASTRO_5_14_4,
    dir = true,
    configureFileName = "index.astro",
    checkResult = false,
  ) {
    type("<")
    checkLookupItems()
    type("MyReac")
    checkLookupItems()
  }
}