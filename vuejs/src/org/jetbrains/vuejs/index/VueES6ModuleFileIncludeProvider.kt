// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.index

import com.intellij.lang.ecmascript6.index.ES6FileIncludeProvider
import com.intellij.lang.ecmascript6.psi.impl.ES6ImportPsiUtil
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.impl.include.FileIncludeInfo
import com.intellij.psi.impl.include.FileIncludeProvider
import com.intellij.util.Consumer
import com.intellij.util.indexing.FileContent
import org.jetbrains.vuejs.lang.html.VueFileType
import org.jetbrains.vuejs.lang.html.VueLanguage

/**
 * ES6FileIncludeProvider doesn't work for vue files but we need these files in index for building ts imports graph
 * @see ES6FileIncludeProvider
 */
class VueES6ModuleFileIncludeProvider : FileIncludeProvider() {

  override fun getId(): String {
    return VueLanguage.INSTANCE.id
  }

  override fun acceptFile(file: VirtualFile): Boolean {
    return file.fileType == VueFileType.INSTANCE
  }

  override fun registerFileTypesUsedForIndexing(fileTypeSink: Consumer<in FileType>) {
    fileTypeSink.consume(VueFileType.INSTANCE)
  }

  override fun getIncludeInfos(content: FileContent): Array<FileIncludeInfo> {
    if (!ES6FileIncludeProvider.checkTextHasFromKeyword(content)) return emptyArray()


    val psiFile = content.psiFile
    val importDeclarations = (findModule(psiFile, false)?.let { ES6ImportPsiUtil.getImportDeclarations(it) } ?: emptyList()) +
                             (findModule(psiFile, true)?.let { ES6ImportPsiUtil.getImportDeclarations(it) } ?: emptyList())

    val result = importDeclarations.map {
      val importModuleText = it.importModuleText
      if (importModuleText != null) return@map importModuleText
      val fromClause = it.fromClause ?: return@map null
      val referenceText = fromClause.referenceText ?: return@map null
      return@map StringUtil.unquoteString(referenceText)
    }.filterNotNull().toSet()

    return ES6FileIncludeProvider.buildFileIncludeInfo(result)
  }
}
