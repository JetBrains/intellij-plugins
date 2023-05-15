// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.refactoring

import com.intellij.lang.javascript.psi.ecmal4.JSAttributeListOwner
import com.intellij.lang.javascript.refactoring.JSRenameExtension
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import org.angular2.Angular2DecoratorUtil.COMPONENT_DEC
import org.angular2.Angular2DecoratorUtil.findDecorator

class Angular2RenameExtension : JSRenameExtension {
  override fun getAdditionalFilesToRename(original: PsiElement,
                                          originalFile: PsiFile,
                                          newFileName: String): Map<PsiFile, String> {
    if (original !is JSAttributeListOwner ||
        findDecorator(original, COMPONENT_DEC) == null) {
      return emptyMap()
    }
    val componentVFile = originalFile.virtualFile
    val result: MutableMap<PsiFile, String> = HashMap()
    val dir = originalFile.parent
    if (dir != null && dir.isDirectory) {
      for (extension in CANDIDATE_EXTENSIONS) {
        val file = dir.findFile(componentVFile.nameWithoutExtension + "." + extension)
        if (file != null) {
          result[file] = "$newFileName.$extension"
        }
      }
    }
    return result
  }

  companion object {
    private val CANDIDATE_EXTENSIONS = arrayOf("css", "scss", "less", "styl", "html", "spec.ts")
  }
}