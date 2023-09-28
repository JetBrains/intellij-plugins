// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight.inspections

import com.intellij.codeInsight.intention.IntentionActionDelegate
import com.intellij.javascript.web.configure
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.webSymbols.moveToOffsetBySignature
import org.angular2.Angular2MultiFileFixtureTestCase
import org.angular2.Angular2TsConfigFile
import org.angular2.inspections.AngularInaccessibleSymbolInspection
import org.angular2.inspections.quickfixes.AngularChangeModifierQuickFix
import org.angularjs.AngularTestUtil

class Angular2InaccessibleMemberAotQuickFixesTest : Angular2MultiFileFixtureTestCase() {
  override fun getTestDataPath(): String {
    return AngularTestUtil.getBaseTestDataPath() + "inspections/inaccessibleSymbol"
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
    doMultiFileTest("private.html", "private<caret>Field")
  }

  fun testPrivateConstructorDecoratedFieldFix() {
    doMultiFileTest("private.html", "private<caret>Field")
  }

  fun testPrivateConstructorDecoratedFieldFix2() {
    doMultiFileTest("private.html", "private<caret>Field")
  }

  fun testPrivateInputFix() {
    myFixture.configure(Angular2TsConfigFile.withStrictTemplates)
    doMultiFileTest("private-input.ts", "[private<caret>Field]", "public")
  }

  fun testProtectedInputFix() {
    myFixture.configure(Angular2TsConfigFile.withStrictTemplates)
    doMultiFileTest("protected-input.ts", "[protected<caret>Field]", "public")
  }

  private fun doMultiFileTest(fileName: String, signature: String, accessType: String = "protected") {
    doTest { _: VirtualFile?, _: VirtualFile? ->
      myFixture.enableInspections(AngularInaccessibleSymbolInspection::class.java)
      myFixture.configureFromTempProjectFile(fileName)
      myFixture.setCaresAboutInjection(false)
      myFixture.moveToOffsetBySignature(signature)
      val intentionAction = myFixture.filterAvailableIntentions("Make '$accessType'")
                              .find { IntentionActionDelegate.unwrap(it) is AngularChangeModifierQuickFix }
                            ?: throw IllegalStateException("\"Make '$accessType'\" not in " + myFixture.availableIntentions.map { it.text })
      myFixture.launchAction(intentionAction)
      WriteAction.run<Throwable> {
        myFixture.tempDirFixture.getFile("tsconfig.json")
          ?.delete(Any())
      }
    }
  }
}
