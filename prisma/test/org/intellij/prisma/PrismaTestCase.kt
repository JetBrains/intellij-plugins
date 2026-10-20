package org.intellij.prisma

import com.intellij.javascript.testFramework.web.WebFrameworkFixture
import com.intellij.javascript.testFramework.web.webFrameworkFixture
import com.intellij.openapi.project.Project
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import org.intellij.prisma.lang.PrismaFileType

abstract class PrismaTestCase(testCasePath: String) {
  protected val web: WebFrameworkFixture by webFrameworkFixture(
    testDataRoot = getPrismaTestDataPath(),
    testCasePath = testCasePath,
    defaultExtension = PrismaFileType.defaultExtension,
  )

  protected val myFixture: CodeInsightTestFixture get() = web.fixture

  protected val project: Project get() = web.project

  protected val testDataPath: String get() = web.testDataPath

  protected fun getTestFileName(ext: String = "prisma"): String = "${web.getTestName(true)}.$ext"
}
