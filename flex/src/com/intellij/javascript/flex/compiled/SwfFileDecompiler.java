// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.javascript.flex.compiled;

import com.intellij.lang.javascript.flex.importer.FlexImporter;
import com.intellij.openapi.fileTypes.BinaryFileDecompiler;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.util.text.Strings;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ArrayUtil;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * @author Maxim.Mossienko
 */
public final class SwfFileDecompiler implements BinaryFileDecompiler {
  @Override
  public @NotNull CharSequence decompile(final @NotNull VirtualFile file) {
    Project project = ArrayUtil.getFirstElement(ProjectManager.getInstance().getOpenProjects());
    try {
      return project != null ? FlexImporter.buildInterfaceFromStream(file.getInputStream()) : "";
    }
    catch (IOException ex) {
      return Strings.EMPTY_CHAR_SEQUENCE;
    }
  }
}