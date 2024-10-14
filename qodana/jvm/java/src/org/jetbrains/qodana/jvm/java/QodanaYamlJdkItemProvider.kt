package org.jetbrains.qodana.jvm.java

import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.JavaSdkType
import com.intellij.openapi.projectRoots.JavaSdkVersionUtil
import com.intellij.openapi.roots.ProjectRootManager
import org.intellij.lang.annotations.Language
import org.jetbrains.qodana.settings.APPLIED_IN_CI_COMMENT
import org.jetbrains.qodana.settings.QodanaYamlItem
import org.jetbrains.qodana.settings.QodanaYamlItemProvider

class QodanaYamlJdkItemProvider : QodanaYamlItemProvider {
  companion object {
    private const val ID = "jdk"
  }

  override suspend fun provide(project: Project): QodanaYamlItem? {
    val projectJdk = ProjectRootManager.getInstance(project).projectSdk
    if (projectJdk?.sdkType !is JavaSdkType) return null

    val jdkVersion = JavaSdkVersionUtil.getJavaSdkVersion(projectJdk) ?: return null

    @Language("YAML")
    val content = """
      
      projectJDK: "${jdkVersion.maxLanguageLevel.feature()}" #$APPLIED_IN_CI_COMMENT
    """.trimIndent()
    return QodanaYamlItem(ID, 120, content)
  }

}