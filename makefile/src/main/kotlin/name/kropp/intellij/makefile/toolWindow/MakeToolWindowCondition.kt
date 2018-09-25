package name.kropp.intellij.makefile.toolWindow

import com.intellij.openapi.project.*
import com.intellij.openapi.util.Condition
import com.intellij.psi.search.*
import name.kropp.intellij.makefile.*

class MakeToolWindowCondition : Condition<Project> {
  override fun value(project: Project) =
      FileTypeIndex.containsFileOfType(MakefileFileType, GlobalSearchScope.projectScope(project))
}
