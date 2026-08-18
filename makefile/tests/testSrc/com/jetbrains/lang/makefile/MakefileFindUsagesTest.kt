package com.jetbrains.lang.makefile

import com.intellij.codeInsight.TargetElementUtil
import com.intellij.find.FindManager
import com.intellij.find.impl.FindManagerImpl
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.hamcrest.core.IsNull.nullValue
import org.junit.Assert.assertThat

class MakefileFindUsagesTest : BasePlatformTestCase() {
  fun testSimple() {
    val usages = myFixture.testFindUsages("$basePath/${getTestName(true)}.mk")

    assertEquals(2, usages.size)
  }

  fun testPhony() = notSearchableForUsages()
  fun testForce() = notSearchableForUsages()

  fun notSearchableForUsages() {
    myFixture.configureByFiles("$basePath/${getTestName(true)}.mk")
    val targetElement = TargetElementUtil.findTargetElement(myFixture.editor, TargetElementUtil.ELEMENT_NAME_ACCEPTED or TargetElementUtil.REFERENCED_ELEMENT_ACCEPTED)
    val handler = (FindManager.getInstance(project) as FindManagerImpl).findUsagesManager.getFindUsagesHandler(targetElement!!, false)

    assertThat(handler, nullValue())
  }

  override fun getTestDataPath() = BASE_TEST_DATA_PATH
  override fun getBasePath() = "findUsages"
}