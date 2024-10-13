package org.jetbrains.qodana.staticAnalysis.stat

import com.intellij.internal.statistic.FUCollectorTestCase
import com.intellij.testFramework.HeavyPlatformTestCase
import com.jetbrains.fus.reporting.model.lion3.LogEvent
import org.jetbrains.qodana.staticAnalysis.inspections.config.*
import org.jetbrains.qodana.staticAnalysis.stat.UsageCollector.profileForReporting
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.nio.file.Paths

@RunWith(JUnit4::class)
class UsageCollectorTest : HeavyPlatformTestCase() {

  @Test
  fun splitEnv() {
    fun case(env: String, system: String, version: String? = null, build: String? = null) {
      val actual = UsageCollector.splitEnv(env)
      assertEquals(system, actual.system)
      assertEquals(version, actual.version)
      assertEquals(build, actual.build)
    }

    // There are a handful of known build environments.
    case("github-actions", "github-actions")
    case("azure-pipelines", "azure-pipelines")
    case("cli", "cli")
    case("teamcity", "teamcity")
    case("teamcity-cloud", "teamcity-cloud")
    case("gradle", "gradle")

    // All other environments are reported as "other".
    case("other", "other")
    case("docker", "other")
    case("bitbucket", "other")
    case("aws", "other")
    case("unknown", "other")
    case("", "other")

    // For known build systems, their version is recorded.
    // Each build system has its own version scheme, most use numeric versions.
    case("github-actions:2021", "github-actions", "2021")
    case("github-actions:1.2.3.004", "github-actions", "1.2.3.004")
    case("teamcity:2022.04_EAP", "teamcity", "2022.04_EAP")

    // Versions that don't match a numeric version with an optional "_EAP" are not reported.
    case("github:1.0-EAP", "other")

    // If the build system is not known, the version information is not reported either.
    case("unknown:2021.1.4:92954", "other")

    // For TeamCity, the build number is included since we have complete control over how much information it conveys.
    // For other environments, the build number might reveal too detailed information, so it is not reported.
    case("teamcity:2021.1.4:92954", "teamcity", "2021.1.4", "92954")
    case("teamcity-cloud:2021.1.4:123456789", "teamcity-cloud", "2021.1.4", "123456789")
    case("github-actions:2021:123456789", "github-actions", "2021")

    // There is no build number 0.
    case("teamcity-cloud:2021.1.4:0", "other")
    // The longest allowed build number is 9 digits, don't report anything longer.
    case("teamcity:2021.1.4:1234567890", "other")

    // In case of a syntax error, "other" is reported, no exception is thrown.
    case("teamcity:2021.1.4:92954:too-much", "other")
  }


  @Test
  fun logEnv_null() {
    val event = collectEvent {
      UsageCollector.logEnv(null)
    }

    assertEvent(event, "env",
                "system" to "other")
  }

  @Test
  fun logEnv_unknown() {
    val event = collectEvent {
      UsageCollector.logEnv("unknown")
    }

    assertEvent(event, "env",
                "system" to "other")
  }

  @Test
  fun logEnv_wrong_format() {
    val event = collectEvent {
      UsageCollector.logEnv("wrong:format::::")
    }

    assertEvent(event, "env",
                "system" to "other")
  }

  @Test
  fun logEnv_teamcity_with_version() {
    val event = collectEvent {
      UsageCollector.logEnv("teamcity:2021.1.4")
    }

    assertEvent(event, "env",
                "system" to "teamcity",
                "version" to "2021.1.4")
  }

  @Test
  fun logEnv_teamcity_with_version_and_build() {
    val event = collectEvent {
      UsageCollector.logEnv("teamcity:2021.1.4:12345")
    }

    assertEvent(event, "env",
                "system" to "teamcity",
                "version" to "2021.1.4",
                "build" to "12345")
  }

  @Test
  fun logConfig_default() {
    val event = collectEvent {
      val config = QodanaConfig.fromYaml(Paths.get("").toAbsolutePath(), Paths.get(""))
      UsageCollector.logConfig(config, "qodana.starter", "")
    }

    assertEvent(event, "config",
                "bootstrap" to false,
                "profile" to "starter",
                "include" to 0,
                "exclude" to 0,
                "baselineType" to "none",
                "fixesStrategy" to "none",
                "includeAbsent" to false,
                "sourceDirectory" to false
    )
  }

  @Test
  fun logConfig_custom() {
    val event = collectEvent {
      val config = QodanaConfig.fromYaml(
        Paths.get("").toAbsolutePath(),
        Paths.get(""),
        yaml = QodanaYamlConfig(
          include = listOf(
            InspectScope(
              name = "include-name",
              paths = listOf("src/main/java"),
              patterns = listOf("*Impl.java"))),
          exclude = listOf(
            InspectScope(name = "exclude1"),
            InspectScope(name = "exclude2"),
            InspectScope(name = "exclude3")),
          failThreshold = 123,
          bootstrap = "echo 'Starting analysis'"
        ),
        profile = QodanaProfileConfig(
          name = "qodana.recommended",
          path = "irrelevant path to inspection profile.xml"
        ),
        baseline = "something not null",
        fixesStrategy = FixesStrategy.CLEANUP,
        includeAbsent = true,
        sourceDirectory = "another thing that's not null",
      )
      UsageCollector.logConfig(config, "qodana.recommended", "")
    }

    assertEvent(event, "config",
                "bootstrap" to true,
                "profile" to "recommended",
                "include" to 1,
                "exclude" to 4,
                "failureConditionAnySeverity" to 128,
                "baselineType" to "local",
                "fixesStrategy" to "cleanup",
                "includeAbsent" to true,
                "sourceDirectory" to true
    )
  }

  @Test
  fun profileForReporting() {
    // There are 3 predefined Qodana inspection profiles.
    assertEquals("empty", profileForReporting("empty", "any path"))
    assertEquals("starter", profileForReporting("qodana.starter", "any path"))
    assertEquals("recommended", profileForReporting("qodana.recommended", "any path"))

    // For now, only report that Qodana has been run with a single inspection,
    // but don't report which inspection has been selected.
    // As this option is undocumented, this will not happen often.
    // Reporting the selected inspection would also become too detailed.
    assertEquals("single", profileForReporting("qodana.single:InspectionName", "any path"))

    // When the profile is selected by specifying a path, only report that some path has been used.
    // Do not report the actual path, as that may reveal too much about the project.
    assertEquals("path", profileForReporting("", "custom inspection profile.xml"))

    // If neither name nor path is given in the command line or qodana.yaml,
    // Qodana uses the default profile from the system property 'qodana.default.profile'.
    // To avoid confusion with the IDEA default profile, it is reported as 'absent'.
    assertEquals("absent", profileForReporting("", ""))

    // Anything unexpected maps to "other".
    assertEquals("other", profileForReporting("user-defined", "any path"))
    assertEquals("other", profileForReporting("starter", "any path"))
    assertEquals("other", profileForReporting("qodana.single", "any path"))
  }

  private fun collectEvent(action: () -> Unit): LogEvent {
    val events = FUCollectorTestCase.collectLogEvents(testRootDisposable) { action() }
    return events.single { it.group.id == "qodana.usage" }
  }

  private fun assertEvent(event: LogEvent, eventId: String, vararg data: Pair<String, Any>) {
    val expected = listOf(eventId, data.toMap().toSortedMap())
    val actual = listOf(event.event.id, event.event.data.toSortedMap())
    assertEquals(expected, actual)
  }
}
