package org.jetbrains.qodana.jvm.java.migrate

import com.intellij.refactoring.migration.MigrationMapEntry
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.qodana.jvm.java.migrate.MigrateClassesTestUtils.mapping
import org.jetbrains.qodana.jvm.java.migrate.MigrateClassesTestUtils.paramMap
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaException
import org.jetbrains.qodana.staticAnalysis.script.UnvalidatedParameters
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows


class MigrationParametersTest {

  @Test
  fun `should fail to parse when both include-mapping and mapping is absent`() {
    val cases = listOf(
      params(paramMap(includeMapping = null)),
      params(paramMap(includeMapping = "")),
      params(paramMap(includeMapping = " ")),
    )

    assertThat(cases).allSatisfy { case ->
      assertFailsWith("Require either 'include-mapping' or a non-empty list 'mapping'") {
        MigrationParameters.fromParameters(case)
      }
    }
  }

  @Test
  fun `should fail to parse invalid mapping entries`() {
    assertFailsWith("Missing required mapping property 'old-name'") {
      val missingProp = mapping(newName = "new-name")
      MigrationParameters.fromParameters(params(paramMap(null, missingProp)))
    }

    assertFailsWith("Unknown mapping type foo - only 'class' or 'package' are applicable") {
      val wrongType = mapping("oldName", "newName", type = "foo")

      MigrationParameters.fromParameters(params(paramMap(null, wrongType)))
    }
  }

  @Test
  fun `should succeed to parse only include-mapping`() {
    val included = "expected-mapping"
    val actual = MigrationParameters.fromParameters(params(paramMap(included)))

    assertThat(actual.included).isEqualTo(included)
    assertThat(actual.mappings).isEmpty()
  }

  @Test
  fun `should correctly parse custom mapping`() {
    val params = params(
      paramMap(
        null,
        mapping("old", "new", type = "class"),
        mapping("old", "new", type = "package", recursive = "true")
      )
    )

    val actual = MigrationParameters.fromParameters(params)

    assertThat(actual.included).isNull()
    assertThat(actual.mappings).allSatisfy {
      assertThat(it.newName).isEqualTo("new")
      assertThat(it.oldName).isEqualTo("old")
    }
    val (first, last) = actual.mappings
    assertThat(first.type).isEqualTo(MigrationMapEntry.CLASS)
    assertThat(last.type).isEqualTo(MigrationMapEntry.PACKAGE)
    assertThat(last.isRecursive).isTrue()
  }

  private inline fun assertFailsWith(message: String, f: () -> Unit) {
    val e = assertThrows<QodanaException>(f)
    assertThat(e).hasMessage(message)
  }

  private fun params(paramMap: Map<String, Any>) =
    UnvalidatedParameters("ignored", paramMap)
}