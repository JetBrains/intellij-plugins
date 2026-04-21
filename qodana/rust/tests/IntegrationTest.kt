package org.intellij.qodana.rust

import com.intellij.ide.starter.extended.allure.Subsystems
import com.intellij.ide.starter.models.IdeInfo
import com.intellij.ide.starter.ide.IDETestContext
import com.intellij.ide.starter.models.TestCase
import com.intellij.ide.starter.project.LocalProjectInfo
import com.intellij.openapi.application.PathManager
import com.intellij.rustrover.integration.testFramework.createRustRoverTestContext
import com.intellij.tools.ide.starter.build.server.rustrover.RustRover
import org.junit.jupiter.api.Timeout
import utilities.qodana.QodanaAnalysisResult
import utilities.qodana.QodanaRunConfig
import java.nio.file.Path
import java.util.concurrent.TimeUnit
import kotlin.io.path.div
import kotlin.time.Duration.Companion.minutes

// Rust Qodana tests need a longer timeout than the default because when cargo build scripts fail
// (e.g., #![feature(rustc_private)] on stable Rust), the macro expansion pipeline takes ~4 minutes
// to complete — expanding declarative macros while proc macro calls time out individually.
@Subsystems.Startup
@Timeout(value = 11, unit = TimeUnit.MINUTES)
abstract class IntegrationTest : utilities.qodana.IntegrationTest() {
    override val testDataPath: Path
        get() = PathManager.getHomeDir() / "contrib/qodana/rust/test-data"

    override fun analyze(
        context: IDETestContext,
        dir: Path,
        configure: QodanaRunConfig.() -> Unit,
    ): QodanaAnalysisResult = super.analyze(context, dir) { timeout = 10.minutes; configure() }

    override fun createTestContext(projectDir: Path): IDETestContext =
        createRustRoverTestContext(
          testName,
          TestCase(IdeInfo.RustRover, LocalProjectInfo(projectDir)),
        )
}
