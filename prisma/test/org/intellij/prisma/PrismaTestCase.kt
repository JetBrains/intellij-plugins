package org.intellij.prisma

import com.intellij.javascript.testFramework.web.WebFrameworkTestCase
import org.intellij.prisma.lang.PrismaFileType

abstract class PrismaTestCase(override val testCasePath: String) : WebFrameworkTestCase() {
  override val testDataRoot: String
    get() = getPrismaTestDataPath()
  override val defaultExtension: String
    get() = PrismaFileType.defaultExtension
  override val defaultDependencies: Map<String, String>
    get() = emptyMap()

  protected fun getTestFileName(ext: String = "prisma"): String = "${getTestName(true)}.$ext"
}
