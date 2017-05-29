package com.intellij.javascript.flex.compiled;

import com.intellij.javascript.flex.FlexApplicationComponent;
import com.intellij.lang.javascript.types.JSFileElementType;
import com.intellij.psi.tree.IFileElementType;

/**
 * @author Konstantin Ulitin
 */
public interface DecompiledSwfElementTypes {
  IFileElementType FILE_TYPE = JSFileElementType.create(FlexApplicationComponent.DECOMPILED_SWF);
}
