// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.plugins.drools.lang.psi.indexes

import com.intellij.plugins.drools.DroolsFileType
import com.intellij.plugins.drools.lang.psi.DroolsFile
import com.intellij.util.indexing.*
import com.intellij.util.io.DataExternalizer
import com.intellij.util.io.EnumeratorStringDescriptor
import com.intellij.util.io.KeyDescriptor
import com.intellij.util.io.externalizer.StringCollectionExternalizer

internal class DroolsDeclareStatementScalarIndex : FileBasedIndexExtension<String, List<String>>() {
  override fun getKeyDescriptor(): KeyDescriptor<String> = EnumeratorStringDescriptor.INSTANCE

  companion object {
    var id = ID.create<String, List<String>>("drools.declareStatementFileIndex")
  }

  override fun getName(): ID<String, List<String>> = id

  override fun getIndexer(): DataIndexer<String, List<String>, FileContent> {
    return DataIndexer { inputData ->
      val declarations = hashMapOf<String, List<String>>()
      val file = inputData.psiFile as? DroolsFile ?: return@DataIndexer emptyMap<String, List<String>>()
      val pkg = file.`package`?.namespace?.text ?: ""

      val values = mutableListOf<String>()
      for (declareStatement in file.declarations) {
        declareStatement.typeDeclaration?.qualifiedName?.let { values.add(it) }
      }

      if (!values.isEmpty()) declarations.put(pkg, values)

      return@DataIndexer declarations
    }
  }

  override fun getValueExternalizer(): DataExternalizer<List<String>> = StringCollectionExternalizer.STRING_LIST_EXTERNALIZER

  override fun getVersion(): Int = 2

  override fun getInputFilter(): FileBasedIndex.InputFilter = DefaultFileTypeSpecificInputFilter(DroolsFileType.DROOLS_FILE_TYPE)

  override fun dependsOnFileContent(): Boolean = true
}
