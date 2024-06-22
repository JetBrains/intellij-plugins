package org.intellij.prisma.refactoring

import org.intellij.prisma.PrismaTestCase

class PrismaRenameTest : PrismaTestCase("rename") {
  fun testModel() {
    myFixture.testRename("model.prisma", "model_after.prisma", "SuperUser")
  }

  fun testModelGlobal() {
    checkSymbolRename("user.prisma", "NewUser")
  }
}