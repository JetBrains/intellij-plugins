// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.svg.parser;

import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.source.html.HtmlFileImpl;
import com.intellij.psi.tree.IFileElementType;
import org.angular2.lang.html.parser.Angular2HtmlParserDefinition;
import org.angular2.lang.svg.Angular2SvgFileElementType;
import org.jetbrains.annotations.NotNull;

public class Angular2SvgParserDefinition extends Angular2HtmlParserDefinition {

  @Override
  public @NotNull IFileElementType getFileNodeType() {
    return Angular2SvgFileElementType.INSTANCE;
  }

  @Override
  public @NotNull PsiFile createFile(@NotNull FileViewProvider viewProvider) {
    return new HtmlFileImpl(viewProvider, Angular2SvgFileElementType.INSTANCE);
  }
}
