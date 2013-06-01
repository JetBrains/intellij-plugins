package com.jetbrains.lang.dart.highlight;

import com.intellij.openapi.util.Condition;
import com.intellij.openapi.vfs.VirtualFile;
import com.jetbrains.lang.dart.DartFileType;

/**
 * @author: Fedor.Korotkov
 */
public class DartProblemFileHighlightFilter implements Condition<VirtualFile> {
  public boolean value(VirtualFile virtualFile) {
    return virtualFile.getFileType() == DartFileType.INSTANCE;
  }
}
