/*
 * Copyright 2000-2015 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

  @Nullable
  @Override
  public String getFileSample() {
    return OsmorcBundle.message("bnd.import.project.sample");
  }
}
