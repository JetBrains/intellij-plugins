// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.index

import com.intellij.lang.javascript.modules.NodeModuleUtil
import com.intellij.openapi.fileTypes.FileType
import com.intellij.psi.XmlElementVisitor
import com.intellij.psi.xml.XmlTag
import com.intellij.util.ThreeState
import com.intellij.util.indexing.DataIndexer
import com.intellij.util.indexing.FileBasedIndex.InputFilter
import com.intellij.util.indexing.FileContent
import com.intellij.util.indexing.ID
import com.intellij.util.indexing.IndexedFile
import com.intellij.util.indexing.ScalarIndexExtension
import com.intellij.util.indexing.hints.BaseFileTypeInputFilter
import com.intellij.util.indexing.hints.FileTypeSubstitutionStrategy.AFTER_SUBSTITUTION
import com.intellij.util.io.EnumeratorStringDescriptor
import com.intellij.util.io.KeyDescriptor
import com.intellij.xml.util.HtmlUtil
import com.intellij.xml.util.HtmlUtil.LANG_ATTRIBUTE_NAME
import org.jetbrains.vuejs.lang.html.VueFile
import org.jetbrains.vuejs.lang.html.VueFileType

val VUE_COMPONENT_STYLES_INDEX_KEY = ID.create<String, Void>("VueComponentStylesIndex")

/**
 * Indexes style languages used in *.vue files.
 */
class VueComponentStylesIndex : ScalarIndexExtension<String>() {

  override fun getName(): ID<String, Void> = VUE_COMPONENT_STYLES_INDEX_KEY

  override fun getIndexer(): DataIndexer<String, Void, FileContent> = DataIndexer { fileContent ->
    val result = mutableSetOf<String>()
    (fileContent.psiFile as? VueFile)?.document?.acceptChildren(object : XmlElementVisitor() {
      override fun visitXmlTag(tag: XmlTag) {
        if (tag.name == HtmlUtil.STYLE_TAG_NAME) {
          result.add(tag.getAttributeValue(LANG_ATTRIBUTE_NAME) ?: "")
        }
      }
    })
    result.associateWith { null }
  }

  override fun getKeyDescriptor(): KeyDescriptor<String> =
    EnumeratorStringDescriptor.INSTANCE

  override fun getVersion(): Int = 2

  override fun getInputFilter(): InputFilter = object : BaseFileTypeInputFilter(AFTER_SUBSTITUTION) {
    override fun acceptFileType(fileType: FileType): ThreeState {
      return if (fileType == VueFileType) {
        ThreeState.UNSURE // check hasNodeModulesDirInPath
      }
      else {
        ThreeState.NO
      }
    }

    override fun slowPathIfFileTypeHintUnsure(file: IndexedFile): Boolean {
      return !NodeModuleUtil.hasNodeModulesDirInPath(file.file, null)
    }
  }

  override fun dependsOnFileContent(): Boolean = true

}