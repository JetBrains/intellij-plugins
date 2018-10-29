// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities.metadata;

import com.intellij.json.psi.JsonValue;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.tree.IStubFileElementType;
import org.angular2.entities.metadata.stubs.Angular2MetadataNodeModuleStub;
import org.angular2.lang.metadata.MetadataJsonFileType;
import org.angular2.lang.metadata.stubs.MetadataFileStubImpl;
import org.jetbrains.annotations.NotNull;

public class Angular2MetadataFileType extends MetadataJsonFileType {

  public static final Angular2MetadataFileType INSTANCE = new Angular2MetadataFileType();

  private static final String METADATA_SUFFIX = ".metadata.json";
  private static final String D_TS_SUFFIX = ".d.ts";

  @Override
  public boolean isMyFileType(@NotNull VirtualFile file) {
    final String fileName = file.getName();
    final VirtualFile parent;
    if (fileName.endsWith(METADATA_SUFFIX)
        && (parent = file.getParent()) != null) {
      final VirtualFile sibling = parent.findChild(fileName.substring(0, fileName.length() - METADATA_SUFFIX.length()) + D_TS_SUFFIX);
      return sibling != null;
    }
    return false;
  }

  @NotNull
  @Override
  public String getName() {
    return "Angular Metadata JSON";
  }

  @NotNull
  @Override
  public String getDescription() {
    return "Angular Metadata JSON";
  }

  @Override
  public IStubFileElementType getFileElementType() {
    return Angular2MetadataElementTypes.FILE;
  }

  @Override
  protected void createRootStub(MetadataFileStubImpl fileStub, JsonValue jsonRoot) {
    new Angular2MetadataNodeModuleStub(fileStub, jsonRoot);
  }
}
