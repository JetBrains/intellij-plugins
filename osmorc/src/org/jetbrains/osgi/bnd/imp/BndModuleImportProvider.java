// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.osgi.bnd.imp;

import aQute.bnd.build.Workspace;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.projectImport.ProjectImportProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osmorc.i18n.OsmorcBundle;

import java.io.File;

public final class BndModuleImportProvider extends ProjectImportProvider {
  public BndModuleImportProvider() {
    super(new BndProjectImportBuilder());
  }

  @Override
  public boolean canImport(@NotNull VirtualFile fileOrDir, @Nullable Project project) {
    Workspace ws = BndProjectImporter.getWorkspace(project);
    if (ws == null) return false;

    File projectDir = fileOrDir.isDirectory() ? new File(fileOrDir.getPath()) : new File(fileOrDir.getPath()).getParentFile();
    return FileUtil.filesEqual(ws.getBase(), projectDir.getParentFile()) &&
           projectDir.isDirectory() &&
           new File(projectDir, BndProjectImporter.BND_FILE).isFile();
  }

  @Override
  public boolean canCreateNewProject() {
    return false;
  }

  @Override
  public @Nullable String getFileSample() {
    return OsmorcBundle.message("bnd.import.project.sample");
  }
}
