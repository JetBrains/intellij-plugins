// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hcl

import com.intellij.openapi.progress.ProgressIndicatorProvider
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.indexing.*
import com.intellij.util.io.EnumeratorStringDescriptor
import com.intellij.util.io.KeyDescriptor
import org.intellij.terraform.hcl.psi.*
import org.intellij.terraform.config.TerraformFileType
import org.jetbrains.annotations.NonNls
import java.util.*

class HCLSymbolIndex : ScalarIndexExtension<String>() {
  companion object {
    @NonNls
    val NAME = ID.create<String, Void>("HCLSymbolIndex")
  }

  override fun getName(): ID<String, Void> = NAME

  override fun getVersion(): Int = 0

  override fun dependsOnFileContent() = true

  override fun getInputFilter(): FileBasedIndex.InputFilter {
    return object : DefaultFileTypeSpecificInputFilter(HCLFileType, TerraformFileType) {
      override fun acceptInput(file: VirtualFile): Boolean {
        return file.isInLocalFileSystem
      }
    }
  }

  override fun getKeyDescriptor(): KeyDescriptor<String> {
    return EnumeratorStringDescriptor.INSTANCE
  }

  override fun getIndexer(): DataIndexer<String, Void, FileContent> {
    return DataIndexer { inputData ->
      val map = HashMap<String, Void?>()
      if (inputData.fileType === HCLFileType || inputData.fileType === TerraformFileType) {
        val file = inputData.psiFile
        if (file is HCLFile) {
          file.acceptChildren(object : HCLElementVisitor() {

            override fun visitElement(element: HCLElement) {
              ProgressIndicatorProvider.checkCanceled()
              element.acceptChildren(this)
            }

            override fun visitProperty(o: HCLProperty) {
              o.name.let { map.put(it, null) }
              ProgressIndicatorProvider.checkCanceled()
              o.value?.accept(this)
            }

            override fun visitBlock(o: HCLBlock) {
              o.fullName.let { map.put(it, null) }
              o.nameElements.forEach { el -> el?.name?.let { map.put(it, null) } }
              ProgressIndicatorProvider.checkCanceled()
              o.`object`?.acceptChildren(this)
            }
          })
        }
      }
      map
    }
  }
}
