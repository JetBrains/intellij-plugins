// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities.metadata;

import com.intellij.json.psi.JsonValue;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.tree.IStubFileElementType;
import org.angular2.entities.metadata.stubs.Angular2MetadataNodeModuleStub;
import org.angular2.lang.Angular2Bundle;
import org.angular2.lang.metadata.MetadataJsonFileType;
import org.angular2.lang.metadata.MetadataJsonLanguage;
import org.angular2.lang.metadata.psi.MetadataStubFileElementType;
import org.angular2.lang.metadata.stubs.MetadataFileStubImpl;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public final class Angular2MetadataFileType extends MetadataJsonFileType {

  public static final Angular2MetadataFileType INSTANCE = new Angular2MetadataFileType();
  private static final IStubFileElementType<MetadataFileStubImpl> FILE = new MetadataStubFileElementType(MetadataJsonLanguage.INSTANCE);

  @NonNls public static final String METADATA_SUFFIX = ".metadata.json";
  @NonNls public static final String D_TS_SUFFIX = ".d.ts";

  private Angular2MetadataFileType() {
  }

  @Override
  public boolean isMyFileType(@NotNull VirtualFile file) {
    final CharSequence fileName = file.getNameSequence();
    final VirtualFile parent;
    if (StringUtil.endsWith(fileName, METADATA_SUFFIX)
        && (parent = file.getParent()) != null
        && parent.isValid()) {
      final VirtualFile sibling = parent.findChild(fileName.subSequence(0, fileName.length() - METADATA_SUFFIX.length()) + D_TS_SUFFIX);
      return sibling != null;
    }
    return false;
  }

  @Override
  public @NotNull String getName() {
    return "Angular Metadata JSON";
  }

  @Override
  public @NotNull String getDescription() {
    return Angular2Bundle.message("filetype.angular-metadata-json.description");
  }

  @Nls
  @Override
  public @NotNull String getDisplayName() {
    return Angular2Bundle.message("filetype.angular-metadata-json.display.name");
  }

  @Override
  public IStubFileElementType getFileElementType() {
    return FILE;
  }

  @Override
  protected void createRootStub(MetadataFileStubImpl fileStub, JsonValue jsonRoot) {
    new Angular2MetadataNodeModuleStub(fileStub, jsonRoot);
  }
}
