package org.intellij.prisma.refactoring

import com.intellij.openapi.application.EDT
import com.intellij.testFramework.common.timeoutRunBlocking
import com.intellij.testFramework.junit5.TestApplication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.intellij.prisma.PrismaFixture
import org.intellij.prisma.prismaFixture
import org.junit.jupiter.api.Test

@TestApplication
class PrismaRenameTest {
  private val prisma: PrismaFixture by prismaFixture("rename")

  @Test
  fun testModel(): Unit = timeoutRunBlocking {
    withContext(Dispatchers.EDT) {
      prisma.fixture.testRename("model.prisma", "model_after.prisma", "SuperUser")
    }
  }

  @Test
  fun testModelGlobal() {
    prisma.doSymbolRenameTest("user.prisma", "NewUser")
  }
}