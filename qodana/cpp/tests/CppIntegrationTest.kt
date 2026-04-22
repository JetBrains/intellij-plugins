package org.jetbrains.qodana.cpp

import com.intellij.ide.starter.extended.CppTestContext
import com.intellij.ide.starter.extended.LanguageEngine
import com.intellij.ide.starter.extended.allure.Subsystems
import com.intellij.ide.starter.extended.getCLionContext
import com.intellij.ide.starter.ide.IDETestContext
import com.intellij.ide.starter.ide.IdeProductProvider
import com.intellij.ide.starter.models.TestCase
import com.intellij.ide.starter.project.LocalProjectInfo
import com.intellij.openapi.application.PathManager
import utilities.qodana.IntegrationTest
import java.nio.file.Path
import kotlin.io.path.div

@Subsystems.Startup
abstract class CppIntegrationTest : IntegrationTest() {
  override val testDataPath: Path
    get() = PathManager.getHomeDir() / "contrib/qodana/cpp/test-data"

  override fun createTestContext(projectDir: Path): IDETestContext =
    getCLionContext(
      testName,
      TestCase(IdeProductProvider.CL, LocalProjectInfo(projectDir)),
      context = CppTestContext.CLION,
      engine = LanguageEngine.NOVA,
    )
}
