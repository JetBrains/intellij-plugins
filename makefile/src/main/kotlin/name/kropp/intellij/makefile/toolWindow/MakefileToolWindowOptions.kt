package name.kropp.intellij.makefile.toolWindow

import com.intellij.openapi.project.*
import name.kropp.intellij.makefile.*
import javax.swing.tree.*

class MakefileToolWindowOptions(val project: Project) {
  private val settings = project.getService(MakefileProjectSettings::class.java)

  var showSpecialTargets: Boolean
    get() = settings?.settings?.showHiddenInToolWindow ?: false
    set(value) { settings?.settings?.showHiddenInToolWindow = value }

  var autoScrollToSource: Boolean
    get() = settings?.settings?.autoScrollToSourceInToolWindow ?: false
    set(value) { settings?.settings?.autoScrollToSourceInToolWindow = value }

  fun getRootNode(): TreeNode {
    val files = MakefileTargetIndex.allTargets(project).filterNot { (it.isSpecialTarget && !showSpecialTargets) || it.isPatternTarget }.groupBy {
      it.containingFile
    }.map {
      MakefileFileNode(it.key, it.value.map(::MakefileTargetNode))
    }
    return MakefileRootNode(files)
  }
}