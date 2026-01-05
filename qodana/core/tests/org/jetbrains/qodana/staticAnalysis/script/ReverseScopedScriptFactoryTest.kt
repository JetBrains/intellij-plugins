package org.jetbrains.qodana.staticAnalysis.script

import com.intellij.openapi.util.io.FileUtil
import org.assertj.core.api.AbstractObjectAssert
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.jetbrains.qodana.staticAnalysis.QodanaTestCase
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaConfig
import org.jetbrains.qodana.staticAnalysis.inspections.runner.OutputFormat
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaException
import org.jetbrains.qodana.util.QodanaMessageReporter
import org.jetbrains.qodana.staticAnalysis.inspections.runner.startup.QodanaRunContextFactory
import org.jetbrains.qodana.staticAnalysis.script.scoped.ReverseScopedRunContextFactory
import org.jetbrains.qodana.staticAnalysis.script.scoped.ReverseScopedRunNewCodeContextFactory
import org.jetbrains.qodana.staticAnalysis.script.scoped.ReverseScopedScriptFactory
import org.jetbrains.qodana.staticAnalysis.script.scoped.ReverseScopedScriptFixes
import org.jetbrains.qodana.staticAnalysis.script.scoped.ReverseScopedScriptNew
import org.jetbrains.qodana.staticAnalysis.script.scoped.ReverseScopedScriptOld
import org.jetbrains.qodana.staticAnalysis.script.scoped.SCOPE_ARG
import org.jetbrains.qodana.staticAnalysis.script.scoped.STAGE_ARG
import org.jetbrains.qodana.staticAnalysis.script.scoped.Stage
import org.junit.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.fail
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.pathString

class ReverseScopedScriptFactoryTest : QodanaTestCase() {
  private val subject = ReverseScopedScriptFactory()

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
      parameters = UnvalidatedParameters("reverse-scoped", args)
    )

  private inline fun <reified R : Any> AbstractObjectAssert<*, *>.isInstance() =
    isExactlyInstanceOf(R::class.java).extracting { it as R }

  @Test
  fun `parse parameters should fail when blank`() = runTest {
    assertThatThrownBy { subject.parseParameters("") }
      .isExactlyInstanceOf(QodanaException::class.java)
      .message()
      .isEqualTo("Cannot start reverse-scoped script without scope file and stage")

    assertThatThrownBy { subject.parseParameters(" ") }
      .isExactlyInstanceOf(QodanaException::class.java)
      .message()
      .isEqualTo("Cannot start reverse-scoped script without scope file and stage")
  }

  @Test
  fun `parse parameters should fail when stage is missing`() = runTest {
    assertThatThrownBy { subject.parseParameters("some.file.json") }
      .isExactlyInstanceOf(QodanaException::class.java)
      .message()
      .isEqualTo("Cannot start reverse-scoped script: Unknown stage some.file.json, expected one of NEW, OLD, FIXES")

    assertThatThrownBy { subject.parseParameters("UNKNOWN,some.file.json") }
      .isExactlyInstanceOf(QodanaException::class.java)
      .message()
      .isEqualTo("Cannot start reverse-scoped script: Unknown stage UNKNOWN, expected one of NEW, OLD, FIXES")
  }

  @Test
  fun `parse parameters doesn't fail on expected input`() = runTest {
    assertDoesNotThrow {
      subject.parseParameters("NEW,some.file.json")
    }
    assertDoesNotThrow {
      subject.parseParameters("NEW,some,file.json")
    }
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
  fun `create script uses stages correctly`() = runTest {
    val scopeFile = FileUtil.createTempFile("scope", null)
    assertThat(createScript(mapOf(SCOPE_ARG to scopeFile.absolutePath, STAGE_ARG to Stage.NEW.name)))
      .isInstance<ReverseScopedScriptNew>()

    assertThat(createScript(mapOf(SCOPE_ARG to scopeFile.absolutePath, STAGE_ARG to Stage.OLD.name)))
      .isInstance<ReverseScopedScriptOld>()

    assertThat(createScript(mapOf(SCOPE_ARG to scopeFile.absolutePath, STAGE_ARG to Stage.FIXES.name)))
      .isInstance<ReverseScopedScriptFixes>()
  }


  @Test
  fun `create script uses absolute path`() = runTest {
    val scopeFile = FileUtil.createTempFile("scope", null)
    val script = createScript(mapOf(SCOPE_ARG to scopeFile.absolutePath, STAGE_ARG to Stage.NEW.name))

    assertThat(script).isInstance<ReverseScopedScriptNew>()
      .extracting(DefaultScript::runContextFactory)
      .isInstance<ReverseScopedRunNewCodeContextFactory>()
      .extracting(ReverseScopedRunContextFactory::scopeFile)
      .isEqualTo(Path(scopeFile.absolutePath))
  }

  @Test
  fun `create script resolves relative path`() = runTest {
    val scopeFile = "scope.txt"
    val proj = FileUtil.createTempDirectory("project", null)
    val projectPath = Path(proj.absolutePath)
    FileUtil.createTempFile(proj, scopeFile, null)

    val script = createScript(mapOf(SCOPE_ARG to scopeFile, STAGE_ARG to Stage.NEW.name), projectPath = projectPath)

    assertThat(script).isInstance<ReverseScopedScriptNew>()
      .extracting(DefaultScript::runContextFactory)
      .isInstance<ReverseScopedRunNewCodeContextFactory>()
      .extracting(ReverseScopedRunContextFactory::scopeFile)
      .isEqualTo(projectPath.resolve(scopeFile))
  }
}
