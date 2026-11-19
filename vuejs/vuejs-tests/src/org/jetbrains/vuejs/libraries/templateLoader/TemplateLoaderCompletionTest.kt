// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.libraries.templateLoader

import com.intellij.polySymbols.testFramework.hybridFixture
import com.intellij.testFramework.junit5.TestApplication
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.vuejs.lang.configureVueDependencies
import org.jetbrains.vuejs.lang.getVueTestDataPath
import org.junit.jupiter.api.Test

@TestApplication
class TemplateLoaderCompletionTest {
  private val hybrid by hybridFixture(getVueTestDataPath() + "/libraries/templateLoader/")
  private val myFixture get() = hybrid.fixture

  @Test
  fun testDecoratedQuery() {
    doTest(hybrid.getTestName(true))
  }

  @Test
  fun testDecoratedSimple() {
    doTest(hybrid.getTestName(true))
  }

  @Test
  fun testRegularSimple() {
    doTest(hybrid.getTestName(true))
  }

  private fun doTest(name: String) {
    myFixture.configureVueDependencies()
    myFixture.configureByFiles("$name.html", "$name.js")
    myFixture.completeBasic()
    assertThat(myFixture.lookupElementStrings!!).contains("text", "fooBar")
  }

}
