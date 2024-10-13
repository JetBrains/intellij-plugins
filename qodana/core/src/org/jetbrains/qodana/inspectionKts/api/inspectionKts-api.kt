@file:Suppress("unused")

package org.jetbrains.qodana.inspectionKts.api

import com.intellij.codeHighlighting.HighlightDisplayLevel
import com.intellij.codeInspection.GlobalInspectionTool
import com.intellij.codeInspection.InspectionProfileEntry
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.vfs.findFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import org.jetbrains.qodana.inspectionKts.InspectionKtsDefaultImportProvider

private class MainInspectionKtsDefaultImportProvider : InspectionKtsDefaultImportProvider {
  override fun imports(): List<String> = listOf(
    "org.jetbrains.qodana.inspectionKts.api.*",
    "com.intellij.codeHighlighting.HighlightDisplayLevel",
    "com.intellij.psi.util.*",
  )
}


/**
 * DO NOT CHANGE THE SIGNATURE OF THIS INTERFACE IN NOT API-COMPATIBLE WAYS!
 * IT IS USED BY USER'S .inspection.kts SCRIPTS!!!
 * IF NEEDED, ASK ANY QUESTIONS QODANA CORE TEAM
 */
interface Inspection {
  val project: Project

  fun registerProblem(psiElement: PsiElement?, message: String)


  fun findPsiFileByRelativeToProjectPath(relativeToProjectPath: String): PsiFile? {
    val projectDir = project.guessProjectDir() ?: return null
    val virtualFile = projectDir.findFile(relativeToProjectPath) ?: return null
    return PsiManager.getInstance(project).findFile(virtualFile)
  }

  fun findPsiFileByRelativeToOtherFilePath(base: PsiFile, relativeToBasePath: String): PsiFile? {
    val virtualFile = base.virtualFile.findFile(relativeToBasePath) ?: return null
    return PsiManager.getInstance(project).findFile(virtualFile)
  }
}

/**
 * DO NOT CHANGE THE SIGNATURE OF THIS METHOD IN NOT API-COMPATIBLE WAYS!
 * IT IS USED BY USER'S .inspection.kts SCRIPTS!!!
 * IF NEEDED, ASK ANY QUESTIONS QODANA CORE TEAM
 */
@JvmOverloads
fun InspectionKts(
  id: String,
  globalTool: GlobalInspectionTool,
  name: String = id,
  htmlDescription: String? = null,
  level: HighlightDisplayLevel = HighlightDisplayLevel.WARNING,
  language: String = "",
): InspectionKts {
  return InspectionKtsImpl(id, name, htmlDescription, level, globalTool, language)
}

/**
 * DO NOT CHANGE THE SIGNATURE OF THIS METHOD IN NOT API-COMPATIBLE WAYS!
 * IT IS USED BY USER'S .inspection.kts SCRIPTS!!!
 * IF NEEDED, ASK ANY QUESTIONS QODANA CORE TEAM
 */
@JvmOverloads
fun InspectionKts(
  id: String,
  localTool: LocalInspectionTool,
  name: String = id,
  htmlDescription: String? = null,
  level: HighlightDisplayLevel = HighlightDisplayLevel.WARNING,
  language: String = "",
): InspectionKts {
  return InspectionKtsImpl(id, name, htmlDescription, level, localTool, language)
}

/**
 * DO NOT CHANGE THE SIGNATURE OF THIS INTERFACE IN NOT API-COMPATIBLE WAYS!
 * IT IS USED BY USER'S .inspection.kts SCRIPTS!!!
 * IF NEEDED, ASK ANY QUESTIONS QODANA CORE TEAM
 */
interface InspectionKts {
  val tool: InspectionProfileEntry

  val id: String

  val name: String

  val htmlDescription: String?

  val level: HighlightDisplayLevel

  val language: String
}

private data class InspectionKtsImpl(
  override val id: String,
  override val name: String,
  override val htmlDescription: String?,
  override val level: HighlightDisplayLevel,
  override val tool: InspectionProfileEntry,
  override val language: String
) : InspectionKts