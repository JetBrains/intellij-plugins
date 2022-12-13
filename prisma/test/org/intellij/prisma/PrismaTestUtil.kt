// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.prisma

import com.intellij.testFramework.fixtures.IdeaTestExecutionPolicy
import java.io.File

private const val PRISMA_TEST_DATA_PATH = "/prisma/test/testData"

fun getPrismaTestDataPath(): String =
  getContribPath() + PRISMA_TEST_DATA_PATH

fun getPrismaRelativeTestDataPath(): String = "/contrib$PRISMA_TEST_DATA_PATH"

private fun getContribPath(): String {
  val homePath = IdeaTestExecutionPolicy.getHomePathWithPolicy()
  return if (File(homePath, "contrib/.gitignore").isFile) {
    homePath + File.separatorChar + "contrib"
  }
  else homePath
}