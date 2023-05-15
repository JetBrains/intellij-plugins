package com.jetbrains.lang.makefile

import com.intellij.testFramework.fixtures.*
import com.jetbrains.lang.makefile.inspections.MakefileUnresolvedPrerequisiteInspection

class MakefileCreateRuleQuickfixTest : BasePlatformTestCase() {
  fun testSimple() = doTest()
  fun testMiddle() = doTest()

  override fun setUp() {
    super.setUp()
    myFixture.enableInspections(MakefileUnresolvedPrerequisiteInspection::class.java)
  }

  fun doTest() {
    myFixture.configureByFile("$basePath/${getTestName(true)}.mk")
    val intention = myFixture.findSingleIntention(MakefileLangBundle.message("intention.name.create.rule"))
    myFixture.launchAction(intention)
    myFixture.checkResultByFile("$basePath/${getTestName(true)}.gold.mk")
  }

  override fun getTestDataPath() = BASE_TEST_DATA_PATH
  override fun getBasePath() = "quickfix/createRule"
}