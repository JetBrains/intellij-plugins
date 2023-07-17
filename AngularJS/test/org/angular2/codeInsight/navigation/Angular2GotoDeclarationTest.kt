// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight.navigation

import com.intellij.webSymbols.checkGotoDeclaration
import org.angular2.Angular2TestCase

class Angular2GotoDeclarationTest: Angular2TestCase("navigation/declaration") {

  fun testExportAs() = doTest(92)

  fun testExportAsHostDirectives() = doTest(186)

  private fun doTest(expectedOffset: Int) {
    configure()
    myFixture.checkGotoDeclaration(expectedOffset)
  }

}