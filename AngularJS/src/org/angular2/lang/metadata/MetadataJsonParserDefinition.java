// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.metadata;

import com.intellij.json.JsonParserDefinition;
import com.intellij.json.psi.impl.JsonFileImpl;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IFileElementType;

public class MetadataJsonParserDefinition extends JsonParserDefinition {

  public static final IFileElementType FILE = new IFileElementType(MetadataJsonLanguage.INSTANCE);

  @Override
  public PsiFile createFile(FileViewProvider fileViewProvider) {
    return new JsonFileImpl(fileViewProvider, MetadataJsonLanguage.INSTANCE);
  }

  @Override
  public IFileElementType getFileNodeType() {
    return FILE;
  }

}
