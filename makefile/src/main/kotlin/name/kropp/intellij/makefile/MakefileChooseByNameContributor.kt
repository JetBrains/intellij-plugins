package name.kropp.intellij.makefile

import com.intellij.navigation.ChooseByNameContributor
import com.intellij.openapi.project.Project

class MakefileChooseByNameContributor : ChooseByNameContributor {
  override fun getItemsByName(name: String, pattern: String, project: Project, includeNonProjectItems: Boolean) = findTargets(project, name).toTypedArray()
  override fun getNames(project: Project, includeNonProjectItems: Boolean) = findTargets(project).map { it.text }.filterNot(String::isNullOrEmpty).toTypedArray()
}