package org.jetbrains.qodana.staticAnalysis.inspections.config

import com.intellij.idea.TestFor
import com.intellij.openapi.util.SystemInfo.isUnix
import com.intellij.testFramework.HeavyPlatformTestCase
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.intellij.lang.annotations.Language
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.fail
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.pathString
import kotlin.io.path.writeText

@RunWith(JUnit4::class)
class QodanaYamlConfigTest : HeavyPlatformTestCase() {

  @Before
  fun createTemporaryDirectory() {
    tempDir.createDir() // The TemporaryDirectory doesn't create the root directory by itself.
  }

  /**
   * When the inspection profile is specified by an absolute path,
   * it is kept as-is, as it can always be resolved later.
   */
  @Test
  @TestFor(issues = ["QD-4429"])
  fun `load with absolute profile path`() {
    org.junit.Assume.assumeTrue(isUnix)
    writeQodanaYaml("""
      version: 1.0
      profile:
        path: /absolute
    """)

    val config = loadQodanaYaml()

    assertThat(config.profile.path).isEqualTo("/absolute")
  }

  /**
   * When the inspection profile is specified using a relative path,
   * the relative path is resolved early, when loading the file.
   * This is the last moment where the path of {@code qodana.yaml}
   * is still known.
   */
  @Test
  @TestFor(issues = ["QD-4429"])
  fun `load with relative profile path`() {
    writeQodanaYaml("""
      version: 1.0
      profile:
        path: relative
    """)

    val config = loadQodanaYaml()

    assertThat(config.profile.path).isEqualTo(Paths.get(project.basePath!!).resolve("relative").pathString)
  }

  /**
   * When the inspection profile is specified using its name instead
   * of its path, the path is not touched.
   */
  @Test
  @TestFor(issues = ["QD-4429"])
  fun `load with profile name`() {
    writeQodanaYaml("""
      version: 1.0
      profile:
        name: Profile Name
    """)

    val config = loadQodanaYaml()

    assertThat(config.profile.path).isEqualTo("")
    assertThat(config.profile.name).isEqualTo("Profile Name")
  }


  private fun writeQodanaYaml(@Language("YAML") yaml: String) =
    qodanaYamlPath().writeText(yaml.trimIndent())

  private fun loadQodanaYaml() = runBlocking {
    val projectPath = Paths.get(project.basePath!!)
    QodanaYamlReader.load(qodanaYamlPath())
      .getOrElse { fail(it) }.withAbsoluteProfilePath(projectPath, qodanaYamlPath())
  }

  private fun qodanaYamlPath(): Path {
    return Paths.get(project.basePath!!).resolve("qodana.yaml")
  }
}