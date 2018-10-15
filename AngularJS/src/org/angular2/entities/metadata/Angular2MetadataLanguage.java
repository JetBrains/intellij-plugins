// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities.metadata;

import com.intellij.json.psi.JsonValue;
import com.intellij.psi.tree.IStubFileElementType;
import org.angular2.entities.metadata.stubs.Angular2MetadataNodeModuleStub;
import org.angular2.lang.metadata.MetadataLanguage;
import org.angular2.lang.metadata.stubs.MetadataFileStubImpl;
import org.jetbrains.annotations.NotNull;

public class Angular2MetadataLanguage extends MetadataLanguage {

  public static final Angular2MetadataLanguage INSTANCE = new Angular2MetadataLanguage();

  protected Angular2MetadataLanguage() {
    super("Angular Metadata JSON");
  }

  @NotNull
  @Override
  public String getDisplayName() {
    return "Angular Metadata JSON";
  }

  @Override
  protected IStubFileElementType getFileElementType() {
    return Angular2MetadataElementTypes.FILE;
  }

  @Override
  public void createRootStub(MetadataFileStubImpl fileStub, JsonValue jsonRoot) {
    new Angular2MetadataNodeModuleStub(fileStub, jsonRoot);
  }
}
