// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package com.intellij.jhipster.uml.model;

import com.intellij.jhipster.JdlIconsMapping;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.pointers.VirtualFilePointer;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;

public final class JdlDiagramRootData implements JdlNodeData {

  private final VirtualFilePointer virtualFile;
  private final String name;

  public JdlDiagramRootData(VirtualFilePointer virtualFile) {
    this.virtualFile = virtualFile;
    this.name = virtualFile.getFileName();
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public Icon getIcon() {
    return JdlIconsMapping.FILE_ICON;
  }

  public @Nullable VirtualFile getVirtualFile() {
    return virtualFile.getFile();
  }
}
