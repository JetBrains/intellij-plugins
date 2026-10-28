// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.prisma

import com.intellij.openapi.application.readAction
import com.intellij.openapi.progress.runBlockingCancellable
import com.intellij.openapi.vfs.VirtualFile
import org.intellij.prisma.ide.config.PrismaConfigLoader
import org.intellij.prisma.ide.config.PrismaConfigManager
import org.intellij.prisma.lang.resolve.getSchemaScope

class Prisma8ConfigTest : PrismaIntegrationTestBase() {
  override fun runFromCoroutine(): Boolean = true

  override fun runInDispatchThread(): Boolean = false

  fun testDefinePrismaConfig() = runBlockingCancellable {
    val config = addDefinePrismaConfig()
    val schema = myFixture.addFileToProject("prisma/contract.prisma", "// use prisma-8\n").virtualFile

    assertTrue(PrismaConfigLoader().isPrisma8Config(project, config))

    val configManager = PrismaConfigManager.getInstance(project)
    val loaded = configManager.getConfigForFile(schema, await = true)
    assertNotNull(loaded)
    assertNull(loaded!!.data)
    assertNull(configManager.getEvaluationError(config))
  }

  fun testSchemaRootFromLegacyScan() = runBlockingCancellable {
    addDefinePrismaConfig()
    val contract = myFixture.addFileToProject("prisma/schema/contract.prisma", "// use prisma-8\n")
    val sibling = myFixture.addFileToProject("prisma/schema/models.prisma", "// use prisma-8\n").virtualFile
    val outside = myFixture.addFileToProject("other/outside.prisma", "// use prisma-8\n").virtualFile

    // The config has no data, so the schema root is the `prisma/schema` directory of the legacy scan
    assertNull(PrismaConfigManager.getInstance(project).getConfigForFile(contract.virtualFile, await = true)!!.data)
    val scope = readAction { getSchemaScope(contract) }
    assertTrue(scope.contains(sibling))
    assertFalse(scope.contains(outside))
  }

  fun testInstalledPrisma8() = runBlockingCancellable {
    myFixture.addFileToProject("node_modules/prisma/package.json", """{ "name": "prisma", "version": "8.0.0-rc.28" }""")
    val config = myFixture.addFileToProject("prisma.config.ts", "export default {};").virtualFile

    assertTrue(PrismaConfigLoader().isPrisma8Config(project, config))
  }

  fun testPrisma7Config() = runBlockingCancellable {
    myFixture.addFileToProject("node_modules/prisma/package.json", """{ "name": "prisma", "version": "7.8.0" }""")
    val config = myFixture.addFileToProject(
      "prisma.config.ts",
      """
        import { defineConfig } from "prisma/config";
        export default defineConfig({ schema: "prisma/schema.prisma" });
      """.trimIndent(),
    ).virtualFile

    assertFalse(PrismaConfigLoader().isPrisma8Config(project, config))
  }

  private fun addDefinePrismaConfig(): VirtualFile = myFixture.addFileToProject(
    "prisma.config.ts",
    """
      import { definePrismaConfig } from "prisma/config";
      export default definePrismaConfig({});
    """.trimIndent(),
  ).virtualFile
}
