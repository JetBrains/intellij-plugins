// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight

import org.angular2.Angular2CodeInsightFixtureTestCase
import org.angularjs.AngularTestUtil

class Angular2ScopesTest : Angular2CodeInsightFixtureTestCase() {
  override fun getTestDataPath(): String {
    return AngularTestUtil.getBaseTestDataPath() + "codeInsight/scopes"
  }

  fun testReferencesAndVariablesScopes() {
    myFixture.configureByFiles("refsAndVars.html", "refsAndVars.ts", "package.json")
    val fileText = myFixture.getFile().getText()
    val TEST_PREFIX = "resolveRef-"
    var lastCase = 0
    var i = 0
    var offset: Int
    while (fileText.indexOf(TEST_PREFIX, lastCase).also { offset = it } > 0) {
      i++
      lastCase = offset + 8
      val result = fileText[offset + TEST_PREFIX.length]
      val testCase = "test case " + i + " (`" + fileText.substring(offset, fileText.indexOf("}", offset) + 2) + "`)"
      assert(result == 'T' || result == 'F') { "Bad result spec for $testCase: $result" }
      val ref = myFixture.getFile().findReferenceAt(offset + TEST_PREFIX.length + 6)
      assertNotNull("Ref is empty for $testCase", ref)
      val resolve = ref!!.resolve()
      assertEquals("Bad result for $testCase", result == 'T', resolve != null)
    }
  }
}
