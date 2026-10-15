package com.intellij.dts.inspections

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.dts.DtsTestBase
import com.intellij.openapi.application.EDT
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.reflect.KClass

abstract class DtsInspectionTest(private vararg val inspectionClass: KClass<out LocalInspectionTool>) : DtsTestBase() {
  override fun setUp() {
    super.setUp()
    myFixture.enableInspections(inspectionClass.map { it.java })
  }

  protected open suspend fun doInspectionTest() {
    myFixture.configureByFile(testFile)
    myFixture.checkHighlighting()
  }

  protected suspend fun doQuickfixTest(name: String) {
    myFixture.configureByFile("$testName.${getTestFileExtension()}")

    val intention = myFixture.filterAvailableIntentions(name).single()
    withContext(Dispatchers.EDT) { myFixture.checkPreviewAndLaunchAction(intention) }
    myFixture.checkResultByFile("$testName-after.${getTestFileExtension()}", true)
  }
}