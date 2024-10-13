package org.jetbrains.qodana.staticAnalysis.inspections.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.intellij.testFramework.assertInstanceOf
import org.intellij.lang.annotations.Language
import org.jetbrains.qodana.staticAnalysis.inspections.runner.OutputFormat
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaException
import org.jetbrains.qodana.yaml.QODANA_YAML_SCHEMA_RESOURCE
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.jupiter.api.assertThrows
import java.nio.file.Path

class QodanaConfigTest {

  @Test
  fun `garbage input fail with QodanaException`() {
    val garbageException = QodanaYamlReader.parse("foo")
      .exceptionOrNull()
    assertInstanceOf<QodanaException>(garbageException)
    assertEquals("Not a valid qodana.yaml configuration 'foo'", garbageException?.message)
  }

  @Test
  fun `missing version does not throw`() {
    val actual = load("""
      profile:
        name: qodana.recommended
    """.trimIndent())
    assertEquals(actual.profile.name, "qodana.recommended")
  }

  @Test
  fun `all qodanaYaml can be parsed from empty yaml`() {
    QodanaYamlReader.parse("").getOrThrow()
  }

  @Test
  fun `wrong version`() {
    val yaml = load("""
      version: 2.0
      profile:
        name: qodana.recommended
    """.trimIndent())

    val exception = assertThrows<QodanaException> {
      getQodanaConfig(yaml)
    }

    assertEquals("Property \"version\" in qodana.yaml must be \"1.0\", not \"2.0\"", exception.message)
  }

  @Test
  fun `wrong version, given as integer`() {
    val yaml = load("""
      version: 1
      profile:
        name: qodana.recommended
    """.trimIndent())

    val exception = assertThrows<QodanaException> {
      getQodanaConfig(yaml)
    }

    assertEquals("Property \"version\" in qodana.yaml must be \"1.0\", not \"1\"", exception.message)
  }

  @Test
  fun `failOnError, but no errors collected`() {
    val yaml = load("""
      version: 1.0
      profile:
        name: qodana.recommended
      failOnErrorNotification: true
      maxRuntimeNotifications: -1
    """.trimIndent())

    val exception = assertThrows<QodanaException> {
      getQodanaConfig(yaml)
    }

    assertEquals("Cannot enable 'failOnErrorNotification' when 'maxRuntimeNotifications' is less than 1", exception.message)
  }

  @Test
  fun `failOnErrors with default maxRuntimeNotifications`() {
    val yaml = load("""
      version: 1.0
      profile:
        name: qodana.recommended
      failOnErrorNotification: true
    """.trimIndent())

    val actual = getQodanaConfig(yaml)

    assertEquals(true, actual.failOnErrorNotification)
  }

  private fun getQodanaConfig(yaml: QodanaYamlConfig) =
    QodanaConfig.fromYaml(
      Path.of(".").toAbsolutePath(),
      Path.of("unused"),
      yaml = yaml,
      resultsStorage = Path.of("these cause an env lookup so ignore them"),
      outputFormat = OutputFormat.SARIF_AND_PROJECT_STRUCTURE
    )

  @Test
  fun `qodana_yaml with unknown options`() {
    // Including or excluding inspections is done with 'include' and 'exclude', not 'inspections'.
    val ex = assertThrows<QodanaException> {
      load("""
      version: 1.0
      profile:
        name: qodana.recommended
      inspections:
        LicenseAudit:
          dependencyScopes:
            - type: "gradle"
              scopes:
                - "testRuntimeClasspath"
    """.trimIndent())
    }

    assertEquals("Unexpected keys in qodana.yaml: [inspections]", ex.message)
  }

  @Test
  fun `qodana_yaml containing a profile name with space`() {
    val config = load("""
      version: 1.0
      profile:
        name: two words
    """.trimIndent()).let(::getQodanaConfig)
    assertEquals("two words", config.profile.name)
  }

  @Test
  fun `qodana_yaml containing a profile path with spaces`() {
    val config = load("""
      version: 1.0
      profile:
        path: /home/two words/target
    """.trimIndent()).let(::getQodanaConfig)
    assertEquals("/home/two words/target", config.profile.path)
  }

  @Test
  fun `yaml with both name and path`() {
    val config = load("""
      version: 1.0
      profile:
        name: name
        path: path
    """.trimIndent()).let(::getQodanaConfig)
    assertEquals("name", config.profile.name)
    assertEquals("path", config.profile.path)
  }

  @Test
  fun `yaml with version only`() {
    val config = load("version: 1.0")
    assertEquals("1.0", config.version)
  }

  @Test
  fun `yaml with defaults`() {
    val config = load("version: 1.0")
    assertEquals("default", config.script.name)
  }

