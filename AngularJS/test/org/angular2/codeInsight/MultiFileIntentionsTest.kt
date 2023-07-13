// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight

import com.intellij.lang.javascript.JavaScriptBundle
import com.intellij.lang.typescript.inspections.TypeScriptUnresolvedReferenceInspection
import com.intellij.webSymbols.moveToOffsetBySignature
import org.angular2.Angular2MultiFileFixtureTestCase
import org.angularjs.AngularTestUtil

class MultiFileIntentionsTest : Angular2MultiFileFixtureTestCase() {
  override fun getTestDataPath(): String {
    return AngularTestUtil.getBaseTestDataPath() + "codeInsight/intentions"
  }

  override fun getTestRoot(): String {
    return "/"
  }

  fun testBasicFieldCreation() {
    doMultiFileTest(JavaScriptBundle.message("javascript.create.field.intention.name", "foo"))
  }

  fun testThisQualifiedFieldCreation() {
    doMultiFileTest(JavaScriptBundle.message("javascript.create.field.intention.name", "foo"))
  }

  fun testQualifiedFieldCreation() {
    doMultiFileTest(JavaScriptBundle.message("javascript.create.field.intention.name", "foo"))
  }

  fun testBasicMethodCreation() {
    doMultiFileTest(JavaScriptBundle.message("javascript.create.method.intention.name", "foo"))
  }

  fun testThisQualifiedMethodCreation() {
    doMultiFileTest(JavaScriptBundle.message("javascript.create.method.intention.name", "foo"))
  }

  fun testQualifiedMethodCreation() {
    doMultiFileTest(JavaScriptBundle.message("javascript.create.method.intention.name", "foo"))
  }

  private fun doMultiFileTest(intentionHint: String) {
    doTest { _, _ ->
      myFixture.enableInspections(
        TypeScriptUnresolvedReferenceInspection::class.java)
      myFixture.configureFromTempProjectFile("template.html")
      myFixture.setCaresAboutInjection(false)
      myFixture.moveToOffsetBySignature("f<caret>oo")
      myFixture.launchAction(myFixture.findSingleIntention(intentionHint))
    }
  }
}
