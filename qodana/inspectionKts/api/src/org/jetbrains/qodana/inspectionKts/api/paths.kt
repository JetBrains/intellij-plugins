@file:Suppress("unused")

package org.jetbrains.qodana.inspectionKts.api

import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.util.Key
import com.intellij.psi.PsiFile
import kotlin.io.path.pathString
import kotlin.io.path.relativeToOrNull

val INSPECTION_KTS_RELATIVE_PATH: Key<String> = Key.create<String>("qodana.inspection.kts.relative.path")

/**
 * DO NOT CHANGE THE SIGNATURE OF THIS METHOD IN NOT API-COMPATIBLE WAYS!
 * IT IS USED BY USER'S .inspection.kts SCRIPTS!!!
 * IF NEEDED, ASK ANY QUESTIONS QODANA CORE TEAM
 */
fun PsiFile.getPathRelativeToProject(): String? {
  val relativePath = getUserData(INSPECTION_KTS_RELATIVE_PATH)
  if (relativePath != null) return relativePath
  val projectDir = project.guessProjectDir()?.toNioPath() ?: return null
  return virtualFile.fileSystem.getNioPath(virtualFile)?.relativeToOrNull(projectDir)?.pathString
}
