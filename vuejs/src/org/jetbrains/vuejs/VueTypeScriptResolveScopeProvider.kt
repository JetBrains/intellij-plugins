package org.jetbrains.vuejs

import com.intellij.lang.javascript.ecmascript6.TypeScriptResolveScopeProvider
import com.intellij.openapi.vfs.VirtualFile

class VueTypeScriptResolveScopeProvider : TypeScriptResolveScopeProvider() {
  override fun isApplicable(file: VirtualFile) = file.fileType == VueFileType.INSTANCE || super.isApplicable(file)
}