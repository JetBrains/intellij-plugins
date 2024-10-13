package org.jetbrains.qodana.staticAnalysis.script

import com.intellij.openapi.util.io.FileUtil
import org.assertj.core.api.AbstractObjectAssert
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.jetbrains.qodana.staticAnalysis.QodanaTestCase
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaConfig
import org.jetbrains.qodana.staticAnalysis.inspections.runner.OutputFormat
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaException
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaMessageReporter
import org.jetbrains.qodana.staticAnalysis.inspections.runner.startup.QodanaRunContextFactory
import org.jetbrains.qodana.staticAnalysis.script.scoped.ScopedRunContextFactory
import org.jetbrains.qodana.staticAnalysis.script.scoped.ScopedScript
import org.jetbrains.qodana.staticAnalysis.script.scoped.ScopedScriptFactory
import org.junit.Test
import org.junit.jupiter.api.fail
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.pathString

class ScopedScriptFactoryTest : QodanaTestCase() {
  private val subject = ScopedScriptFactory()

  private fun createScript(args: Map<String, String>, projectPath: Path = Path("/absolute/project")) =
    subject.createScript(
      config = QodanaConfig.fromYaml(
        projectPath,
        Path("/output/is/ignored"),
        resultsStorage = Path("/yet/another/ignored/path"),
        outputFormat = OutputFormat.SARIF_AND_PROJECT_STRUCTURE
      ),
      messageReporter = QodanaMessageReporter.EMPTY,
      contextFactory = QodanaRunContextFactory { fail("Not called", null) },
      parameters = UnvalidatedParameters("scoped", args)
    )

  private inline fun <reified R : Any> AbstractObjectAssert<*, *>.isInstance() =
    isExactlyInstanceOf(R::class.java).extracting { it as R }

  @Test
  fun `parse parameters should fail when blank`() = runTest {
    assertThatThrownBy { subject.parseParameters("") }
      .isExactlyInstanceOf(QodanaException::class.java)
      .message()
      .isEqualTo("Cannot start scoped script without scope file")

    assertThatThrownBy { subject.parseParameters(" ") }
      .isExactlyInstanceOf(QodanaException::class.java)
      .message()
      .isEqualTo("Cannot start scoped script without scope file")
  }

  @Test
  fun `create script should fail when absolute path does not exist`() = runTest {
    val path = Path("/absolutely/does/not/exist")
    assertThatThrownBy { createScript(mapOf("scope-file" to "/absolutely/does/not/exist")) }
      .isExactlyInstanceOf(QodanaException::class.java)
      .message()
      .isEqualTo("Scope file ${path.pathString} does not exist")
  }

  @Test
  fun `create script should fail when relative path does not exist`() = runTest {
    val path = Path("/absolute/project/relatively/does/not/exist")
    assertThatThrownBy { createScript(mapOf("scope-file" to "relatively/does/not/exist")) }
      .isExactlyInstanceOf(QodanaException::class.java)
      .message()
      .isEqualTo("Scope file ${path.pathString} does not exist")
  }

  @Test
  fun `create script uses absolute path`() = runTest {
    val scopeFile = FileUtil.createTempFile("scope", null)
    val script = createScript(mapOf("scope-file" to scopeFile.absolutePath))

    assertThat(script).isInstance<ScopedScript>()
      .extracting(DefaultScript::runContextFactory)
      .isInstance<ScopedRunContextFactory>()
      .extracting(ScopedRunContextFactory::scopeFile)
      .isEqualTo(Path(scopeFile.absolutePath))
  }

  @Test
  fun `create script resolves relative path`() = runTest {
    val scopeFile = "scope.txt"
    val proj = FileUtil.createTempDirectory("project", null)
    val projectPath = Path(proj.absolutePath)
    FileUtil.createTempFile(proj, scopeFile, null)

    val script = createScript(mapOf("scope-file" to scopeFile), projectPath = projectPath)

    assertThat(script).isInstance<ScopedScript>()
      .extracting(DefaultScript::runContextFactory)
      .isInstance<ScopedRunContextFactory>()
      .extracting(ScopedRunContextFactory::scopeFile)
      .isEqualTo(projectPath.resolve(scopeFile))
  }
}
