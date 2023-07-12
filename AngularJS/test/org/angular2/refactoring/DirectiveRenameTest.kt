// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.refactoring

import com.intellij.webSymbols.testWebSymbolRename
import org.angular2.Angular2CodeInsightFixtureTestCase
import org.angularjs.AngularTestUtil

class DirectiveRenameTest : Angular2CodeInsightFixtureTestCase() {
  override fun getTestDataPath(): String {
    return AngularTestUtil.getBaseTestDataPath(javaClass) + "rename"
  }

  fun testTag2() {
    myFixture.configureByFiles("tag.html", "tag2.ts", "package.json")
    myFixture.testWebSymbolRename("tag.after.html", "foo-bar2")
    myFixture.checkResultByFile("tag2.ts", "tag2.after.ts", false)
  }

  fun testTag2Normalized() {
    myFixture.configureByFiles("tag.html", "tag2.ts", "package.json")
    myFixture.testWebSymbolRename("tag2Normalized.after.html", "fooBar2")
    myFixture.checkResultByFile("tag2.ts", "tag2Normalized.after.ts", false)
  }

  fun testAttribute2() {
    myFixture.configureByFiles("attribute2.html", "attribute2.ts", "package.json")
    myFixture.testWebSymbolRename("attribute2.after.html", "foo-bar2")
    myFixture.checkResultByFile("attribute2.ts", "attribute2.after.ts", false)
  }

  fun testAttribute2Normalized() {
    myFixture.configureByFiles("attribute2.html", "attribute2.ts", "package.json")
    myFixture.testWebSymbolRename("attribute2Normalized.after.html", "fooBar2")
    myFixture.checkResultByFile("attribute2.ts", "attribute2Normalized.after.ts", false)
  }

  fun testBinding() {
    myFixture.configureByFiles("binding.html", "object.ts", "package.json")
    myFixture.testWebSymbolRename("binding.after.html", "model2")
    myFixture.checkResultByFile("object.ts", "object.binding.ts", false)
  }

  fun testDirective() {
    myFixture.configureByFiles("directive2.ts", "directive2.html", "package.json")
    myFixture.testWebSymbolRename("directive2.after.ts", "foo-bar2")
    myFixture.checkResultByFile("directive2.html", "directive2.after.html", false)
  }

  fun testEventHandler() {
    myFixture.configureByFiles("event.html", "object.ts", "package.json")
    myFixture.testWebSymbolRename("event.after.html", "complete2")
    myFixture.checkResultByFile("object.ts", "object.event.ts", false)
  }
}
