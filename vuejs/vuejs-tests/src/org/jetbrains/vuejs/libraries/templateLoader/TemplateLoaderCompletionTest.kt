// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.libraries.templateLoader

import com.intellij.openapi.application.PathManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.jetbrains.vuejs.lang.createPackageJsonWithVueDependency

class TemplateLoaderCompletionTest : BasePlatformTestCase() {

  override fun getTestDataPath(): String = PathManager.getHomePath() + "/contrib/vuejs/vuejs-tests/testData/libraries/templateLoader/"

  fun testDecoratedQuery() {
    doTest(getTestName(true))
  }

  fun testDecoratedSimple() {
    doTest(getTestName(true))
  }

  fun testRegularSimple() {
    doTest(getTestName(true))
  }

  private fun doTest(name: String) {
    createPackageJsonWithVueDependency(myFixture)
    myFixture.configureByFiles("$name.html", "$name.js")
    myFixture.completeBasic()
    assertContainsElements(myFixture.lookupElementStrings!!, "text", "fooBar")
  }

}
