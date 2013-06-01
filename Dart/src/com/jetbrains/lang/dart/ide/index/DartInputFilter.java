package com.jetbrains.lang.dart.ide.index;

import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.indexing.FileBasedIndex;
import com.jetbrains.lang.dart.DartFileType;

public class DartInputFilter implements FileBasedIndex.InputFilter {
  public static DartInputFilter INSTANCE = new DartInputFilter();

  @Override
  public boolean acceptInput(VirtualFile file) {
    FileType type = file.getFileType();
    return type == DartFileType.INSTANCE || type == StdFileTypes.HTML;
  }
}
