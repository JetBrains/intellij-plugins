package org.jetbrains.qodana.staticAnalysis.profile

import com.intellij.configurationStore.LISTEN_SCHEME_VFS_CHANGES_IN_TEST_MODE
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.JDOMUtil
import com.intellij.profile.codeInspection.PROFILE_DIR
import com.intellij.profile.codeInspection.PROJECT_DEFAULT_PROFILE_NAME
import com.intellij.profile.codeInspection.ProjectInspectionProfileManager
import com.intellij.project.stateStore
import com.intellij.testFramework.*
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.qodana.staticAnalysis.profile.providers.QodanaEmbeddedProfile
import org.jetbrains.qodana.staticAnalysis.profile.providers.QodanaEmbeddedProfile.*
import org.jetbrains.qodana.staticAnalysis.profile.providers.QodanaEmbeddedProfilesProvider
import org.junit.Assert.assertEquals
import org.junit.ClassRule
import org.junit.Rule
import org.junit.Test
import java.nio.file.Paths
import kotlin.io.path.readText

class QodanaEmbeddedProfilesProviderTest {
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

  private fun doTest(task: suspend (Project) -> Unit) {
    runBlocking {
      loadAndUseProjectInLoadComponentStateMode(tempDirManager, { Paths.get(it.path) }, task)
    }
  }

  private val embeddedProfilesProvider = QodanaEmbeddedProfilesProvider()

  @Test
  fun testRecommended() {
    doTest { project ->
      val recommended = embeddedProfilesProvider.provideProfile(QODANA_RECOMMENDED.profileName, project)!!
      assertEquals(recommended.name, QODANA_RECOMMENDED.profileName)
      assertThat(recommended.getAllEnabledInspectionTools(project)).isNotEmpty
    }
  }

  @Test
  fun testStarter() {
    doTest { project ->
      val starter = embeddedProfilesProvider.provideProfile(QODANA_STARTER.profileName, project)!!
      assertEquals(QODANA_STARTER.profileName, starter.name)
      assertThat(starter.getAllEnabledInspectionTools(project)).isNotEmpty
    }
  }

  @Test
  fun testStarterInheritsProjectDefault() {
    doTest { project ->
      project.putUserData(LISTEN_SCHEME_VFS_CHANGES_IN_TEST_MODE, true)

      val projectInspectionProfileManager = ProjectInspectionProfileManager.getInstance(project)
      projectInspectionProfileManager.forceLoadSchemes()

      // set isProjectLevel = true
      projectInspectionProfileManager.currentProfile
      val currentProfile = projectInspectionProfileManager.getProfile(PROJECT_DEFAULT_PROFILE_NAME)
      assertThat(currentProfile.isProjectLevel).isTrue()
      currentProfile.setToolEnabled("ConstantValue", false)

      project.stateStore.save()

      val inspectionDir = project.stateStore.directoryStorePath!!.resolve(PROFILE_DIR)
      val file = inspectionDir.resolve("profiles_settings.xml")

      assertThat(file).doesNotExist()
      val profileFile = inspectionDir.resolve("Project_Default.xml")
      assertThat(profileFile.readText()).isEqualTo("""
      <component name="InspectionProjectProfileManager">
        <profile version="1.0">
          <option name="myName" value="Project Default" />
          <inspection_tool class="ConstantValue" enabled="false" level="WARNING" enabled_by_default="false" />
        </profile>
      </component>""".trimIndent())


      val profileManager = QodanaProjectInspectionProfileManager.getInstance(project)

      val defaultProfile = profileManager.getProfile(PROJECT_DEFAULT_PROFILE_NAME)
      assertThat(defaultProfile.getTools("ConstantValue", project).isEnabled).isEqualTo(false)

      val starter = embeddedProfilesProvider.provideProfile(QODANA_STARTER.profileName, project)!!
      assertThat(starter.getTools("ConstantValue", project).isEnabled).isEqualTo(false)
    }
  }

  @Test
  fun `testStarter is more narrow than recommended `() {
    doTest { project ->
      val recommended = embeddedProfilesProvider.provideProfile(QODANA_RECOMMENDED.profileName, project)!!
      val starter = embeddedProfilesProvider.provideProfile(QODANA_STARTER.profileName, project)!!

      val enabledInRecommended = recommended.getAllEnabledInspectionTools(project).map { it.shortName }.toSet()
      val enabledInStarter = starter.getAllEnabledInspectionTools(project).map { it.shortName }.toSet()

      UsefulTestCase.assertEmpty(enabledInStarter - enabledInRecommended)
    }
  }

  private fun doProfileValidationTest(
    embeddedProfile: QodanaEmbeddedProfile,
    requestedProfileName: String,
    expectedProfileXmlName: String
  ) {
    doTest { project ->
      assertThat(embeddedProfile.matchesName(requestedProfileName)).isTrue

      val providedProfile = embeddedProfilesProvider.provideProfile(requestedProfileName, project)!!

      assertThat(providedProfile.name).isEqualTo(embeddedProfile.profileName)
      assertThat(providedProfile.tools).isNotEmpty

      val element = JDOMUtil.load(embeddedProfilesProvider.getInputStreamForProfile(embeddedProfile))

      val profilesElements = element.getChildren("profile")
      assertThat(profilesElements.size).isOne

      val profileElement = profilesElements.first()
      val optionsElements = profileElement.getChildren("option")
      assertThat(optionsElements.size).isOne

      val optionElement = optionsElements.first()
      assertThat(optionElement.getAttributeValue("name")).isEqualTo("myName")
      assertThat(optionElement.getAttributeValue("value")).isEqualTo(expectedProfileXmlName)

      val inspectionToolsElements = profileElement.getChildren("inspection_tool")
      assertThat(inspectionToolsElements).isNotEmpty
    }
  }
}
