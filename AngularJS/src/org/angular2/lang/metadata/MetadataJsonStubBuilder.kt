// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.metadata;

import com.intellij.json.JsonLanguage;
import com.intellij.json.psi.impl.JsonFileImpl;
import com.intellij.openapi.fileEditor.impl.LoadTextUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.stubs.BinaryFileStubBuilder;
import com.intellij.psi.stubs.Stub;
import com.intellij.util.indexing.FileContent;
import org.angular2.lang.metadata.stubs.MetadataFileStubImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MetadataJsonStubBuilder implements BinaryFileStubBuilder {

  @Override
  public boolean acceptsFile(@NotNull VirtualFile file) {
    return file.getFileType() instanceof MetadataJsonFileType;
  }

  @Override
  public @Nullable Stub buildStubTree(@NotNull FileContent fileContent) {
    MetadataJsonFileType fileType = (MetadataJsonFileType)fileContent.getFileType();

    CharSequence text = LoadTextUtil.getTextByBinaryPresentation(
      fileContent.getContent(), fileContent.getFile());

    JsonFileImpl jsonFile = (JsonFileImpl)PsiFileFactory
      .getInstance(fileContent.getProject())
      .createFileFromText(JsonLanguage.INSTANCE, text);

    MetadataFileStubImpl result = new MetadataFileStubImpl(null, fileType.getFileElementType());
    if (jsonFile.getTopLevelValue() != null) {
      fileType.createRootStub(result, jsonFile.getTopLevelValue());
    }
    return result;
  }

  @Override
  public int getStubVersion() {
    return 22;
  }
}
