// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.ide.index;

import com.intellij.openapi.fileTypes.FileTypeRegistry;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.vfs.JarFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.indexing.DefaultFileTypeSpecificInputFilter;
import com.jetbrains.lang.dart.DartFileType;
import org.jetbrains.annotations.NotNull;

public class DartInputFilter extends DefaultFileTypeSpecificInputFilter {
  public static DartInputFilter INSTANCE = new DartInputFilter();

  @Override
  public boolean acceptInput(@NotNull VirtualFile file) {
    boolean accepts = super.acceptInput(file);
    if (accepts && FileTypeRegistry.getInstance().isFileOfType(file, StdFileTypes.HTML)) {
      accepts = !(file.getFileSystem() instanceof JarFileSystem);
    }
    return accepts;
  }

  public DartInputFilter() {
    super(DartFileType.INSTANCE, StdFileTypes.HTML);
  }
}
