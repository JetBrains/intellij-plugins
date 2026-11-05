package org.jetbrains.qodana.dart.integration

import com.intellij.ide.starter.extended.engine.newTestContainerExtended
import com.intellij.ide.starter.ide.IDETestContext
import com.intellij.ide.starter.models.IdeInfo
import com.intellij.ide.starter.models.TestCase
import com.intellij.ide.starter.project.LocalProjectInfo
import com.intellij.ide.starter.runner.Starter
import com.intellij.openapi.application.PathManager
import com.intellij.tools.ide.starter.product.idea.ultimate.IdeaUltimate
import org.jetbrains.qodana.tests.utils.IntegrationTest
import org.jetbrains.qodana.tests.utils.addToQodanaYaml
import java.nio.file.Path
import kotlin.io.path.div

abstract class DartIntegrationTest : IntegrationTest() {
  override val testDataPath: Path
    get() = PathManager.getHomeDir() / "contrib/qodana/dart/integration-tests/test-data"

  override fun createTestContext(projectDir: Path): IDETestContext {
    val context = Starter.newTestContainerExtended().newContext(
      testName,
      TestCase(QODANA_POLY, LocalProjectInfo(projectDir)),
    )
    context.addToQodanaYaml(
      """
      profile:
        base:
          name: empty
        inspections:
          - inspection: DartAnalyze
            enabled: true
      """.trimIndent()
    )
    return context
  }

  protected companion object {
    val QODANA_POLY: IdeInfo = IdeInfo.IdeaUltimate.copy(
      platformPrefix = "QodanaIJVoid",
      qodanaProductCode = "QDPOLY",
      fullName = "Qodana Poly",
    )
  }
}
