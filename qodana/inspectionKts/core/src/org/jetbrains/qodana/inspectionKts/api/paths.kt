@file:Suppress("unused")

package org.jetbrains.qodana.inspectionKts.api

import com.intellij.openapi.project.guessProjectDir
import com.intellij.psi.PsiFile
import kotlin.io.path.pathString
import kotlin.io.path.relativeToOrNull

/**
 * DO NOT CHANGE THE SIGNATURE OF THIS METHOD IN NOT API-COMPATIBLE WAYS!
 * IT IS USED BY USER'S .inspection.kts SCRIPTS!!!
 * IF NEEDED, ASK ANY QUESTIONS QODANA CORE TEAM
 */
fun PsiFile.getPathRelativeToProject(): String? {
  val projectDir = project.guessProjectDir()?.toNioPath() ?: return null
  return virtualFile.fileSystem.getNioPath(virtualFile)?.relativeToOrNull(projectDir)?.pathString
}
