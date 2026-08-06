package org.jetbrains.qodana.python.community

import com.intellij.python.junit5Tests.framework.env.PyEnvTestCase
import com.intellij.python.junit5Tests.framework.env.PythonBinaryPath
import com.intellij.testFramework.common.timeoutRunBlocking
import com.intellij.testFramework.common.withEnvVars
import com.intellij.testFramework.junit5.EnvValue
import com.intellij.testFramework.junit5.fixture.projectFixture
import com.jetbrains.python.PythonBinary
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.io.path.pathString

@PyEnvTestCase
internal class QodanaPythonPathConfigurationTest {
  companion object {
    private val projectFixture = projectFixture()
  }

  private val project by projectFixture

  @Test
  fun testNoQodanaPythonPath(): Unit = timeoutRunBlocking {
    assertEquals(
      QodanaPythonPathConfigurationResult.NoPath,
      configureSdkFromQodanaPythonPath(project, emptyArray()),
    )
  }

  @Test
  @EnvValue("QODANA_PYTHON_PATH", "asdasdasdasd/:;/\\\u0000")
  fun testInvalidQodanaPythonPath(): Unit = timeoutRunBlocking {
    assertEquals(
      QodanaPythonPathConfigurationResult.Failure,
      configureSdkFromQodanaPythonPath(project, emptyArray()),
    )
  }

  @Test
  fun testValidQodanaPythonPath(@PythonBinaryPath pythonPath: PythonBinary): Unit = timeoutRunBlocking {
    withEnvVars("QODANA_PYTHON_PATH" to pythonPath.pathString) {
      assertEquals(
        QodanaPythonPathConfigurationResult.Success,
        configureSdkFromQodanaPythonPath(project, emptyArray()),
      )
    }
  }
}
