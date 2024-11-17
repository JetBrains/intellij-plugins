package org.jetbrains.qodana.yaml

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ex.LocalInspectionToolWrapper
import com.intellij.openapi.Disposable
import com.intellij.openapi.extensions.LoadingOrder
import com.intellij.openapi.project.Project
import org.jetbrains.qodana.staticAnalysis.testFramework.reinstantiateInspectionRelatedServices
import org.jetbrains.qodana.staticAnalysis.profile.QodanaInspectionProfile
import org.jetbrains.qodana.staticAnalysis.profile.QodanaInspectionProfileManager
import org.jetbrains.qodana.staticAnalysis.profile.QodanaInspectionProfileProvider

internal data class MockInspectionDescriptor(val shortName: String, val name: String, val group: String)
internal data class ProfileDescriptor(val name: String, val inspections: List<MockInspectionDescriptor>)

internal val PROFILE_EMPTY = ProfileDescriptor("test.empty", listOf())

internal val PROFILE_SANITY = ProfileDescriptor("test.qodana.sanity", listOf(
  MockInspectionDescriptor("Sanity1", "Sanity First", "Group/Sanity"),
  MockInspectionDescriptor("Sanity2", "Sanity Second", "Group/AnotherSanity"),
  MockInspectionDescriptor("Sanity3", "Sanity Third", "Group/Kotlin"),
  MockInspectionDescriptor("Sanity4", "Sanity Forth", "Group/Java"),
  MockInspectionDescriptor("Sanity5", "Sanity Fifth", "Group/Maldives"),
))

internal val PROFILE_STARTER = ProfileDescriptor("test.qodana.starter", PROFILE_SANITY.inspections + listOf(
  MockInspectionDescriptor("StartFromMeOne", "Starter First", "Kotlin/Start"),
  MockInspectionDescriptor("StartFromMeTwo", "Starter Two", "Java/Start"),
  MockInspectionDescriptor("StartFromMeThree", "Starter Three", "Maldives/Start")
))

internal val PROFILE_RECOMMENDED = ProfileDescriptor("test.qodana.recommended", PROFILE_STARTER.inspections + (0 until 50).map {
  MockInspectionDescriptor("Recommended$it", "Recommended $it-th", "Groups/Some/Group$it")
})

internal val ALL_PROFILES: List<ProfileDescriptor> = listOf(PROFILE_EMPTY, PROFILE_SANITY, PROFILE_STARTER, PROFILE_RECOMMENDED)

internal fun setupMockProfiles(project: Project, testRootDisposable: Disposable) {
  System.setProperty("qodana.default.profile", "test.qodana.recommended")
  reinstantiateInspectionRelatedServices(project, testRootDisposable)

  val profileManager = QodanaInspectionProfileManager.getInstance(project)
  val profilesMap = ALL_PROFILES
    .associate { (name, inspections) ->
      val profile = QodanaInspectionProfile(name, profileManager, null)
      inspections.forEach { (shortName, name, group) ->
        val tool = object : LocalInspectionTool() {
          override fun getID(): String = shortName
          override fun getShortName(): String = shortName
          override fun getDisplayName(): String = name
          override fun getGroupPath(): Array<String> = group.split("/").toTypedArray()
          override fun isEnabledByDefault(): Boolean = true
        }
        profile.addTool(project, LocalInspectionToolWrapper(tool), emptyMap())
      }
      name to profile
    }

  QodanaInspectionProfileProvider.EP_NAME.point.registerExtension(
    object : QodanaInspectionProfileProvider {
      override fun provideProfile(profileName: String, project: Project?): QodanaInspectionProfile {
        println("requested name: $profileName | allNames: ${profilesMap.keys}")
        if (profileName == "Default") return profilesMap[PROFILE_RECOMMENDED.name]!!
        if (profileName == "empty") return profilesMap[PROFILE_EMPTY.name]!!
        return profilesMap[profileName]!!
      }

      override fun getAllProfileNames(project: Project?): MutableList<String> = profilesMap.keys.toMutableList()
    },
    LoadingOrder.FIRST,
    testRootDisposable
  )
}