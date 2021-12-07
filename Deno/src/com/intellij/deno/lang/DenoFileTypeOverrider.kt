package com.intellij.deno.lang

import com.intellij.deno.DenoUtil.isDenoCacheFile
import com.intellij.lang.javascript.TypeScriptFileType
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.fileTypes.impl.FileTypeOverrider
import com.intellij.openapi.vfs.VirtualFile

class DenoFileTypeOverrider : FileTypeOverrider {
  override fun getOverriddenFileType(file: VirtualFile): FileType? {
    return if (isDenoCacheFile(file)) TypeScriptFileType.INSTANCE else null
  }
}