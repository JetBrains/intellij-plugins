package org.jetbrains.qodana.ijVoid

import com.intellij.ide.starter.extended.allure.Subsystems
import com.intellij.ide.starter.extended.engine.newTestContainerExtended
import com.intellij.ide.starter.ide.IDETestContext
import com.intellij.ide.starter.models.IdeInfo
import com.intellij.ide.starter.models.TestCase
import com.intellij.ide.starter.project.LocalProjectInfo
import com.intellij.ide.starter.runner.Starter
import com.intellij.openapi.application.PathManager
import com.intellij.tools.ide.starter.product.idea.ultimate.IdeaUltimate
import java.nio.file.Path
import kotlin.io.path.div

@Subsystems.Startup
abstract class IntegrationTest : org.jetbrains.qodana.tests.utils.IntegrationTest() {
  override val testDataPath: Path
    get() = PathManager.getHomeDir() / "contrib/qodana/ijVoid/test-data"

  override fun createTestContext(projectDir: Path): IDETestContext =
    Starter.newTestContainerExtended().newContext(
      testName,
      TestCase(QODANA_IJ_VOID, LocalProjectInfo(projectDir)),
    )

  private companion object {
    val QODANA_IJ_VOID: IdeInfo = IdeInfo.IdeaUltimate.copy(
      platformPrefix = "QodanaIJVoid",
      qodanaProductCode = "QDIV",
      fullName = "Qodana for IJ Void",
    )
  }
}
