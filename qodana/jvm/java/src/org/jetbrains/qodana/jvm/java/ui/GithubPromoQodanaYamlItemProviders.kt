package org.jetbrains.qodana.jvm.java.ui

import com.intellij.openapi.project.Project
import org.intellij.lang.annotations.Language
import org.jetbrains.qodana.settings.DefaultQodanaItemContext
import org.jetbrains.qodana.settings.QodanaYamlItem
import org.jetbrains.qodana.settings.QodanaYamlItemContext
import org.jetbrains.qodana.settings.QodanaYamlItemProvider
import org.jetbrains.qodana.ui.Linter
import org.jetbrains.qodana.ui.getQodanaImageNameMatchingIDE

class GithubPromoQodanaYamlItemContext: DefaultQodanaItemContext()

class QodanaYamlGithubPromoLinterItemProvider : QodanaYamlItemProvider {
  companion object {
    const val ID: String = "githubPromoLinter"
  }

  override suspend fun provide(project: Project, context: QodanaYamlItemContext): QodanaYamlItem? {
    if (context !is GithubPromoQodanaYamlItemContext) return null
    // in case of any changes also change contrib/qodana/core/src/org/jetbrains/qodana/staticAnalysis/stat/UsageCollector.kt
    @Language("YAML")
    val content = """
      
      #Qodana supports other languages, for example, Python, JavaScript, TypeScript, Go, C#, PHP
      #For all supported languages see https://www.jetbrains.com/help/qodana/linters.html
      linter: ${getQodanaImageNameMatchingIDE(useVersionPostfix = true, Linter.IC)}
    """.trimIndent()
    return QodanaYamlItem(ID, 1000, content)
  }
}