/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.protobuf.ide.settings;

import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.protobuf.ide.settings.PbProjectSettings.ImportPathEntry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;

/**
 * A {@link ProjectSettingsConfigurator} that generates configuration based on project source roots.
 *
 * <p>The descriptor is set to <code>google/protobuf/descriptor.proto</code>.
 */
public final class DefaultConfigurator implements ProjectSettingsConfigurator {
  private static final String DESCRIPTOR = "google/protobuf/descriptor.proto";
  private static final Path TEMP_BUNDLED_PROTO_DIRECTORY = Paths.get(PathManager.getSystemPath()).resolve("protoeditor");

  public static Path getExtractedProtoPath() {
    return TEMP_BUNDLED_PROTO_DIRECTORY;
  }

  @Override
  public @NotNull PbProjectSettings configure(Project project, PbProjectSettings settings) {
    settings.setDescriptorPath(DESCRIPTOR);
    settings.getImportPathEntries().clear();
    VirtualFile[] roots = ProjectRootManager.getInstance(project).getContentSourceRoots();
    for (VirtualFile root : roots) {
      settings.getImportPathEntries().add(new ImportPathEntry(root.getUrl(), ""));
    }

    ImportPathEntry includeEntry = getBuiltInIncludeEntry();
    if (includeEntry != null) {
      settings.getImportPathEntries().add(includeEntry);
    }

    return settings;
  }

  @NotNull
  @Override
  public Collection<String> getDescriptorPathSuggestions(Project project) {
    return Collections.singletonList(DESCRIPTOR);
  }

  @Nullable
  ImportPathEntry getBuiltInIncludeEntry() {
    // Assume that the migration was successfully performed in the post startup activity, return null otherwise
    File extractedProtoFile = getExtractedProtoPath().toFile();
    if (!extractedProtoFile.exists()) {
      Logger.getInstance(DefaultConfigurator.class)
        .warn("Bundled proto files directory is not found in local file system or is not accessible");
      return null;
    }
    String url = VfsUtilCore.pathToUrl(extractedProtoFile.getPath());
    return new ImportPathEntry(url, null);
  }
}
