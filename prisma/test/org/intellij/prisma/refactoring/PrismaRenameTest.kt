package org.intellij.prisma.refactoring

import com.intellij.openapi.application.EDT
import com.intellij.testFramework.common.timeoutRunBlocking
import com.intellij.testFramework.junit5.TestApplication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.intellij.prisma.PrismaTestCase
import org.junit.jupiter.api.Test

@TestApplication
class PrismaRenameTest : PrismaTestCase("rename") {
  @Test
  fun testModel(): Unit = timeoutRunBlocking {
    withContext(Dispatchers.EDT) {
      myFixture.testRename("model.prisma", "model_after.prisma", "SuperUser")
    }
  }

  @Test
  fun testModelGlobal() {
    web.doSymbolRenameTest("user.prisma", "NewUser")
  }
}