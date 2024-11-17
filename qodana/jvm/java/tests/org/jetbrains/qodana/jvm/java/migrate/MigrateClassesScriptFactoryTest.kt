package org.jetbrains.qodana.jvm.java.migrate

import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaException
import org.jetbrains.qodana.staticAnalysis.script.UnvalidatedParameters
import org.junit.Test
import org.junit.jupiter.api.assertThrows

class MigrateClassesScriptFactoryTest {
  private val subject = MigrateClassesScriptFactory()

  @Test
  fun `parse empty parameters should fail`() {
    val ex = assertThrows<QodanaException> { subject.parseParameters("") }
    assertThat(ex).hasMessage(
      "CLI parameter for migrate-classes must be passed as '--script migrate-classes:%migrationName%'. " +
      "For example '--script migrate-classes:Java EE to Jakarta EE'."
    )
  }


  @Test
  fun `parse non-empty parameters should succeed`() {
    val arg = "JUnit (4.x -> 5.0)"
    val params = UnvalidatedParameters(MigrateClassesScriptFactory.SCRIPT_NAME, subject.parseParameters(arg))
    val actual = MigrationParameters.fromParameters(params)

    assertThat(actual.included).isEqualTo(arg)
  }
}
