package org.jetbrains.qodana.jvm.java.migrate


import com.intellij.openapi.application.invokeAndWaitIfNeeded
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.psi.PsiFile
import com.intellij.testFramework.TestLoggerFactory
import com.intellij.testFramework.rethrowLoggedErrorsIn
import org.assertj.core.api.Assertions.assertThat
import org.intellij.lang.annotations.Language
import org.jetbrains.qodana.jvm.java.migrate.MigrateClassesTestUtils.mapping
import org.jetbrains.qodana.jvm.java.migrate.MigrateClassesTestUtils.paramMap
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaProfileConfig
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaScriptConfig
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaRunnerTestCase
import org.junit.Test
import org.junit.jupiter.api.assertThrows

class MigrateClassesScriptTest : QodanaRunnerTestCase() {

  private lateinit var sourceFile: PsiFile

  @Language("java")
  private val original = """
    import org.junit.Test;
    import foo.Old;

    class A {}
  """.trimIndent()

  private fun setScriptParameters(params: Map<String, Any>) = updateQodanaConfig {
    it.copy(
      profile = QodanaProfileConfig(),
      script = QodanaScriptConfig(MigrateClassesScriptFactory.SCRIPT_NAME, params)
    )
  }

  override fun setUp() {
    super.setUp()
    sourceFile = invokeAndWaitIfNeeded { createFile("A.java", original) }
  }

  @Test
  fun `should fail to run when include cannot be resolved`() {
    setScriptParameters(paramMap(includeMapping = "this-does-not-exist"))
    rethrowLoggedErrorsIn {
      val e = assertThrows<TestLoggerFactory.TestLoggerAssertionError>(::runAnalysis)
      assertThat(e).hasMessage("Cannot find migration this-does-not-exist - Is the required plugin installed?")
    }
  }

  @Test
  fun `should succeed to run given existing include`() {
    setScriptParameters(paramMap(includeMapping = "JUnit (4.x -> 5.0)"))
    runAnalysis()

    assertMigrationResult("""
      import org.junit.jupiter.api.Test;
      import foo.Old;

      class A {}
    """.trimIndent())
  }

  @Test
  fun `should succeed to run given only custom mappings`() {
    setScriptParameters(paramMap(null, mapping("foo.Old", "foo.Next")))
    runAnalysis()

    assertMigrationResult("""
      import org.junit.Test;
      import foo.Next;

      class A {}
    """.trimIndent())
  }

  @Test
  fun `should succeed to run given include and custom mappings`() {
    setScriptParameters(paramMap("JUnit (4.x -> 5.0)", mapping("foo.Old", "foo.Next")))
    runAnalysis()

    assertMigrationResult("""
      import org.junit.jupiter.api.Test;
      import foo.Next;

      class A {}
    """.trimIndent())
  }

  private fun assertMigrationResult(@Language("java") fileContent: String) {
    assertThat(FileDocumentManager.getInstance().unsavedDocuments).isEmpty()
    assertThat(invokeAndWaitIfNeeded { sourceFile.text }).isEqualTo(fileContent)
  }
}
