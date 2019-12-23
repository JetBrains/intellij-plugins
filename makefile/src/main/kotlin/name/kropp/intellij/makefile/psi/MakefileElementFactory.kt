package name.kropp.intellij.makefile.psi

import com.intellij.openapi.project.*
import com.intellij.psi.*
import name.kropp.intellij.makefile.*

object MakefileElementFactory {
  fun createFile(project: Project, text: String) =
      PsiFileFactory.getInstance(project).createFileFromText("Makefile", MakefileFileType, text) as MakefileFile

  fun createRule(project: Project, target: String) =
      createFile(project, "$target:\n").firstChild as MakefileRule

  fun createTarget(project: Project, name: String) =
      createRule(project, name).firstChild.firstChild.firstChild as MakefileTarget

  fun createPrerequisite(project: Project, name: String) =
      (createFile(project, "a: $name").firstChild as MakefileRule).targetLine.prerequisites!!.normalPrerequisites.firstChild as MakefilePrerequisite

  fun createWhiteSpace(project: Project, whitespace: String) =
      createFile(project, whitespace).firstChild as PsiWhiteSpace

  fun createEOL(project: Project, whitespace: String) =
      createFile(project, whitespace).firstChild

  fun createRecipe(project: Project, text: String) =
      createRule(project, "target:\n\t$text").firstChild.nextSibling as MakefileRecipe
}