  @Test
  fun `yaml with include and exclude, several inspections and several paths`() {
    val config = load("""
      version: 1.0
      profile:
        name: qodana.recommended
      include:
        - name: SomeInspectionId
        - name: AnotherInspectionId
          paths:
          - tests
          - src/main/java/org
      exclude:
        - name: Annotator
        - name: AnotherInspectionId
          paths:
            - relative/path
            - another/relative/path
        - name: All
          paths:
            - asm-test/src/main/java/org
            - benchmarks
            - tools    
    """.trimIndent())

    assertEquals(2, config.include.size)
    assertEquals(2, config.include[1].paths.size)
    assertEquals(3, config.exclude.size)
    assertEquals(3, config.exclude[2].paths.size)
  }

  @Test
  fun `yaml include`() {
    testProviderInspectionScopes("include", { it.include })
  }

  @Test
  fun `yaml include with two paths`() {
    testProviderInspectionScopesWithTwoPath("include", { it.include })
  }

  @Test
  fun `yaml exclude`() {
    testProviderInspectionScopes("exclude", { it.exclude })
  }

  @Test
  fun `yaml exclude with two paths`() {
    testProviderInspectionScopesWithTwoPath("exclude", { it.exclude })
  }

  @Test
  fun `yaml failThreshold`() {
    val config = load("""
      version: "1.0"
      failThreshold: 17
      """.trimIndent())

    assertEquals(
      FailureConditions(FailureConditions.SeverityThresholds(any = 17)),
      config.failureConditions
    )
  }

  @Test
  fun `yaml failureThresholds`() {
    val config = load("""
      version: "1.0"
      failThreshold: 17 # should be overridden
      failureConditions:
        severityThresholds:
          any: 2
          critical: 3
          high: 4
          moderate: 5
          low: 6
          info: 7
        testCoverageThresholds:
          total: 8
          fresh: 9
      """.trimIndent())

    assertEquals(
      FailureConditions(
        severityThresholds = FailureConditions.SeverityThresholds(
          any = 2,
          critical = 3,
          high = 4,
          moderate = 5,
          low = 6,
          info = 7),
        testCoverageThresholds = FailureConditions.TestCoverageThresholds(
          total = 8,
          fresh = 9)),
      config.failureConditions
    )
  }

  @Test
  fun `yaml disableSanityInspections`() {
    val config = load("""
      version: "1.0"
      disableSanityInspections: true
      """.trimIndent())
    assertEquals(true, config.disableSanityInspections)
  }

  @Test
  fun `yaml runPromoInspections`() {
    val config = load("""
      version: "1.0"
      runPromoInspections: false
      """.trimIndent())
    assertEquals(false, config.runPromoInspections)
  }

  @Test
  fun `yaml default runPromoInspections`() {
    val config = load("""
      version: "1.0"
      """.trimIndent())
    assertEquals(null, config.runPromoInspections)
  }

  @Test
  fun `yaml for php-migration script`() {
    val config = load("""
      version: "1.0"
      script:
        parameters:
            fromLevel: 7.1
            toLevel: 8.0
    """.trimIndent())
    assertEquals("7.1", config.script.parameters.getValue("fromLevel").toString())
    assertEquals("8.0", config.script.parameters.getValue("toLevel").toString())
  }

  @Test
  fun `root properties should be exactly as described in schema`() {
    val schemaRoots = ObjectMapper()
      .readTree(javaClass.getResource(QODANA_YAML_SCHEMA_RESOURCE))
      .get("properties")
      .fieldNames()
      .asSequence()
      .sorted()
      .toList()
    val rootProps = QodanaYamlReader.rootProps.sorted()

    assertEquals(schemaRoots, rootProps)
  }

  private fun testProviderInspectionScopes(name: String,
                                           fieldSelector: (QodanaYamlConfig) -> InspectScopes,
                                           paths: List<String> = emptyList()) {
    val pathPart = if (paths.isNotEmpty()) {
      "paths:\n" + paths.joinToString(separator = "\n") { "            - $it" }
    }
    else ""
    val config = load("""
      version: "1.0"
      $name:
        - name: InspectionName
          $pathPart
    """.trimIndent())
    assertEquals(listOf(InspectScope("InspectionName", paths, emptyList())), fieldSelector.invoke(config))
  }

  private fun testProviderInspectionScopesWithTwoPath(name: String, fieldSelector: (QodanaYamlConfig) -> InspectScopes) =
    testProviderInspectionScopes(name, fieldSelector, listOf("./first path", "second-path"))


  private fun load(@Language("YAML") yaml: String): QodanaYamlConfig =
    QodanaYamlReader.parse(yaml).getOrThrow()
}
