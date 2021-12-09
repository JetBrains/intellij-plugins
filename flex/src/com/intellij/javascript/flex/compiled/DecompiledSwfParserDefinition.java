// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.javascript.flex.compiled;

import com.intellij.javascript.flex.FlexApplicationComponent;
import com.intellij.lang.javascript.dialects.ECMAL4ParserDefinition;
import com.intellij.lang.javascript.types.JSFileElementType;
import com.intellij.psi.tree.IFileElementType;
import org.jetbrains.annotations.NotNull;

public class DecompiledSwfParserDefinition extends ECMAL4ParserDefinition {
  private static final IFileElementType FILE_TYPE = JSFileElementType.create(FlexApplicationComponent.DECOMPILED_SWF);

  @Override
  public @NotNull IFileElementType getFileNodeType() {
    return FILE_TYPE;
  }
}
