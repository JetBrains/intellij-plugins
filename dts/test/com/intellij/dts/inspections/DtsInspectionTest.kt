package com.intellij.dts.inspections

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.dts.DtsTestBase
import kotlin.reflect.KClass

abstract class DtsInspectionTest(private vararg val inspectionClass: KClass<out LocalInspectionTool>) : DtsTestBase() {
  override fun setUp() {
    super.setUp()
    myFixture.enableInspections(inspectionClass.map { it.java })
  }

  protected fun doInspectionTest() {
    myFixture.configureByFile(testFile)
    myFixture.checkHighlighting()
  }

  protected fun doQuickfixTest(name: String) {
    myFixture.configureByFile("$testName.${getTestFileExtension()}")

    val intention = myFixture.filterAvailableIntentions(name).single()
    myFixture.checkPreviewAndLaunchAction(intention)
    myFixture.checkResultByFile("$testName-after.${getTestFileExtension()}", true)
  }
}