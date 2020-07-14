// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.index

import com.intellij.psi.xml.XmlFile
import com.intellij.util.indexing.*
import com.intellij.util.io.KeyDescriptor
import org.jetbrains.vuejs.lang.html.VueFileType
import java.io.DataInput
import java.io.DataOutput
import java.util.*

class VueNoScriptFilesIndex : ScalarIndexExtension<Boolean>() {

  override fun getName(): ID<Boolean, Void> = VUE_NO_SCRIPT_FILES_INDEX

  override fun getIndexer(): DataIndexer<Boolean, Void, FileContent> = DataIndexer { inputData ->
    inputData.psiFile.let {
      it is XmlFile && findScriptTag(it) == null
    }.let {
      Collections.singletonMap<Boolean, Void>(it, null)
    }
  }

  override fun getVersion(): Int = 2

  override fun getInputFilter(): FileBasedIndex.InputFilter = FileBasedIndex.InputFilter {
    it.fileType == VueFileType.INSTANCE
  }

  companion object {
    val VUE_NO_SCRIPT_FILES_INDEX = ID.create<Boolean, Void>("VueNoScriptFilesIndex")
  }

  override fun getKeyDescriptor(): KeyDescriptor<Boolean> = object : KeyDescriptor<Boolean> {
    override fun getHashCode(value: Boolean): Int = value.hashCode()

    override fun isEqual(val1: Boolean, val2: Boolean): Boolean = val1 == val2

    override fun save(out: DataOutput, value: Boolean) = out.writeBoolean(value)

    override fun read(`in`: DataInput): Boolean = `in`.readBoolean()

  }

  override fun dependsOnFileContent(): Boolean = true

}