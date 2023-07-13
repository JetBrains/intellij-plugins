// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight.inspections

import com.intellij.codeInsight.intention.IntentionActionDelegate
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.webSymbols.moveToOffsetBySignature
import org.angular2.Angular2MultiFileFixtureTestCase
import org.angular2.inspections.AngularInaccessibleComponentMemberInAotModeInspection
import org.angular2.inspections.quickfixes.AngularMakePublicQuickFix
import org.angularjs.AngularTestUtil

class Angular2InaccessibleMemberAotQuickFixesTest : Angular2MultiFileFixtureTestCase() {
  override fun getTestDataPath(): String {
    return AngularTestUtil.getBaseTestDataPath() + "inspections/aot"
  }

  override fun getTestRoot(): String {
    return "/"
  }

  fun testPrivateFieldFix() {
    doMultiFileTest("private.html", "private<caret>Used")
  }

  fun testPrivateFieldInlineFix() {
    doMultiFileTest("private-inline.ts", "private<caret>Used")
  }

  fun testPrivateGetterFix() {
    doMultiFileTest("private.html", "private<caret>UsedGet")
  }

  fun testPrivateConstructorFieldFix() {
    doMultiFileTest("private.ts", "private<caret>Field")
  }

  fun testPrivateConstructorDecoratedFieldFix() {
    doMultiFileTest("private.ts", "private<caret>Field")
  }

  fun testPrivateConstructorDecoratedFieldFix2() {
    doMultiFileTest("private.html", "private<caret>Field")
  }

  private fun doMultiFileTest(fileName: String, signature: String) {
    doTest { _: VirtualFile?, _: VirtualFile? ->
      myFixture.enableInspections(AngularInaccessibleComponentMemberInAotModeInspection::class.java)
      myFixture.configureFromTempProjectFile(fileName)
      myFixture.setCaresAboutInjection(false)
      myFixture.moveToOffsetBySignature(signature)
      val intentionAction = myFixture.filterAvailableIntentions("Make 'public'")
        .find { IntentionActionDelegate.unwrap(it) is AngularMakePublicQuickFix }!!
      myFixture.launchAction(intentionAction)
    }
  }
}
