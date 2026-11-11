// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.prisma

import com.intellij.openapi.application.readAction
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.testFramework.common.timeoutRunBlocking
import com.intellij.testFramework.junit5.TestApplication
import org.intellij.prisma.ide.config.PrismaConfigLoader
import org.intellij.prisma.ide.config.PrismaConfigManager
import org.intellij.prisma.lang.resolve.getSchemaScope
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.minutes

@TestApplication
class Prisma8ConfigTest {
  private val prisma by prismaIntegrationFixture()

  @Test
  fun testDefinePrismaConfig(): Unit = timeoutRunBlocking(1.minutes) {
    val config = addDefinePrismaConfig()
    val schema = prisma.fixture.addFileToProject("prisma/contract.prisma", "// use prisma-8\n").virtualFile

    assertTrue(PrismaConfigLoader().isPrisma8Config(prisma.project, config))

    val configManager = PrismaConfigManager.getInstance(prisma.project)
    val loaded = configManager.getConfigForFile(schema, await = true)
    assertNotNull(loaded)
    assertNull(loaded!!.data)
    assertNull(configManager.getEvaluationError(config))
  }

  @Test
  fun testSchemaRootFromLegacyScan(): Unit = timeoutRunBlocking(1.minutes) {
    addDefinePrismaConfig()
    val contract = prisma.fixture.addFileToProject("prisma/schema/contract.prisma", "// use prisma-8\n")
    val sibling = prisma.fixture.addFileToProject("prisma/schema/models.prisma", "// use prisma-8\n").virtualFile
    val outside = prisma.fixture.addFileToProject("other/outside.prisma", "// use prisma-8\n").virtualFile

    // The config has no data, so the schema root is the `prisma/schema` directory of the legacy scan
    assertNull(PrismaConfigManager.getInstance(prisma.project).getConfigForFile(contract.virtualFile, await = true)!!.data)
    val scope = readAction { getSchemaScope(contract) }
    assertTrue(scope.contains(sibling))
    assertFalse(scope.contains(outside))
  }

  @Test
  fun testInstalledPrisma8(): Unit = timeoutRunBlocking(1.minutes) {
    prisma.fixture.addFileToProject("node_modules/prisma/package.json", """{ "name": "prisma", "version": "8.0.0-rc.28" }""")
    val config = prisma.fixture.addFileToProject("prisma.config.ts", "export default {};").virtualFile

    assertTrue(PrismaConfigLoader().isPrisma8Config(prisma.project, config))
  }

  @Test
  fun testPrisma7Config(): Unit = timeoutRunBlocking(1.minutes) {
    prisma.fixture.addFileToProject("node_modules/prisma/package.json", """{ "name": "prisma", "version": "7.8.0" }""")
    val config = prisma.fixture.addFileToProject(
      "prisma.config.ts",
      """
        import { defineConfig } from "prisma/config";
        export default defineConfig({ schema: "prisma/schema.prisma" });
      """.trimIndent(),
    ).virtualFile

    assertFalse(PrismaConfigLoader().isPrisma8Config(prisma.project, config))
  }

  private fun addDefinePrismaConfig(): VirtualFile = prisma.fixture.addFileToProject(
    "prisma.config.ts",
    """
      import { definePrismaConfig } from "prisma/config";
      export default definePrismaConfig({});
    """.trimIndent(),
  ).virtualFile
}
