package org.intellij.prisma.completion

import com.intellij.codeInsight.completion.CompletionType
import com.intellij.testFramework.common.timeoutRunBlocking
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.util.PathUtil
import org.assertj.core.api.Assertions.assertThat
import org.intellij.prisma.getPrismaRelativeTestDataPath
import org.intellij.prisma.ide.config.PrismaConfigManager
import org.intellij.prisma.lang.PrismaConstants
import org.intellij.prisma.prismaIntegrationFixture
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.minutes

@TestApplication
class PrismaConfigCompletionTest {
  private val prisma by prismaIntegrationFixture("${getPrismaRelativeTestDataPath()}/completion/config")

  @Test
  fun testConfigSplitSchema(): Unit = timeoutRunBlocking(4.minutes) {
    prisma.fixture.copyDirectoryToProject(prisma.getTestName(true), "")
    prisma.performNpmInstallForPackageJson("package.json")
    val schemaDir = "prisma/schema/subDir"
    prisma.fixture.configureFromTempProjectFile("$schemaDir/enum.prisma")
    val config = PrismaConfigManager.getInstance(prisma.project).getConfigForFile(prisma.fixture.file.virtualFile, true)
    check(config != null)
    assertTrue(config.data?.schema?.let { PathUtil.toSystemIndependentName(it) }?.endsWith(schemaDir) == true)
    val lookupElements = prisma.fixture.complete(CompletionType.BASIC, 1)!!
      .toList()
      .filter { it.lookupString !in PrismaConstants.PrimitiveTypes.ALL }
    val expected = listOf(
      "AuthRole",
      "Session",
      "UserView",
      "VerificationToken",
      "Test",
    )
    assertThat(lookupElements.map { it.lookupString }).hasSameElementsAs(expected).hasSameSizeAs(expected)
  }
}