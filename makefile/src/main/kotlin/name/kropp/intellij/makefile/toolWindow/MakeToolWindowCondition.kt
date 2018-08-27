package name.kropp.intellij.makefile.toolWindow

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Condition
import name.kropp.intellij.makefile.MakefileTargetIndex

class MakeToolWindowCondition : Condition<Project> {
  override fun value(project: Project) = MakefileTargetIndex.allTargets(project).any()
}
