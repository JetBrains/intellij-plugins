// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight.inspections

import com.intellij.webSymbols.testFramework.moveToOffsetBySignature
import org.angular2.Angular2TestCase
import org.angular2.Angular2TestModule
import org.angular2.Angular2TsConfigFile
import org.angular2.inspections.AngularInaccessibleSymbolInspection

class Angular2InaccessibleMemberAotQuickFixesTest : Angular2TestCase("inspections/inaccessibleSymbol", true) {

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
    doMultiFileTest("private-input.ts", "[private<caret>Field]", "public",
                    strict = true)
  }

  fun testProtectedInputFix() {
    doMultiFileTest("protected-input.ts", "[protected<caret>Field]", "public",
                    strict = true)
  }

  fun testReadonlyInputFix() {
    doMultiFileTest("readonly-input.ts", "[readonly<caret>Field]", hint = "Remove readonly modifier",
                    strict = true)
  }

  private fun doMultiFileTest(
    fileName: String, signature: String, accessType: String = "protected", hint: String = "Make '$accessType'",
    strict: Boolean = false,
  ) {
    myFixture.enableInspections(AngularInaccessibleSymbolInspection::class.java)
    doConfiguredTest(Angular2TestModule.ANGULAR_CORE_13_3_5, dir = true, configureFileName = fileName,
                     checkResult = true, configurators = listOfNotNull(Angular2TsConfigFile(strict))) {
      setCaresAboutInjection(false)
      moveToOffsetBySignature(signature)
      val intentionAction = filterAvailableIntentions(hint)
                              .firstOrNull()
                            ?: throw IllegalStateException("\"$hint\" not in " + myFixture.availableIntentions.map { it.text })
      launchAction(intentionAction)
    }
  }
}
