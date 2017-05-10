package com.intellij.javascript.flex.compiled;

import com.intellij.javascript.flex.FlexApplicationComponent;
import com.intellij.lang.javascript.dialects.ECMAL4ParserDefinition;
import com.intellij.lang.javascript.types.JSFileElementType;
import com.intellij.psi.tree.IFileElementType;

public class DecompiledSwfParserDefinition extends ECMAL4ParserDefinition {
  private static final IFileElementType FILE_TYPE = JSFileElementType.create(FlexApplicationComponent.DECOMPILED_SWF);

  @Override
  public IFileElementType getFileNodeType() {
    return FILE_TYPE;
  }
}
