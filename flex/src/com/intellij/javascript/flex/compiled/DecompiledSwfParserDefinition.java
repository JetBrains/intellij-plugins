package com.intellij.javascript.flex.compiled;

import com.intellij.lang.javascript.dialects.ECMAL4ParserDefinition;
import com.intellij.psi.tree.IFileElementType;

import static com.intellij.javascript.flex.compiled.DecompiledSwfElementTypes.FILE_TYPE;

public class DecompiledSwfParserDefinition extends ECMAL4ParserDefinition {

  @Override
  public IFileElementType getFileNodeType() {
    return FILE_TYPE;
  }
}
