// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.metadata;

import com.intellij.json.psi.JsonValue;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.ex.FileTypeIdentifiableByVirtualFile;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.tree.IStubFileElementType;
import org.angular2.lang.metadata.stubs.MetadataFileStubImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public abstract class MetadataJsonFileType implements FileType, FileTypeIdentifiableByVirtualFile {

  protected MetadataJsonFileType() {
  }

  @Override
  public @NotNull String getDefaultExtension() {
    return "json";
  }

  @Override
  public @Nullable Icon getIcon() {
    return null;
  }

  @Override
  public boolean isBinary() {
    return true;
  }

  @Override
  public boolean isReadOnly() {
    return true;
  }

  @Override
  public @Nullable String getCharset(@NotNull VirtualFile file, final byte @NotNull [] content) {
    return null;
  }

  public abstract IStubFileElementType getFileElementType();

  protected abstract void createRootStub(MetadataFileStubImpl result, JsonValue value);
}
