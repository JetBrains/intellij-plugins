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

import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.protobuf.ide.settings.PbProjectSettings.ImportPathEntry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URL;
import java.util.Collection;
import java.util.Collections;

/**
 * A {@link ProjectSettingsConfigurator} that generates configuration based on project source roots.
 *
 * <p>The descriptor is set to <code>google/protobuf/descriptor.proto</code>.
 */
public class DefaultConfigurator implements ProjectSettingsConfigurator {
  private static final String DESCRIPTOR = "google/protobuf/descriptor.proto";

  @Nullable
  @Override
  public PbProjectSettings configure(Project project, PbProjectSettings settings) {
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
  static ImportPathEntry getBuiltInIncludeEntry() {
    URL descriptorUrl = DefaultConfigurator.class.getResource("/include/google/protobuf/descriptor.proto");
    if (descriptorUrl == null) {
      return null;
    }
    VirtualFile descriptorFile = VfsUtil.findFileByURL(descriptorUrl);
    if (descriptorFile == null) {
      return null;
    }
    return new ImportPathEntry(descriptorFile.getParent().getParent().getParent().getUrl(), null);
  }
}
