package com.jetbrains.lang.dart.ide.index;

import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.util.indexing.DefaultFileTypeSpecificInputFilter;
import com.jetbrains.lang.dart.DartFileType;

public class DartInputFilter extends DefaultFileTypeSpecificInputFilter {
  public static DartInputFilter INSTANCE = new DartInputFilter();

  public DartInputFilter() {
    super(DartFileType.INSTANCE, StdFileTypes.HTML);
  }
}
