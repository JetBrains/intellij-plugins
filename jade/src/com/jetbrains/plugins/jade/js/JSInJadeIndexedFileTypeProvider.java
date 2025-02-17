package com.jetbrains.plugins.jade.js;

import com.intellij.lang.javascript.index.IndexedFileTypeProvider;
import com.intellij.openapi.fileTypes.FileType;
import com.jetbrains.plugins.jade.psi.JadeFileType;

public final class JSInJadeIndexedFileTypeProvider implements IndexedFileTypeProvider {

  @Override
  public FileType[] getFileTypesToIndex() {
    return new FileType[]{
      JadeFileType.INSTANCE,
    };
  }
}
