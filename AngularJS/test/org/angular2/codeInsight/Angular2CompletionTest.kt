// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight

import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.webSymbols.checkListByFile
import com.intellij.webSymbols.enableIdempotenceChecksOnEveryCache
import com.intellij.webSymbols.moveToOffsetBySignature
import com.intellij.webSymbols.renderLookupItems
import org.angular2.Angular2CodeInsightFixtureTestCase
import org.angular2.modules.Angular2TestModule
import org.angular2.modules.Angular2TestModule.ANGULAR_CDK_14_2_0
import org.angular2.modules.Angular2TestModule.ANGULAR_CORE_13_3_5
import org.angularjs.AngularTestUtil

class Angular2CompletionTest : Angular2CodeInsightFixtureTestCase() {

  override fun getTestDataPath(): String =
    AngularTestUtil.getBaseTestDataPath(javaClass) + "completion"

  override fun setUp() {
    super.setUp()
    // Let's ensure we don't get WebSymbols registry stack overflows randomly
    this.enableIdempotenceChecksOnEveryCache()
  }

  fun testCompletionInExpression() {
    doLookupTest(ANGULAR_CORE_13_3_5, ANGULAR_CDK_14_2_0, dir = true)
    // Export from other file
    myFixture.type("kThemes\n")
    myFixture.type(".")
    myFixture.completeBasic()
    myFixture.type("l\n;")

    // Local symbol
    myFixture.type("CdkColors")
    myFixture.completeBasic()
    myFixture.type(".")
    myFixture.completeBasic()
    myFixture.type("re\n;")

    // Global symbol
    myFixture.type("Ma")
    myFixture.completeBasic()
    myFixture.type("th\n")
    myFixture.type(".")
    myFixture.completeBasic()
    myFixture.type("abs\n")

    myFixture.checkResultByFile("completionInExpression/completionInExpression.ts.after")
  }

  private fun doLookupTest(vararg modules: Angular2TestModule,
                           fileContents: String? = null,
                           dir: Boolean = false,
                           noConfigure: Boolean = false,
                           locations: List<String> = emptyList(),
                           renderPriority: Boolean = true,
                           renderTypeText: Boolean = true,
                           renderTailText: Boolean = false,
                           containsCheck: Boolean = false,
                           renderProximity: Boolean = false,
                           renderPresentedText: Boolean = false,
                           linkModules: Boolean = true,
                           extension: String = "ts",
                           lookupFilter: (item: LookupElement) -> Boolean = { true },
                           filter: (item: String) -> Boolean = { true }) {
    if (!noConfigure) {
      if (dir) {
        myFixture.copyDirectoryToProject(getTestName(true), ".")
      }
      if (modules.isNotEmpty()) {
        Angular2TestModule.configure(myFixture, linkModules, *modules)
      }
      if (fileContents != null) {
        myFixture.configureByText(getTestName(true) + ".$extension", fileContents)
      }
      else if (dir) {
        myFixture.configureFromTempProjectFile(getTestName(true) + ".$extension")
      }
      else {
        myFixture.configureByFile(getTestName(true) + ".$extension")
      }
    }
    if (locations.isEmpty()) {
      myFixture.completeBasic()
      myFixture.checkListByFile(
        myFixture.renderLookupItems(renderPriority, renderTypeText, renderTailText, renderProximity, renderPresentedText, lookupFilter)
          .filter(filter),
        getTestName(true) + (if (dir) "/results" else "") + ".txt",
        containsCheck
      )
    }
    else {
      locations.forEachIndexed { index, location ->
        myFixture.moveToOffsetBySignature(location)
        myFixture.completeBasic()
        myFixture.checkListByFile(
          myFixture.renderLookupItems(renderPriority, renderTypeText, renderTailText, renderProximity, renderPresentedText, lookupFilter)
            .filter(filter),
          getTestName(true) + (if (dir) "/results" else "") + ".${index + 1}.txt",
          containsCheck
        )
      }
    }
  }

}