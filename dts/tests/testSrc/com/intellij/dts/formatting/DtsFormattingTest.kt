package com.intellij.dts.formatting

import com.intellij.application.options.CodeStyle
import com.intellij.dts.DTS_TEST_DATA_PATH
import com.intellij.dts.core.lang.DtsFileType
import com.intellij.dts.core.lang.DtsLanguage
import com.intellij.dts.core.settings.DtsCodeStyleSettings
import com.intellij.dts.dtsFixture
import com.intellij.openapi.project.Project
import com.intellij.psi.formatter.FormatterTestUtil
import com.intellij.testFramework.PlatformTestUtil
import com.intellij.testFramework.junit5.fixture.projectFixture
import com.intellij.testFramework.junit5.fixture.tempPathFixture
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInfo
import java.io.File

abstract class DtsFormattingTest {
  private val tempPathFixture = tempPathFixture()
  private val projectFixture = projectFixture(tempPathFixture, openAfterCreation = true)

  private val dtsFixture by dtsFixture(projectFixture)

  private val myFixture
    get() = dtsFixture.codeInsight

  private val project: Project get() = myFixture.project

  private lateinit var testName: String

  protected abstract fun getBasePath(): String

  @BeforeEach
  fun setUp(testInfo: TestInfo) {
    FormatterTestUtil.assertNoProjectCodeStyle(project)
    testName = PlatformTestUtil.getTestName(testInfo.testMethod.get().name, false)
  }

  protected fun doFormattingTest(
    keepLineBreaks: Boolean = false,
    alignPropertyAssignment: Boolean = false,
    alignPropertyValues: Boolean = false,
    maxBlankLinesBetweenProperties: Int = 1,
    minBlankLinesBetweenProperties: Int = 0,
    maxBlankLinesBetweenNodes: Int = 1,
    minBlankLinesBetweenNodes: Int = 1,
  ) {
    val settings = CodeStyle.getSettings(project)

    val indent = settings.getIndentOptions(DtsFileType)
    indent.USE_TAB_CHARACTER = false

    val common = settings.getCommonSettings(DtsLanguage)
    common.KEEP_LINE_BREAKS = keepLineBreaks

    val custom = settings.getCustomSettings(DtsCodeStyleSettings::class.java)
    custom.ALIGN_PROPERTY_ASSIGNMENT = alignPropertyAssignment
    custom.ALIGN_PROPERTY_VALUES = alignPropertyValues
    custom.MAX_BLANK_LINES_BETWEEN_PROPERTIES = maxBlankLinesBetweenProperties
    custom.MIN_BLANK_LINES_BETWEEN_PROPERTIES = minBlankLinesBetweenProperties
    custom.MAX_BLANK_LINES_BETWEEN_NODES = maxBlankLinesBetweenNodes
    custom.MIN_BLANK_LINES_BETWEEN_NODES = minBlankLinesBetweenNodes

    FormatterTestUtil.doTest(project, "$DTS_TEST_DATA_PATH/formatting" + File.separatorChar + getBasePath(), "dtsi", testName)
  }
}