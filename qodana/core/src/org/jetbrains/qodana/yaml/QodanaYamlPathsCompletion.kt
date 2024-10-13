package org.jetbrains.qodana.yaml

import com.intellij.codeInsight.AutoPopupController
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.lookup.LookupElementPresentation
import com.intellij.icons.AllIcons
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.vfs.findDirectory
import com.intellij.openapi.vfs.findFileOrDirectory
import kotlinx.coroutines.*
import org.jetbrains.yaml.psi.YAMLFile

class QodanaYamlPathsCompletion : QodanaYamlCompletionContributorBase() {
  override suspend fun variantsForKey(key: String, file: YAMLFile, prefix: String): List<QodanaLookupElement> = when (key) {
    QODANA_INSPECTION_INCLUDE_PATHS, QODANA_INSPECTION_EXCLUDE_PATHS -> getAllPathsAsync(
      file.project,
      prefix.removeSuffix("IntellijIdeaRulezzz")
    )
    else -> emptyList()
  }

  private suspend fun getAllPathsAsync(project: Project, prefix: String): List<QodanaPathLookupElement> {
    return withContext(Dispatchers.IO) {
      async {
        val root = project.guessProjectDir()
        if (root == null) {
          return@async emptyList()
        }
        val prefixPath = prefix.dropLastWhile { it != '/' }

        val currentDir = root.findFileOrDirectory(prefix)
        if (currentDir != null && !currentDir.isDirectory) {
          return@async emptyList()
        }

        val currentDirLookup = if (currentDir != null)
          listOf(QodanaPathLookupElement(prefix, PathType.DIRECTORY, false))
        else
          emptyList()

        val dir = currentDir ?: root.findDirectory(prefixPath)

        if (dir != null) {
          currentDirLookup + dir.children
            ?.associateBy { it.path.removePrefix(root.path).removePrefix("/") }
            ?.filterKeys { it.removeSuffix("/").startsWith(prefix) }
            .orEmpty()
            .map { (path, virtualFile) ->
              QodanaPathLookupElement(path, if (virtualFile.isDirectory) PathType.DIRECTORY else PathType.FILE)
            }
        }
        else {
          emptyList()
        }
      }
    }.await()
  }
}

internal class QodanaPathLookupElement(val path: String, val type: PathType, private val autoPopup: Boolean = true) : QodanaLookupElement(path, "") {
  override fun handleInsert(context: InsertionContext) {
    super.handleInsert(context)
    val editor = context.editor
    val project = editor.project ?: return
    if (autoPopup) {
      AutoPopupController.getInstance(project).scheduleAutoPopup(editor)
    }
  }

  override fun renderElement(presentation: LookupElementPresentation) {
    super.renderElement(presentation)
    presentation.icon = when (type) {
      PathType.FILE -> when {
        path.endsWith(".java") -> AllIcons.FileTypes.Java
        path.endsWith(".js") -> AllIcons.FileTypes.JavaScript
        path.endsWith(".html") -> AllIcons.FileTypes.Html
        path.endsWith(".css") -> AllIcons.FileTypes.Css
        path.endsWith(".json") -> AllIcons.FileTypes.Json
        path.endsWith(".xml") -> AllIcons.FileTypes.Xml
        path.endsWith(".txt") -> AllIcons.FileTypes.Text
        path.endsWith(".yaml") -> AllIcons.FileTypes.Yaml
        path.endsWith(".properties") -> AllIcons.FileTypes.Properties
        else -> AllIcons.FileTypes.Any_type
      }
      PathType.DIRECTORY -> AllIcons.Nodes.Folder
    }
  }
}

internal enum class PathType {
  FILE, DIRECTORY
}