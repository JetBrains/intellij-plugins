package org.intellij.prisma.completion

import com.intellij.codeInsight.completion.CompletionType
import com.intellij.openapi.progress.runBlockingCancellable
import com.intellij.util.PathUtil
import org.intellij.prisma.PrismaIntegrationTestBase
import org.intellij.prisma.getPrismaRelativeTestDataPath
import org.intellij.prisma.ide.config.PrismaConfigManager
import org.intellij.prisma.lang.PrismaConstants
import java.time.Duration

class PrismaConfigCompletionTest : PrismaIntegrationTestBase() {
  override fun getBasePath(): String = "${getPrismaRelativeTestDataPath()}/completion/config"

  override fun runFromCoroutine(): Boolean = true

  override fun runInDispatchThread(): Boolean = false

  override fun getCoroutineTimeout(): Duration = Duration.ofMinutes(4)

  fun testConfigSplitSchema() = runBlockingCancellable {
    myFixture.copyDirectoryToProject(getTestName(true), "")
    performNpmInstallForPackageJson("package.json")
    val schemaDir = "prisma/schema/subDir"
    myFixture.configureFromTempProjectFile("$schemaDir/enum.prisma")
    val config = PrismaConfigManager.getInstance(project).getConfigForFile(myFixture.file.virtualFile, true)
    check(config != null)
    assertTrue(config.data?.schema?.let { PathUtil.toSystemIndependentName(it) }?.endsWith(schemaDir) == true)
    val lookupElements = myFixture.complete(CompletionType.BASIC, 1)!!
      .toList()
      .filter { it.lookupString !in PrismaConstants.PrimitiveTypes.ALL }
    assertSameElements(
      lookupElements.map { it.lookupString },
      "AuthRole",
      "Session",
      "UserView",
      "VerificationToken",
      "Test",
    )
  }
}