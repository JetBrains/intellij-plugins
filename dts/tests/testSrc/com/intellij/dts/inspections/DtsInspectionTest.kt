package com.intellij.dts.inspections

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.dts.dtsFixture
import com.intellij.dts.dtsProjectFixture
import com.intellij.openapi.application.EDT
import com.intellij.openapi.application.writeIntentReadAction
import com.intellij.testFramework.junit5.fixture.tempPathFixture
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.junit.jupiter.api.BeforeEach
import kotlin.reflect.KClass

abstract class DtsInspectionTest(
  private vararg val inspectionClass: KClass<out LocalInspectionTool>,
  needsZephyr: Boolean,
  extension: String = "dtsi",
) {
  private val tempPathFixture = tempPathFixture()
  private val projectFixture = dtsProjectFixture(tempPathFixture, withZephyr = needsZephyr, openAfterCreation = true)

  protected val dts by dtsFixture(projectFixture, extension)

  protected val myFixture get() = dts.codeInsight

  @BeforeEach
  fun enableInspections() {
    myFixture.enableInspections(inspectionClass.map { it.java })
  }

  protected suspend fun doInspectionTest() {
    myFixture.configureByFile(dts.testFile)
    writeIntentReadAction { myFixture.checkHighlighting() }
  }

  protected suspend fun doQuickfixTest(name: String) {
    myFixture.configureByFile(dts.testFile)

    val intention = myFixture.filterAvailableIntentions(name).single()
    withContext(Dispatchers.EDT) { myFixture.checkPreviewAndLaunchAction(intention) }
    myFixture.checkResultByFile("${dts.testName}-after.${dts.extension}", true)
  }
}