package com.intellij.javascript.flex;

import com.intellij.lang.javascript.index.JSImplicitElementsIndexFileTypeProvider;
import com.intellij.openapi.fileTypes.FileType;

public class MxmlJSImplicitElementsIndexFileTypeProvider implements JSImplicitElementsIndexFileTypeProvider {
  @Override
  public FileType[] getFileTypes() {
    return new FileType[]{FlexApplicationComponent.MXML};
  }
}
