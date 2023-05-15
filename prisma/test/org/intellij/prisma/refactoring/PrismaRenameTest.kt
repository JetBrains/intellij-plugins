package org.intellij.prisma.refactoring

import org.intellij.prisma.PrismaTestCase

class PrismaRenameTest : PrismaTestCase() {

  override fun getBasePath(): String = "/rename"

  fun testModel() {
    myFixture.testRename("model.prisma", "model_after.prisma", "SuperUser")
  }
}