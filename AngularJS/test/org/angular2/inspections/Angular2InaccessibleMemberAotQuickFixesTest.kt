// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.inspections

import com.intellij.codeInsight.intention.IntentionActionDelegate
import com.intellij.openapi.vfs.VirtualFile
import org.angular2.Angular2MultiFileFixtureTestCase
import org.angular2.inspections.AngularInaccessibleComponentMemberInAotModeInspection
import org.angular2.inspections.quickfixes.AngularMakePublicQuickFix
import org.angularjs.AngularTestUtil

class Angular2InaccessibleMemberAotQuickFixesTest : Angular2MultiFileFixtureTestCase() {
  override fun getTestDataPath(): String {
    return AngularTestUtil.getBaseTestDataPath(javaClass) + "aot"
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
      AngularTestUtil.moveToOffsetBySignature(signature, myFixture)
      val intentionAction = myFixture.filterAvailableIntentions("Make 'public'")
        .find { IntentionActionDelegate.unwrap(it) is AngularMakePublicQuickFix }!!
      myFixture.launchAction(intentionAction)
    }
  }
}
