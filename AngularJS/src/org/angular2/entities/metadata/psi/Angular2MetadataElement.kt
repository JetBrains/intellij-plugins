// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities.metadata.psi;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.stubs.StubElement;
import org.angular2.entities.metadata.stubs.Angular2MetadataElementStub;
import org.angular2.entities.metadata.stubs.Angular2MetadataNodeModuleStub;
import org.angular2.lang.metadata.psi.MetadataElement;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.intellij.util.ObjectUtils.doIfNotNull;

public abstract class Angular2MetadataElement<Stub extends Angular2MetadataElementStub<?>> extends MetadataElement<Stub> {

  @NonNls private static final String INDEX_FILE_NAME = "index";

  public Angular2MetadataElement(@NotNull Stub element) {
    super(element);
  }

  public @Nullable Angular2MetadataNodeModule getNodeModule() {
    StubElement stub = getStub();
    while (stub != null && !(stub instanceof Angular2MetadataNodeModuleStub)) {
      stub = stub.getParentStub();
    }
    return stub != null ? (Angular2MetadataNodeModule)stub.getPsi() : null;
  }

  @Override
  public @Nullable String getText() {
    return "";
  }

  @Override
  public int getTextLength() {
    return 0;
  }

  protected PsiFile loadRelativeFile(@NotNull String path, @NotNull String extension) {
    return doIfNotNull(getContainingFile().getViewProvider().getVirtualFile().getParent(),
                       baseDir -> loadRelativeFile(baseDir, path, extension));
  }

  protected PsiFile loadRelativeFile(@NotNull VirtualFile baseDir, @NotNull String path, @NotNull String extension) {
    VirtualFile moduleFile = baseDir.findFileByRelativePath(path + extension);
    if (moduleFile != null) {
      return getManager().findFile(moduleFile);
    }
    VirtualFile moduleDir = baseDir.findFileByRelativePath(path);
    if (moduleDir == null || !moduleDir.isDirectory()) {
      return null;
    }
    return doIfNotNull(moduleDir.findChild(INDEX_FILE_NAME + extension), getManager()::findFile);
  }
}
