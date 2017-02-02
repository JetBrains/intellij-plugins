package name.kropp.intellij.makefile

import com.intellij.navigation.ChooseByNameContributor
import com.intellij.openapi.project.Project

class MakefileChooseByNameContributor : ChooseByNameContributor {
  override fun getItemsByName(name: String, pattern: String, project: Project, includeNonProjectItems: Boolean) = findTargets(project, name).filterNot { it.isSpecialTarget }.toTypedArray()
  override fun getNames(project: Project, includeNonProjectItems: Boolean) = findTargets(project).filterNot { it.isSpecialTarget }.map { it.text }.filterNot(String::isNullOrEmpty).toTypedArray()
}