// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.cli

import com.intellij.codeInsight.daemon.impl.analysis.HtmlUnknownTargetInspection
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.psi.css.inspections.invalid.CssUnknownTargetInspection
import com.intellij.testFramework.UsefulTestCase
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.webSymbols.testFramework.moveToOffsetBySignature
import org.angular2.Angular2TestUtil

class Angular2CliContextTest : BasePlatformTestCase() {
  override fun getTestDataPath(): String {
    return Angular2TestUtil.getBaseTestDataPath() + "cli/context"
  }

  fun testAngular9() {
    myFixture.enableInspections(HtmlUnknownTargetInspection::class.java)
    myFixture.copyDirectoryToProject("angular-9", ".")
    myFixture.configureFromTempProjectFile("src/app/app.component.html")
    myFixture.checkHighlighting(true, false, true)
    WriteAction.runAndWait<RuntimeException> {
      myFixture.getEditor().getDocument().setText("<img src=''/>")
      FileDocumentManager.getInstance().saveAllDocuments()
    }
    myFixture.moveToOffsetBySignature("<img src='<caret>'/>")
    myFixture.completeBasic()
    UsefulTestCase.assertContainsElements(myFixture.getLookupElementStrings()!!, "favicon.ico", "image.png")
  }

  fun testAngular9Css() {
    myFixture.enableInspections(CssUnknownTargetInspection::class.java)
    myFixture.copyDirectoryToProject("angular-9", ".")
    myFixture.configureFromTempProjectFile("src/app/app.component.css")
    myFixture.checkHighlighting(true, false, true)
    WriteAction.runAndWait<RuntimeException> {
      myFixture.getEditor().getDocument().setText("div {\nbackground-image=url('')\n}")
      FileDocumentManager.getInstance().saveAllDocuments()
    }
    myFixture.moveToOffsetBySignature("url('<caret>')")
    myFixture.completeBasic()
    UsefulTestCase.assertContainsElements(myFixture.getLookupElementStrings()!!, "favicon.ico", "image.png")
  }
}
