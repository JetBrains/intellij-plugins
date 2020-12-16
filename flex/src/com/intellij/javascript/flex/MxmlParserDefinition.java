// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.javascript.flex;

import com.intellij.lang.javascript.flex.MxmlLanguage;
import com.intellij.lang.xml.XMLParserDefinition;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.source.xml.XmlFileImpl;
import com.intellij.psi.tree.IFileElementType;
import org.jetbrains.annotations.NotNull;

public class MxmlParserDefinition extends XMLParserDefinition{
  @Override
  public @NotNull PsiFile createFile(@NotNull FileViewProvider viewProvider) {
    return new MxmlFile(viewProvider);
  }

  public static class MxmlFile extends XmlFileImpl {
    private static final IFileElementType MXML_FILE = new IFileElementType("MXML_FILE", MxmlLanguage.INSTANCE);

    public MxmlFile(FileViewProvider viewProvider) {
      super(viewProvider, MXML_FILE);
    }
  }
}