// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.index

import com.intellij.lang.ecmascript6.psi.JSExportAssignment
import com.intellij.lang.ecmascript6.resolve.ES6PsiUtil
import com.intellij.lang.javascript.psi.JSCallExpression
import com.intellij.lang.javascript.psi.JSEmbeddedContent
import com.intellij.lang.javascript.psi.JSObjectLiteralExpression
import com.intellij.lang.javascript.psi.ecmal4.JSClass
import com.intellij.lang.javascript.psi.impl.JSPropertyImpl
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlFile
import com.intellij.util.indexing.*
import com.intellij.util.io.KeyDescriptor
import org.jetbrains.vuejs.lang.html.VueFileType
import org.jetbrains.vuejs.libraries.componentDecorator.findComponentDecorator
import org.jetbrains.vuejs.model.source.VueComponents
import java.util.*

class VueEmptyComponentInitializersIndex : ScalarIndexExtension<Boolean>() {

  override fun getName(): ID<Boolean, Void> = VUE_NO_INITIALIZER_COMPONENTS_INDEX

  override fun getIndexer(): DataIndexer<Boolean, Void, FileContent> = DataIndexer { inputData ->
    inputData.psiFile.let { file ->
      file is XmlFile && findScriptTag(file, false).let { script ->
        if (script == null) {
          true
        }
        else {
          val module = PsiTreeUtil.getStubChildOfType(script, JSEmbeddedContent::class.java)
          if (module != null) {
            val exportedElement = (ES6PsiUtil.findDefaultExport(module) as? JSExportAssignment)
              ?.stubSafeElement
            if (exportedElement is JSObjectLiteralExpression || exportedElement is JSCallExpression) {
              VueComponents.getSourceComponentDescriptor(exportedElement)
                ?.initializer
                ?.let { it as? JSObjectLiteralExpression }
                ?.properties
                ?.count { it is JSPropertyImpl } == 0
            }
            else
              (exportedElement is JSClass
               && findComponentDecorator(exportedElement) == null)
          }
          else true
        }
      }
    }.let {
      Collections.singletonMap<Boolean, Void>(it, null)
    }
  }

  override fun getVersion(): Int = 8

  override fun getInputFilter(): FileBasedIndex.InputFilter = object: DefaultFileTypeSpecificInputFilter(VueFileType.INSTANCE) {
    override fun acceptInput(file: VirtualFile): Boolean {
      return file.fileType == VueFileType.INSTANCE
    }
  }

  companion object {
    val VUE_NO_INITIALIZER_COMPONENTS_INDEX = ID.create<Boolean, Void>("VueNoScriptFilesIndex")
  }

  override fun getKeyDescriptor(): KeyDescriptor<Boolean> = BooleanKeyDescriptor()

  override fun dependsOnFileContent(): Boolean = true

}