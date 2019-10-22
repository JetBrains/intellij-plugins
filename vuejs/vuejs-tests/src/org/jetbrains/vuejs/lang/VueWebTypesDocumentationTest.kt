package org.jetbrains.vuejs.lang

import com.intellij.lang.javascript.JSAbstractDocumentationTest
import com.intellij.openapi.application.PathManager

class VueWebTypesDocumentationTest : JSAbstractDocumentationTest() {

  override fun getTestDataPath(): String = PathManager.getHomePath() + "/contrib/vuejs/vuejs-tests/testData/documentation/web-types"

  override fun getBasePath(): String = "/"

  override fun getExtension(): String = "vue"

  override fun setUp() {
    super.setUp()
    createPackageJsonWithVueDependency(myFixture,""""test-lib":"0.0.0"""")
    myFixture.copyDirectoryToProject("node_modules", "node_modules")
  }

  fun testComponent() {
    defaultTest()
  }

  fun testComponentEvent() {
    defaultTest()
  }

  fun testComponentAttribute() {
    defaultTest()
  }

  fun testComponentSlot() {
    defaultTest()
  }

  fun testComponentPatternSlot() {
    defaultTest()
  }

  fun testDirective() {
    defaultTest()
  }

  fun testFilter() {
    defaultTest()
  }
}
