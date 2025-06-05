package org.jetbrains.qodana.staticAnalysis.inspections.runner

import com.intellij.codeInspection.ex.InspectionProfileImpl
import com.intellij.openapi.util.Disposer
import org.jetbrains.qodana.registry.QodanaRegistry.SCOPE_EXTENDING_ENABLE_KEY
import org.jetbrains.qodana.staticAnalysis.QodanaTestCase
import org.jetbrains.qodana.staticAnalysis.scopes.InspectionToolScopeExtender
import org.jetbrains.qodana.staticAnalysis.scopes.QodanaScopeExtenderProvider
import org.jetbrains.qodana.staticAnalysis.script.scoped.SCOPED_SCRIPT_NAME
import org.jetbrains.qodana.staticAnalysis.testFramework.reinstantiateInspectionRelatedServices
import org.junit.Test

class QodanaScopedScriptConfigurationIntegrationTest : QodanaConfigurationIntegrationBaseTest() {
  public override fun setUp() {
    super.setUp()
    val initInspections = InspectionProfileImpl.INIT_INSPECTIONS
    InspectionProfileImpl.INIT_INSPECTIONS = true
    Disposer.register(testRootDisposable) {
      InspectionProfileImpl.INIT_INSPECTIONS = initInspections
    }
    reinstantiateInspectionRelatedServices(project, testRootDisposable)
    InspectionToolScopeExtender.EP_NAME.point.registerExtension(InspectionToolScopeExtenderMock, testRootDisposable)
    QodanaScopeExtenderProvider.EP_NAME.point.registerExtension(QodanaScopeExtenderProviderMock, testRootDisposable)
  }

  @Test
  fun `scoped with empty scope`() {
    val extendedScope = buildExtendedScope("""
      {
        "files" : []
      }
    """.trimIndent())
    assertEmpty(extendedScope)
  }

  @Test
  fun `scoped with extended scope`() {
    val extendedScope = buildExtendedScope("""
      {
        "files" : [ {
          "path" : "A.java",
          "added" : [ ],
          "deleted" : [ ]
        }]
      }
    """.trimIndent())
    assertDoesntContain(extendedScope, "A.java")
    assertContainsElements(extendedScope, "B.java")
    assertDoesntContain(extendedScope, "C.java")
  }

  @Test
  fun `scoped with extended scope by multi files`() {
    val extendedScope = buildExtendedScope("""
      {
        "files" : [{
            "path" : "A.java",
            "added" : [ ],
            "deleted" : [ ]
          },
          {
            "path" : "B.java",
            "added" : [ ],
            "deleted" : [ ]
          }
        ]
      }
    """.trimIndent())
    assertDoesntContain(extendedScope, "A.java")
    assertDoesntContain(extendedScope, "B.java")
    assertContainsElements(extendedScope, "C.java")
  }

  @Test
  fun `scoped with non referenced file`() {
    val extendedScope = buildExtendedScope("""
      {
        "files" : [{
            "path" : "C.java",
            "added" : [ ],
            "deleted" : [ ]
          }]
      }
    """.trimIndent())
    assertEmpty(extendedScope)
  }

  @Test
  fun `scoped with non extending inspection profile`() {
    val extendedScope = buildExtendedScope("""
      {
        "files" : [{
            "path" : "A.java",
            "added" : [ ],
            "deleted" : [ ]
          }]
      }
    """.trimIndent(), profileName = "qodana.single:unused")
    assertEmpty(extendedScope)
  }

  private fun buildExtendedScope(scopeFileText: String, profileName: String = "qodana.single:ConstantValue"): Collection<String> {
    val testProjectPath = project.basePath

    val qodanaYAML = """
      version: 1.0
      profile:
        name: $profileName
      script:
        name: $SCOPED_SCRIPT_NAME
        parameters:
          scope-file: scope
      runPromoInspections: false
      disableSanityInspections: true
    """.trimIndent()

    val projectFiles = listOf(
      "qodana.yaml" to qodanaYAML,
      "scope" to scopeFileText,
      "A.java" to "class A {}",
      "B.java" to "class B {}",
      "C.java" to "class C {}",
    )

    val cliArgs = listOf(
      "$testProjectPath",
      "$testProjectPath/out")

    val additionalFiles = mutableSetOf<String>()
    try {
      System.setProperty(SCOPE_EXTENDING_ENABLE_KEY, "true")
      QodanaTestCase.runTest {
        val script = buildScript(cliArgs, project, projectFiles, this)
        additionalFiles.addAll(script.runContext.getAdditionalFiles())
      }
    } finally {
      System.clearProperty(SCOPE_EXTENDING_ENABLE_KEY)
    }
    return additionalFiles
  }

  private fun QodanaRunContext.getAdditionalFiles() = when(this) {
    is QodanaRunIncrementalContext -> scopeExtended.keys.map { it.name }
    else -> emptyList()
  }
}
