package org.jetbrains.qodana.jvm.java

import com.intellij.codeInspection.ex.ApplicationInspectionProfileManager
import com.intellij.codeInspection.nullable.NullableStuffInspection
import com.intellij.openapi.project.Project
import com.intellij.testFramework.ApplicationRule
import com.intellij.testFramework.InitInspectionRule
import com.intellij.testFramework.TemporaryDirectory
import com.intellij.testFramework.loadAndUseProjectInLoadComponentStateMode
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.runBlocking
import org.jetbrains.qodana.staticAnalysis.profile.providers.QodanaEmbeddedProfile
import org.jetbrains.qodana.staticAnalysis.profile.providers.QodanaEmbeddedProfilesProvider
import org.junit.Before
import org.junit.ClassRule
import org.junit.Rule
import org.junit.Test
import java.nio.file.Paths

class QodanaRecommendedProfileTest {
  companion object {
    @JvmField
    @ClassRule
    val appRule = ApplicationRule()
  }

  @Rule
  @JvmField
  val tempDirManager = TemporaryDirectory()

  @Rule
  @JvmField
  val initInspectionRule = InitInspectionRule()

  /**
   * Forces initialization of [ApplicationInspectionProfileManager], which is a lazy service .
   * Its [init] block calls [com.intellij.codeInspection.ex.ApplicationInspectionProfileManager.syncProvidedSeverities],
   * registering plugin-provided [com.intellij.codeHighlighting.HighlightDisplayLevel] entries
   * (e.g., SYNTAX_UPDATE from Go, STYLE_SUGGESTION from Grazi) into [com.intellij.codeHighlighting.HighlightDisplayLevel.LEVEL_MAP].
   * Without this, [com.intellij.codeInspection.InspectionEP.getDefaultLevel]
   * throws [com.intellij.testFramework.TestLoggerFactory.TestLoggerAssertionError] for unknown severity names
   * when [com.intellij.codeInspection.ex.InspectionProfileImpl.initInspectionTools] processes inspections
   * during [com.intellij.codeInspection.inspectionProfile.YamlInspectionProfileImpl.buildEffectiveProfile].
   */
  @Before
  fun setUp() {
    ApplicationInspectionProfileManager.getInstanceImpl()
  }

  private val embeddedProfilesProvider = QodanaEmbeddedProfilesProvider()

  private fun doTest(task: suspend (Project) -> Unit) {
    runBlocking {
      loadAndUseProjectInLoadComponentStateMode(tempDirManager, { Paths.get(it.path) }, task)
    }
  }

  @Test
  fun testNullableProblems() {
    doTest { project ->
      val recommended = embeddedProfilesProvider.provideProfile(QodanaEmbeddedProfile.QODANA_RECOMMENDED.profileName, project)!!
      val tools = recommended.getTools("NullableProblems", project)
      val scopeToolStates = tools.tools
      scopeToolStates.forEach {
        assertTrue((it.tool.tool as NullableStuffInspection).IGNORE_EXTERNAL_SUPER_NOTNULL)
      }
    }
  }
}