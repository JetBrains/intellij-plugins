package org.jetbrains.qodana.settings

import com.intellij.openapi.application.ApplicationInfo
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.util.JDOMUtil
import com.intellij.openapi.vcs.changes.ChangeListManager
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.profile.codeInspection.InspectionProfileLoadUtil
import com.intellij.profile.codeInspection.PROFILE_DIR
import com.intellij.profile.codeInspection.PROJECT_DEFAULT_PROFILE_NAME
import com.intellij.profile.codeInspection.ProjectInspectionProfileManager
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.runInterruptible
import kotlinx.coroutines.withContext
import org.intellij.lang.annotations.Language
import org.jdom.JDOMException
import org.jetbrains.qodana.coroutines.QodanaDispatchers
import org.jetbrains.qodana.staticAnalysis.profile.providers.QodanaEmbeddedProfile
import org.jetbrains.qodana.ui.getQodanaImageNameMatchingIDE
import java.io.IOException
import java.nio.file.Path
import kotlin.io.path.Path

const val APPLIED_IN_CI_COMMENT = "(Applied in CI/CD pipeline)"

class QodanaYamlHeaderItemProvider : QodanaYamlItemProvider {
  companion object {
    const val ID = "header"
  }

  override suspend fun provide(project: Project): QodanaYamlItem {
    @Language("YAML")
    val content = """
      #-------------------------------------------------------------------------------#
      #               Qodana analysis is configured by qodana.yaml file               #
      #             https://www.jetbrains.com/help/qodana/qodana-yaml.html            #
      #-------------------------------------------------------------------------------#
    """.trimIndent()
    return QodanaYamlItem(ID, -100, content)
  }
}

class QodanaYamlVersionItemProvider : QodanaYamlItemProvider {
  companion object {
    const val ID = "version"
  }

  override suspend fun provide(project: Project): QodanaYamlItem {
    @Language("YAML")
    val content = "version: \"1.0\""
    return QodanaYamlItem(ID, 0, content)
  }
}

class QodanaYamlProfileItemProvider : QodanaYamlItemProvider {
  companion object {
    const val ID: String = "profile"
  }

  override suspend fun provide(project: Project): QodanaYamlItem {
    val profileName = getProjectCustomProfileName(project) ?: QodanaEmbeddedProfile.QODANA_STARTER.profileName
    @Language("YAML")
    val content = """
      
      #Specify inspection profile for code analysis
      profile:
        name: $profileName
    """.trimIndent()
    return QodanaYamlItem(ID, 20, content)
  }

  private suspend fun getProjectCustomProfileName(project: Project): String? {
    val currentProfile = withContext(QodanaDispatchers.Default) {
      ProjectInspectionProfileManager.getInstance(project).currentProfile
    }
    if (!currentProfile.isProjectLevel) return null

    val currentProfileName = currentProfile.name
    if (currentProfileName == PROJECT_DEFAULT_PROFILE_NAME) return null

    val allProjectProfileFiles = allProjectProfileFiles(project)

    val currentProfilePath = allProjectProfileFiles.map { path ->
      flow {
        val profileName = getProfileNameFromXmlProfile(path)
        if (profileName == currentProfileName) emit(path)
      }
    }.merge().firstOrNull() ?: return null
    val virtualFile = LocalFileSystem.getInstance().findFileByIoFile(currentProfilePath.toFile()) ?: return null

    val changeListManager = ChangeListManager.getInstance(project)
    if (changeListManager.isUnversioned(virtualFile) || changeListManager.isIgnoredFile(virtualFile)) return null
    return currentProfileName
  }

  private suspend fun allProjectProfileFiles(project: Project): List<Path> {
    val projectPath = project.guessProjectDir()?.toNioPath() ?: project.basePath?.let { Path(it) } ?: return emptyList()
    val profilesDir = projectPath.resolve("${Project.DIRECTORY_STORE_FOLDER}/$PROFILE_DIR")
    return withContext(QodanaDispatchers.IO) {
      profilesDir.toFile()
        .let { it.listFiles { pathname -> pathname?.isFile == true && pathname.extension == "xml" }?.toList() ?: emptyList() }
        .map { it.toPath() }
    }
  }

  private suspend fun getProfileNameFromXmlProfile(path: Path): String? {
    val element = runInterruptible(QodanaDispatchers.IO) {
      try {
        JDOMUtil.load(path)
      }
      catch (_ : IOException) {
        null
      }
      catch (_ : JDOMException) {
        null
      }
    } ?: return null
    return InspectionProfileLoadUtil.getProfileName(path, element)
  }
}

class QodanaYamlIncludeItemProvider : QodanaYamlItemProvider {
  companion object {
    const val ID = "include"
  }

  override suspend fun provide(project: Project): QodanaYamlItem {
    @Language("YAML")
    val content = """
      
      #Enable inspections
      #include:
      #  - name: <SomeEnabledInspectionId>
    """.trimIndent()
    return QodanaYamlItem(ID, 50, content)
  }
}

class QodanaYamlExcludeItemProvider : QodanaYamlItemProvider {
  companion object {
    const val ID = "exclude"
  }

  override suspend fun provide(project: Project): QodanaYamlItem {
    @Language("YAML")
    val content = """
      
      #Disable inspections
      #exclude:
      #  - name: <SomeDisabledInspectionId>
      #    paths:
      #      - <path/where/not/run/inspection>
    """.trimIndent()
    return QodanaYamlItem(ID, 80, content)
  }
}

class QodanaYamlBootstrapItemProvider : QodanaYamlItemProvider {
  companion object {
    const val ID = "bootstrap"
  }

  override suspend fun provide(project: Project): QodanaYamlItem {
    @Language("YAML")
    val content = """
      
      #Execute shell command before Qodana execution $APPLIED_IN_CI_COMMENT
      #bootstrap: sh ./prepare-qodana.sh
    """.trimIndent()
    return QodanaYamlItem(ID, 150, content)
  }
}

class QodanaYamlPluginItemProvider : QodanaYamlItemProvider {
  companion object {
    const val ID = "plugins"
  }

  override suspend fun provide(project: Project): QodanaYamlItem {
    @Language("YAML")
    val content = """
      
      #Install IDE plugins before Qodana execution $APPLIED_IN_CI_COMMENT
      #plugins:
      #  - id: <plugin.id> #(plugin id can be found at https://plugins.jetbrains.com)
    """.trimIndent()
    return QodanaYamlItem(ID, 150, content)
  }
}

// TODO – move this provider to the top when QD-5820 fixed
class QodanaYamlLinterItemProvider : QodanaYamlItemProvider {
  companion object {
    const val ID = "linter"
  }

  override suspend fun provide(project: Project): QodanaYamlItem {
    if (ApplicationInfo.getInstance().build.productCode == "RD") {
      @Language("YAML")
      val content = """
      
      #Specify IDE code to run analysis without container $APPLIED_IN_CI_COMMENT
      ide: QDNET
    """.trimIndent()
      return QodanaYamlItem(ID, 1, content)
    }
    @Language("YAML")
    val content = """
      
      #Specify Qodana linter for analysis $APPLIED_IN_CI_COMMENT
      linter: ${getQodanaImageNameMatchingIDE(useVersionPostfix = true)}
    """.trimIndent()
    return QodanaYamlItem(ID, 1000, content)
  }
}