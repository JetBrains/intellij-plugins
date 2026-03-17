package org.intellij.qodana.rust

import com.intellij.ide.starter.extended.allure.Subsystems
import com.intellij.ide.starter.models.IdeInfo
import com.intellij.ide.starter.ide.IDETestContext
import com.intellij.ide.starter.models.TestCase
import com.intellij.ide.starter.project.LocalProjectInfo
import com.intellij.openapi.application.PathManager
import com.intellij.rustrover.integration.testFramework.createRustRoverTestContext
import com.intellij.tools.ide.starter.build.server.rustrover.RustRover
import java.nio.file.Path
import kotlin.io.path.div

@Subsystems.Startup
abstract class IntegrationTest : utilities.qodana.IntegrationTest() {
    override val testDataPath: Path
        get() = PathManager.getHomeDir() / "contrib/qodana/rust/test-data"

    override fun createTestContext(projectDir: Path): IDETestContext =
        createRustRoverTestContext(
          testName,
          TestCase(IdeInfo.RustRover, LocalProjectInfo(projectDir)),
        )
}
