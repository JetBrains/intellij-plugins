package org.intellij.prisma

import com.intellij.testFramework.fixtures.BasePlatformTestCase

abstract class PrismaTestCase : BasePlatformTestCase() {
  override fun getTestDataPath(): String = "${getPrismaTestDataPath()}/$basePath"

  protected fun getTestName(ext: String = "prisma"): String = "${getTestName(true)}.$ext"
}
