package com.intellij.javascript.flex.compiled;

import com.intellij.javascript.flex.FlexApplicationComponent;
import com.intellij.lang.javascript.ECMAL4ParserDefinition;
import com.intellij.lang.javascript.types.JSFileElementType;
import com.intellij.psi.tree.IFileElementType;

/**
 * Created by IntelliJ IDEA.
 * User: Maxim.Mossienko
 * Date: 07.03.2009
 * Time: 0:33:23
 * To change this template use File | Settings | File Templates.
 */
public class DecompiledSwfParserDefinition extends ECMAL4ParserDefinition {
  private static final IFileElementType FILE_TYPE = new JSFileElementType(FlexApplicationComponent.DECOMPILED_SWF);

  @Override
  public IFileElementType getFileNodeType() {
    return FILE_TYPE;
  }
}
