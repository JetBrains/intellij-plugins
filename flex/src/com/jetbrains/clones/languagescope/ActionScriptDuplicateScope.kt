// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.clones.languagescope

import com.intellij.lang.javascript.ActionScriptFileType
import com.intellij.openapi.fileTypes.FileTypeRegistry
import com.intellij.openapi.vfs.VirtualFile

class ActionScriptDuplicateScope : JSBaseDuplicateScope("ActionScript") {
  override fun isDuplicateFileAcceptable(file: VirtualFile): Boolean {
    return FileTypeRegistry.getInstance().isFileOfType(file, ActionScriptFileType.INSTANCE)
  }
}