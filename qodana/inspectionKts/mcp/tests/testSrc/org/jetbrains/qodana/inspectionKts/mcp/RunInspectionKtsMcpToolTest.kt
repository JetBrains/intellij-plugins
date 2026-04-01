package org.jetbrains.qodana.inspectionKts.mcp

import com.intellij.mcpserver.McpToolsetTestBase
import com.intellij.testFramework.junit5.fixture.moduleFixture
import com.intellij.testFramework.junit5.fixture.sourceRootFixture
import com.intellij.testFramework.junit5.fixture.virtualFileFixture
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertContains
import kotlin.test.assertTrue

class RunInspectionKtsMcpToolTest : McpToolsetTestBase() {

  private val simpleJavaCode = """
    public class Example {
      public void test() {
        String unused = "hello";
      }
    }
  """.trimIndent()

  private val moduleFixture = projectFixture.moduleFixture("testModule")
  private val sourceRootFixture = moduleFixture.sourceRootFixture()

  @Suppress("unused")
  private val exampleFile = sourceRootFixture.virtualFileFixture("Example.java", simpleJavaCode)

  @BeforeEach
  fun initSourceRootFixture() {
    sourceRootFixture.get()
  }

  private val simpleInspectionKts = """
    import com.intellij.psi.*

    val inspection = localInspection { psiFile, inspection ->
        psiFile.descendantsOfType<PsiLocalVariable>().forEach { variable ->
            if (variable.name?.startsWith("unused") == true) {
                inspection.registerProblem(variable, "Variable starts with 'unused'")
            }
        }
    }
    
    
    listOf(
        InspectionKts(
            id = "TestInspection",
            localTool = inspection,
            name = "TestInspection",
            htmlDescription = "",
            level = HighlightDisplayLevel.WARNING,
        )
    )
  """.trimIndent()

  @Test
  fun `test run_inspection_kts with provided content`() = runBlocking {
    testMcpTool(
      toolName = "run_inspection_kts",
      input = buildJsonObject {
        put("inspectionKtsCode", JsonPrimitive(simpleInspectionKts))
        put("contextPath", JsonPrimitive("src/Example.java"))
        put("targetFileContent", JsonPrimitive(simpleJavaCode))
      }
    ) { result ->
      val text = result.textContent.text
      assertTrue(text.contains("Variable starts with 'unused'"))
    }
  }

  @Test
  fun `test run_inspection_kts without content reads from project`() = runBlocking {

    testMcpTool(
      toolName = "run_inspection_kts",
      input = buildJsonObject {
        put("inspectionKtsCode", JsonPrimitive(simpleInspectionKts))
        put("contextPath", JsonPrimitive("src/Example.java"))
        put("targetFileContent", JsonNull)
      }
    ) { result ->
      val text = result.textContent.text
      assertTrue(text.contains("Variable starts with 'unused'"))
    }
  }

  @Test
  fun `test run_inspection_kts file not found error`() = runBlocking {
    testMcpTool(
      toolName = "run_inspection_kts",
      input = buildJsonObject {
        put("inspectionKtsCode", JsonPrimitive(simpleInspectionKts))
        put("contextPath", JsonPrimitive("src/NonExistent.java"))
        put("targetFileContent", JsonNull)
      }
    ) { result ->
      val text = result.textContent.text
      // Should report file not found
      assertContains(text, "No PSI file found", ignoreCase = true,
        message = "Result should indicate file not found: $text")
    }
  }
}
