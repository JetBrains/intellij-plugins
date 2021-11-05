// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.index

import com.intellij.lang.javascript.ecmascript6.TypeScriptUtil.TYPESCRIPT_DECLARATIONS_FILE_EXTENSION
import com.intellij.lang.javascript.psi.JSFile
import com.intellij.lang.javascript.psi.ecma6.TypeScriptVariable
import com.intellij.lang.javascript.psi.util.JSStubBasedPsiTreeUtil
import com.intellij.psi.PsiFile
import com.intellij.util.indexing.*
import com.intellij.util.io.KeyDescriptor
import org.jetbrains.vuejs.model.typed.VueTypedEntitiesProvider
import java.util.*

class VueTypedComponentFilesIndex : ScalarIndexExtension<Boolean>() {

  override fun getName(): ID<Boolean, Void> = VUE_TYPED_COMPONENTS_INDEX

  override fun getInputFilter(): FileBasedIndex.InputFilter =
    FileBasedIndex.InputFilter { file -> file.nameSequence.endsWith(TYPESCRIPT_DECLARATIONS_FILE_EXTENSION) }

  override fun dependsOnFileContent(): Boolean = true

  override fun getIndexer(): DataIndexer<Boolean, Void, FileContent> = DataIndexer { inputData ->
    Collections.singletonMap<Boolean, Void>(hasComponentDefinitions(inputData.psiFile), null)
  }

  private fun hasComponentDefinitions(psiFile: PsiFile): Boolean {
    if (psiFile !is JSFile) return false
    var result = false
    JSStubBasedPsiTreeUtil.processDeclarationsInScope(psiFile, { element, _ ->
      if (element is TypeScriptVariable
          && element.isExported
          && VueTypedEntitiesProvider.isComponentDefinition(element)) {
        result = true
        false
      }
      else true
    }, false)
    return result
  }

  override fun getKeyDescriptor(): KeyDescriptor<Boolean> = BooleanKeyDescriptor()

  override fun getVersion(): Int = 0

  companion object {
    val VUE_TYPED_COMPONENTS_INDEX = ID.create<Boolean, Void>("VueTypedComponentFilesIndex")
  }
}

