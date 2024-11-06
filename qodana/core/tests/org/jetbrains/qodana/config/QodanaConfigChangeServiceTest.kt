package org.jetbrains.qodana.config

import com.intellij.codeInspection.ex.ProjectInspectionToolRegistrar
import com.intellij.openapi.application.ApplicationInfo
import com.intellij.openapi.application.invokeAndWaitIfNeeded
import com.intellij.openapi.application.writeAction
import com.intellij.openapi.vfs.readText
import com.intellij.profile.codeInspection.InspectionProfileLoadUtil
import com.intellij.profile.codeInspection.ProjectInspectionProfileManager
import com.intellij.psi.PsiDocumentManager
import com.intellij.testFramework.utils.editor.getVirtualFile
import com.intellij.testFramework.utils.editor.saveToDisk
import org.assertj.core.api.Assertions.assertThat
import org.intellij.lang.annotations.Language
import org.jetbrains.qodana.QodanaPluginHeavyTestBase
import org.jetbrains.qodana.dispatchAllTasksOnUi
import org.jetbrains.qodana.runDispatchingOnUi
import org.jetbrains.qodana.settings.ConfigExcludeItem
import org.jetbrains.qodana.settings.QodanaConfigChangeService
import kotlin.io.path.Path
import kotlin.io.path.pathString

class QodanaConfigChangeServiceTest : QodanaPluginHeavyTestBase() {
  private val configChangeService get() = QodanaConfigChangeService.getInstance(project)

  override fun getBasePath(): String = Path(super.getBasePath(), "QodanaConfigChangeServiceTest").pathString

  override fun setUp() {
    super.setUp()
    setUpProject()
  }

  private fun setUpProject() {
    invokeAndWaitIfNeeded {
      copyProjectTestData(getTestName(true).trim())
    }
  }

  private fun doCheck() = runDispatchingOnUi {
    dispatchAllTasksOnUi()
    val expectedQodanaYaml = myFixture.tempDirFixture.getFile("expected-qodana.yaml")!!

    val virtualFile = configChangeService.getConfigFile()
    assertNotNull(virtualFile)
    val psiFile = myFixture.psiManager.findFile(virtualFile!!)
    assertNotNull(psiFile)
    val document = PsiDocumentManager.getInstance(project).getDocument(psiFile!!)
    assertNotNull(document)
    writeAction {
      document!!.saveToDisk()
    }
    assertThat(document!!.getVirtualFile().readText().replace("\r", ""))
      .isEqualTo(expectedQodanaYaml.readText().replace("\r", "").updateVersion())
  }

  fun `test config don't exists`() = runDispatchingOnUi {
    doCheck()
  }

  fun `test config exclude don't exists`() = runDispatchingOnUi {
    doExclude(null, "")
    doCheck()
  }

  fun `test exclude inspection`() = runDispatchingOnUi {
    doExclude("ExcludedInspection", null)
    doCheck()
  }

  fun `test exclude multiple inspections`() = runDispatchingOnUi {
    listOf(
      "ExcludedInspection1",
      "ExcludedInspection2",
      "ExcludedInspection2"
    ).forEach {
      doExclude(it, null)
    }
    doCheck()
  }

  fun `test exclude paths`() = runDispatchingOnUi {
    listOf(
      "excluded/path1",
      "excluded/path2",
      "excluded/path1"
    ).forEach {
      doExclude(null, it)
    }
    doCheck()
  }

  fun `test exclude path for inspection`() = runDispatchingOnUi {
    doExclude("ExcludedInspection", "excluded/path/for/inspection")
    doCheck()
  }

  fun `test exclude inspection then path for inspection`() = runDispatchingOnUi {
    doExclude("ExcludedInspection", null)
    doExclude("ExcludedInspection", "excluded/path/for/inspection")
    doCheck()
  }

  fun `test exclude all paths then path`() = runDispatchingOnUi {
    doExclude(null, "")
    doExclude(null, "excluded/path")
    doCheck()
  }

  fun `test exclude all types`() = runDispatchingOnUi {
    doExclude(null, "excluded/path1")
    doExclude("ExcludedInspection1", null)
    doExclude("ExcludedInspection2", "excluded/path/for/inspection1")
    doExclude("ExcludedInspection3", "excluded/path/for/inspection2")
    doExclude("ExcludedInspection3", "excluded/path/for/inspection3")
    doExclude(null, "excluded/path2")
    doCheck()
  }

  fun `test exclude path for inspection then inspection`() = runDispatchingOnUi {
    doExclude("ExcludedInspection", "excluded/path/for/inspection1")
    doExclude("ExcludedInspection", "excluded/path/for/inspection2")
    doExclude("ExcludedInspection", null)
    doCheck()
  }

  fun `test exclude path then path for inspection then all paths`() = runDispatchingOnUi {
    doExclude(null, "excluded/path")
    doExclude("ExcludedInspection", "excluded/path/for/inspection")
    doExclude(null, "")
    doCheck()
  }

