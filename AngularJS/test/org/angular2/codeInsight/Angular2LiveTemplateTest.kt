// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight

import com.intellij.codeInsight.template.impl.LiveTemplateCompletionContributor
import com.intellij.lang.javascript.BaseJSCompletionTestCase
import org.angular2.Angular2CodeInsightFixtureTestCase

class Angular2LiveTemplateTest : Angular2CodeInsightFixtureTestCase() {
  @Throws(Exception::class)
  override fun setUp() {
    super.setUp()
    LiveTemplateCompletionContributor.setShowTemplatesInTests(true, testRootDisposable)
  }

  fun testComponent() {
    myFixture.configureByText("foo.ts", "a-comp<caret>")
    val elements = myFixture.completeBasic()
    BaseJSCompletionTestCase.checkWeHaveInCompletion(elements, "a-component")
  }

  fun testRxjsOperatorImport() {
    myFixture.configureByText("foo.ts", "a-rxjs-operator-imp<caret>")
    val elements = myFixture.completeBasic()
    BaseJSCompletionTestCase.checkWeHaveInCompletion(elements, "a-rxjs-operator-import")
  }

  fun testComponentNoCompletion() {
    myFixture.configureByText("foo.ts", "var a = a-comp<caret>")
    val elements = myFixture.completeBasic()
    BaseJSCompletionTestCase.checkNoCompletion(elements, "a-component")
  }

  fun testOutputEvent() {
    myFixture.configureByText("foo.ts", "class Foo {\na-outp<caret>\n}")
    val elements = myFixture.completeBasic()
    BaseJSCompletionTestCase.checkWeHaveInCompletion(elements, "a-output-event")
  }

  fun testOutputEventLastPart() {
    myFixture.configureByText("foo.ts", "class Foo {\na-output-ev<caret>\n}")
    val elements = myFixture.completeBasic()
    BaseJSCompletionTestCase.checkWeHaveInCompletion(elements, "a-output-event")
  }

  fun testRoutePath404() {
    myFixture.configureByText("foo.ts", "var z = [a-rou<caret>]")
    val elements = myFixture.completeBasic()
    BaseJSCompletionTestCase.checkWeHaveInCompletion(elements, "a-route-path-404")
  }
}
