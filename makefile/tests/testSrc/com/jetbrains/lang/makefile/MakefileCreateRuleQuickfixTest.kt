package com.jetbrains.lang.makefile

import com.intellij.openapi.application.EDT
import com.intellij.platform.testFramework.junit5.codeInsight.fixture.codeInsightFixture
import com.intellij.platform.testFramework.junit5.codeInsight.fixture.codeInsightProjectFixture
import com.intellij.testFramework.PlatformTestUtil
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.common.timeoutRunBlocking
import com.intellij.testFramework.junit5.TestApplication
import com.jetbrains.lang.makefile.inspections.MakefileUnresolvedPrerequisiteInspection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInfo

@TestApplication
@TestDataPath($$"$PROJECT_ROOT/contrib/makefile/tests/testData")
class MakefileCreateRuleQuickfixTest {
  private val projectFixture = codeInsightProjectFixture()
  private val myFixture by codeInsightFixture(projectFixture)

  private lateinit var testName: String

  @Test
  fun testSimple(): Unit = doTest()
  @Test
  fun testMiddle(): Unit = doTest()

  @BeforeEach
  fun setUp(testInfo: TestInfo) {
    testName = PlatformTestUtil.getTestName(testInfo.testMethod.get().name, true)
    myFixture.enableInspections(MakefileUnresolvedPrerequisiteInspection::class.java)
  }

  fun doTest(): Unit = timeoutRunBlocking {
    myFixture.configureByFile("$basePath/$testName.mk")
    withContext(Dispatchers.EDT) {
      val intention = myFixture.findSingleIntention(MakefileLangBundle.message("intention.name.create.rule"))
      myFixture.launchAction(intention)
      myFixture.checkResultByFile("$basePath/$testName.gold.mk")
    }
  }

  private val basePath = "quickfix/createRule"
}