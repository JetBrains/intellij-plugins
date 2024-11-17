package org.jetbrains.qodana.staticAnalysis

import com.intellij.codeInspection.ex.InspectionProfileImpl
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.util.JDOMUtil
import com.intellij.testFramework.LightPlatformTestCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.runBlocking
import org.intellij.lang.annotations.Language
import org.jetbrains.qodana.license.QodanaLicenseType
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaConfig
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaYamlConfig
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaYamlReader
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaMessageReporter
import org.jetbrains.qodana.staticAnalysis.inspections.runner.startup.LoadedProfile
import org.jetbrains.qodana.staticAnalysis.inspections.runner.startup.PreconfiguredRunContextFactory
import org.jetbrains.qodana.staticAnalysis.profile.MainInspectionGroup
import org.jetbrains.qodana.staticAnalysis.profile.QodanaInspectionProfile
import org.jetbrains.qodana.staticAnalysis.profile.QodanaInspectionProfileManager
import org.jetbrains.qodana.staticAnalysis.profile.QodanaProfile
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.nio.file.Paths
import kotlin.io.path.Path

@RunWith(JUnit4::class)
abstract class QodanaTestCase : LightPlatformTestCase() {
  companion object {
    fun runTest(block: suspend CoroutineScope.() -> Unit) {
      runBlocking {
        block(this)
        this.coroutineContext.cancelChildren()
      }
    }
  }
  override fun setUp() {
    super.setUp()
    InspectionProfileImpl.INIT_INSPECTIONS = true
  }

  override fun tearDown() {
    try {
      super.tearDown()
    }
    finally {
      InspectionProfileImpl.INIT_INSPECTIONS = false
    }
  }

  fun constructProfile(
    config: QodanaConfig,
    loadedProfile: LoadedProfile? = null // workaround because load call suspends
  ): QodanaProfile = runBlocking {
    val loaded = loadedProfile ?: LoadedProfile.load(config, project, QodanaMessageReporter.DEFAULT)
    val factory = PreconfiguredRunContextFactory(
      config,
      QodanaMessageReporter.DEFAULT,
      project,
      loaded,
      this
    )

    factory.openRunContext().qodanaProfile
  }

  fun writeProfile(profile: InspectionProfileImpl): String {
    return JDOMUtil.writeElement(profile.writeScheme())
  }

  fun qodanaConfig(@Language("YAML") config: String = ""): QodanaConfig {
    val yaml = QodanaYamlReader.parse(config)
      .getOrThrow()
    return qodanaConfig(yaml)
  }

  fun qodanaConfig(yaml: QodanaYamlConfig): QodanaConfig {
    return QodanaConfig.fromYaml(projectPath(), Paths.get("unused"), yaml = yaml)
  }

  protected inline fun qodanaConfig(cfg: QodanaConfig.() -> QodanaConfig) =
    qodanaConfig(QodanaYamlConfig.EMPTY_V1).cfg()

  protected fun projectPath() = Path(project.guessProjectDir()?.path ?: project.basePath ?: "")

  fun parseXmlProfile(@Language("XML") xml: String): QodanaInspectionProfile {
    val element = JDOMUtil.load(xml)
    val inspectionProfile = QodanaInspectionProfile.newFromXml(element, name = null, profileManager = QodanaInspectionProfileManager.getInstance(project))
    inspectionProfile.lockProfile(true)

    val qodanaProfile = QodanaProfile(MainInspectionGroup(inspectionProfile), emptyList(), project, QodanaLicenseType.ULTIMATE_PLUS)
    return qodanaProfile.effectiveProfile.apply { name = "foo" }
  }
}
