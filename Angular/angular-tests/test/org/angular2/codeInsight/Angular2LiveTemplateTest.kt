// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight

import com.intellij.codeInsight.template.Template
import com.intellij.codeInsight.template.TemplateManager
import com.intellij.codeInsight.template.impl.InvokeTemplateAction
import com.intellij.codeInsight.template.impl.LiveTemplateCompletionContributor
import com.intellij.codeInsight.template.impl.TemplateImpl
import com.intellij.lang.ecmascript6.psi.impl.JSImportsCoroutineScope
import com.intellij.lang.javascript.BaseJSCompletionTestCase
import com.intellij.testFramework.utils.coroutines.waitCoroutinesBlocking
import org.angular2.Angular2TestCase
import org.angular2.codeInsight.Angular2LiveTemplateTest.TestMode.NO_COMPLETION
import org.angular2.codeInsight.Angular2LiveTemplateTest.TestMode.WITH_COMPLETION

class Angular2LiveTemplateTest : Angular2TestCase("liveTemplate", false) {
  @Throws(Exception::class)
  override fun setUp() {
    super.setUp()
    LiveTemplateCompletionContributor.setShowTemplatesInTests(true, testRootDisposable)
  }

  fun testTemplateComponent() =
    doTest("import { Component } from '@angular/core';\n\n@Component({ template: `<div a-cla<caret>></div>` })\nexport class AppComponent {}", "a-class")

  fun testTemplateHtml() =
    doTest("<div a-cla<caret>></div>", "a-class", extension = "html")

  fun testTemplateNoCompletion() =
    doTest("a-cla<caret>", "a-class", testMode = NO_COMPLETION)

  fun testExpression() =
    doTest("let routes = [a-rou<caret>];", "a-route-path-404")

  fun testExpressionNoCompletion() =
    doTest("class AppComponent {\na-rou<caret>\n}", "a-route-path-404", testMode = NO_COMPLETION)

  fun testTopLevelStatement() =
    doTest("a-guard<caret>", "a-guard-can-activate")

  fun testTopLevelStatementNoCompletion() =
    doTest("class AppComponent {\na-guard<caret>\n}", "a-guard-can-activate", testMode = NO_COMPLETION)

  fun testStatement() =
    doTest("class AppComponent {\nfetchData() {\na-http<caret>\n}\n}", "a-httpclient-get")

  fun testStatementNoCompletion() =
    doTest("class AppComponent {\na-http<caret>\n}", "a-httpclient-get", testMode = NO_COMPLETION)

  fun testClass() =
    doTest("class AppComponent {\na-route<caret>\n}", "a-router-events")

  fun testClassNoCompletion() =
    doTest("a-route<caret>", "a-router-events", testMode = NO_COMPLETION)

  fun testUnambiguousAutoImportEnabled() {
    val template = TemplateManager.getInstance(project).createTemplate("", "", "injectable: Injectable;\$END\$")
    template.setValue(Template.Property.USE_STATIC_IMPORT_IF_POSSIBLE, true)
    doConfiguredTest(checkResult = true, additionalFiles = listOf("injectable.ts")) {
      InvokeTemplateAction(template as TemplateImpl?, myFixture.editor, project, HashSet()).perform()
      waitCoroutinesBlocking(JSImportsCoroutineScope.get(project))
    }
  }

  fun testUnambiguousAutoImportDisabled() {
    val template = TemplateManager.getInstance(project).createTemplate("", "", "injectable: Injectable;\$END\$")
    doConfiguredTest(checkResult = true, additionalFiles = listOf("injectable.ts")) {
      InvokeTemplateAction(template as TemplateImpl?, myFixture.editor, project, HashSet()).perform()
      waitCoroutinesBlocking(JSImportsCoroutineScope.get(project))
    }
  }

  private fun doTest(fileContents: String, variant: String, testMode: TestMode = WITH_COMPLETION, extension: String = defaultExtension) =
    doConfiguredTest(fileContents = fileContents, extension = extension) {
      when (testMode) {
        WITH_COMPLETION -> BaseJSCompletionTestCase.checkWeHaveInCompletion(myFixture.completeBasic(), variant)
        else -> BaseJSCompletionTestCase.checkNoCompletion(myFixture.completeBasic(), variant)
      }
    }

  private enum class TestMode {
    WITH_COMPLETION,
    NO_COMPLETION
  }
}
