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
package com.intellij.protobuf.lang.resolve;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.GlobalSearchScopesCore;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/** A {@link FileResolveProvider} that simply looks at project roots. Used mainly for testing. */
public class LocalRootsFileResolveProvider implements FileResolveProvider {

  private final String descriptorPath;

  public LocalRootsFileResolveProvider(String descriptorPath) {
    this.descriptorPath = descriptorPath;
  }

  @Nullable
  @Override
  public VirtualFile findFile(@NotNull String path, @NotNull Project project) {
    return findFileInRoots(path, getProjectRoots(project));
  }

  @Nullable
  @Override
  public VirtualFile findFile(@NotNull String path, @NotNull Module module) {
    return findFileInRoots(path, getModuleRoots(module));
  }

  @NotNull
  @Override
  public List<ChildEntry> getChildEntries(@NotNull String path, @NotNull Project project) {
    return getChildEntriesForFile(findFile(path, project));
  }

  @NotNull
  @Override
  public List<ChildEntry> getChildEntries(@NotNull String path, @NotNull Module module) {
    return getChildEntriesForFile(findFile(path, module));
  }

  @Nullable
  @Override
  public VirtualFile getDescriptorFile(@NotNull Project project) {
    if (descriptorPath != null) {
      return findFile(descriptorPath, project);
    }
    return null;
  }

  @Nullable
  @Override
  public VirtualFile getDescriptorFile(@NotNull Module module) {
    if (descriptorPath != null) {
      return findFile(descriptorPath, module);
    }
    return null;
  }

  @NotNull
  @Override
  public GlobalSearchScope getSearchScope(@NotNull Project project) {
    return GlobalSearchScopesCore.directoriesScope(
        project, /* withSubDirectories= */ true, getProjectRoots(project));
  }

  private List<ChildEntry> getChildEntriesForFile(VirtualFile file) {
    if (file != null && file.isDirectory()) {
      return VfsUtil.getChildren(file, PROTO_AND_DIRECTORY_FILTER)
          .stream()
          .map(child -> new ChildEntry(child.getName(), child.isDirectory()))
          .collect(Collectors.toList());
    }
    return Collections.emptyList();
  }

  private VirtualFile findFileInRoots(String path, VirtualFile[] roots) {
    for (VirtualFile root : roots) {
      VirtualFile file = root.findFileByRelativePath(path);
      if (file != null && file.exists()) {
        return file;
      }
    }
    return null;
  }

  private VirtualFile[] getProjectRoots(@NotNull Project project) {
    return ProjectRootManager.getInstance(project).getContentRoots();
  }

  private VirtualFile[] getModuleRoots(@NotNull Module module) {
    return ModuleRootManager.getInstance(module).getContentRoots();
  }
}
