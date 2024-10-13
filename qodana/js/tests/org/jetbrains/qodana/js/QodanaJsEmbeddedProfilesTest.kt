package org.jetbrains.qodana.js

import com.intellij.openapi.project.Project
import com.intellij.testFramework.ApplicationRule
import com.intellij.testFramework.InitInspectionRule
import com.intellij.testFramework.TemporaryDirectory
import com.intellij.testFramework.loadAndUseProjectInLoadComponentStateMode
import com.intellij.util.PlatformUtils
import kotlinx.coroutines.runBlocking
import org.jetbrains.qodana.staticAnalysis.profile.providers.QodanaEmbeddedProfile.QODANA_RECOMMENDED
import org.jetbrains.qodana.staticAnalysis.profile.providers.QodanaEmbeddedProfile.QODANA_STARTER
import org.jetbrains.qodana.staticAnalysis.profile.providers.QodanaEmbeddedProfilesProvider
import org.junit.Assert.*
import org.junit.ClassRule
import org.junit.Rule
import org.junit.Test
import java.nio.file.Paths

class QodanaJsEmbeddedProfilesTest {
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
  fun testRecommendedOtherLinters() {
    doTest { project ->
      val recommended = embeddedProfilesProvider.provideProfile(QODANA_RECOMMENDED.profileName, project)!!
      assertEquals(recommended.name, QODANA_RECOMMENDED.profileName)
      assertFalse(recommended.getTools ("JsCoverageInspection", project).isEnabled)
      assertFalse(recommended.getTools ("ExceptionCaughtLocallyJS", project).isEnabled)
    }
  }

  @Test
  fun testRecommended() {
    val oldPrefix = System.getProperty(PlatformUtils.PLATFORM_PREFIX_KEY, PlatformUtils.IDEA_PREFIX)
    try {
      System.setProperty(PlatformUtils.PLATFORM_PREFIX_KEY, PlatformUtils.WEB_PREFIX)
      doTest { project ->
        val recommended = embeddedProfilesProvider.provideProfile(QODANA_RECOMMENDED.profileName, project)!!
        assertEquals(recommended.name, QODANA_RECOMMENDED.profileName)
        assertTrue(recommended.getTools ("JsCoverageInspection", project).isEnabled)
        assertTrue(recommended.getTools ("ExceptionCaughtLocallyJS", project).isEnabled)
      }
    }
    finally {
      System.setProperty(PlatformUtils.PLATFORM_PREFIX_KEY, oldPrefix)
    }
  }

  @Test
  fun testStarterOtherLinters() {
    doTest { project ->
      val recommended = embeddedProfilesProvider.provideProfile(QODANA_STARTER.profileName, project)!!
      assertEquals(recommended.name, QODANA_STARTER.profileName)
      assertFalse(recommended.getTools ("JsCoverageInspection", project).isEnabled)
      assertFalse(recommended.getTools ("ExceptionCaughtLocallyJS", project).isEnabled)
    }
  }

  @Test
  fun testStarter() {
    val oldPrefix = System.getProperty(PlatformUtils.PLATFORM_PREFIX_KEY, PlatformUtils.IDEA_PREFIX)
    try {
      System.setProperty(PlatformUtils.PLATFORM_PREFIX_KEY, PlatformUtils.WEB_PREFIX)
      doTest { project ->
        val recommended = embeddedProfilesProvider.provideProfile(QODANA_STARTER.profileName, project)!!
        assertEquals(recommended.name, QODANA_STARTER.profileName)
        assertTrue(recommended.getTools ("JsCoverageInspection", project).isEnabled)
        assertTrue(recommended.getTools ("ExceptionCaughtLocallyJS", project).isEnabled)
      }
    }
    finally {
      System.setProperty(PlatformUtils.PLATFORM_PREFIX_KEY, oldPrefix)
    }
  }
}