  fun `test default generated qodana yaml`() = runDispatchingOnUi {
    @Language("YAML")
    val expected = """
      #-------------------------------------------------------------------------------#
      #               Qodana analysis is configured by qodana.yaml file               #
      #             https://www.jetbrains.com/help/qodana/qodana-yaml.html            #
      #-------------------------------------------------------------------------------#
      version: "1.0"
      
      #Specify inspection profile for code analysis
      profile:
        name: qodana.starter
      
      #Enable inspections
      #include:
      #  - name: <SomeEnabledInspectionId>
      
      #Disable inspections
      #exclude:
      #  - name: <SomeDisabledInspectionId>
      #    paths:
      #      - <path/where/not/run/inspection>
      
      #Execute shell command before Qodana execution (Applied in CI/CD pipeline)
      #bootstrap: sh ./prepare-qodana.sh
      
      #Install IDE plugins before Qodana execution (Applied in CI/CD pipeline)
      #plugins:
      #  - id: <plugin.id> #(plugin id can be found at https://plugins.jetbrains.com)
      
      #Specify Qodana linter for analysis (Applied in CI/CD pipeline)
      linter: jetbrains/qodana-<linter>:LINTER_PLACEHOLDER
      
    """.trimIndent().updateVersion()
    val generated = configChangeService.createDefaultConfigContent()
    assertThat(generated).isEqualTo(expected)
  }

  fun `test default generated qodana yaml with custom project profile`() = runDispatchingOnUi {
    withProjectInspectionProfile("qodana.project.xml") {
      @Language("YAML")
      val expected = """
        #-------------------------------------------------------------------------------#
        #               Qodana analysis is configured by qodana.yaml file               #
        #             https://www.jetbrains.com/help/qodana/qodana-yaml.html            #
        #-------------------------------------------------------------------------------#
        version: "1.0"
        
        #Specify inspection profile for code analysis
        profile:
          name: qodana.project
        
        #Enable inspections
        #include:
        #  - name: <SomeEnabledInspectionId>
        
        #Disable inspections
        #exclude:
        #  - name: <SomeDisabledInspectionId>
        #    paths:
        #      - <path/where/not/run/inspection>
        
        #Execute shell command before Qodana execution (Applied in CI/CD pipeline)
        #bootstrap: sh ./prepare-qodana.sh
        
        #Install IDE plugins before Qodana execution (Applied in CI/CD pipeline)
        #plugins:
        #  - id: <plugin.id> #(plugin id can be found at https://plugins.jetbrains.com)
        
        #Specify Qodana linter for analysis (Applied in CI/CD pipeline)
        linter: jetbrains/qodana-<linter>:LINTER_PLACEHOLDER
        
      """.trimIndent().updateVersion()
      val generated = configChangeService.createDefaultConfigContent()
      assertThat(generated).isEqualTo(expected)
    }
  }

  fun `test default generated qodana yaml with not custom project profile`() = runDispatchingOnUi {
    withProjectInspectionProfile("qodana.project_default.xml") {
      @Language("YAML")
      val expected = """
        #-------------------------------------------------------------------------------#
        #               Qodana analysis is configured by qodana.yaml file               #
        #             https://www.jetbrains.com/help/qodana/qodana-yaml.html            #
        #-------------------------------------------------------------------------------#
        version: "1.0"
        
        #Specify inspection profile for code analysis
        profile:
          name: qodana.starter
        
        #Enable inspections
        #include:
        #  - name: <SomeEnabledInspectionId>
        
        #Disable inspections
        #exclude:
        #  - name: <SomeDisabledInspectionId>
        #    paths:
        #      - <path/where/not/run/inspection>
        
        #Execute shell command before Qodana execution (Applied in CI/CD pipeline)
        #bootstrap: sh ./prepare-qodana.sh
        
        #Install IDE plugins before Qodana execution (Applied in CI/CD pipeline)
        #plugins:
        #  - id: <plugin.id> #(plugin id can be found at https://plugins.jetbrains.com)
        
        #Specify Qodana linter for analysis (Applied in CI/CD pipeline)
        linter: jetbrains/qodana-<linter>:LINTER_PLACEHOLDER
        
      """.trimIndent().updateVersion()
      val generated = configChangeService.createDefaultConfigContent()
      assertThat(generated).isEqualTo(expected)
    }
  }

  private suspend fun doExclude(inspectionId: String?, path: String?) {
    configChangeService.excludeData(ConfigExcludeItem(inspectionId, path))
    dispatchAllTasksOnUi()
  }

  private suspend fun withProjectInspectionProfile(filename: String, action: suspend () -> Unit) {
    val profileManager = ProjectInspectionProfileManager.getInstance(project)
    val profile = InspectionProfileLoadUtil.load(
      myFixture.tempDirFixture.getFile(".idea/inspectionProfiles/$filename")!!.toNioPath(),
      ProjectInspectionToolRegistrar.getInstance(project),
      profileManager
    )
    profile.isProjectLevel = true
    try {
      profileManager.addProfile(profile)
      profileManager.setCurrentProfile(profile)
      action.invoke()
    } finally {
      profileManager.setCurrentProfile(null)
      profileManager.deleteProfile(profile)
    }
  }

  private fun String.updateVersion(): String {
    val ideMajorVersion = ApplicationInfo.getInstance().majorVersion
    val ideMinorVersion = ApplicationInfo.getInstance().minorVersionMainPart
    return this.replace("LINTER_PLACEHOLDER","${ideMajorVersion}.${ideMinorVersion}")
  }
}