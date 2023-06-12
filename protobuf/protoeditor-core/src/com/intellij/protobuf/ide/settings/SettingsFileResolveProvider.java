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
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.protobuf.ide.settings.PbProjectSettings.ImportPathEntry;
import com.intellij.protobuf.lang.resolve.FileResolveProvider;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.GlobalSearchScopesCore;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.intellij.protobuf.ide.settings.PbImportPathsConfiguration.BUNDLED_DESCRIPTOR;
import static com.intellij.protobuf.ide.settings.PbImportPathsConfiguration.computeDeterministicImportPathsStream;


/** {@link FileResolveProvider} implementation that uses settings from {@link PbProjectSettings}. */
public class SettingsFileResolveProvider implements FileResolveProvider {

  /** No-op constructor. Settings will be resolved by looking up the PbProjectSettings service. */
  public SettingsFileResolveProvider() { }

  @Nullable
  @Override
  public VirtualFile findFile(@NotNull String path, @NotNull Project project) {
    return findFileWithSettings(path, project, PbProjectSettings.getInstance(project));
  }

  @NotNull
  @Override
  public Collection<ChildEntry> getChildEntries(@NotNull String path, @NotNull Project project) {
    Set<ChildEntry> results = new HashSet<>();
    for (ImportPathEntry entry : getImportPaths(project, PbProjectSettings.getInstance(project))) {
      String prefix = normalizePath(entry.getPrefix());
      path = normalizePath(path);

      if (prefix.startsWith(path) && !prefix.equals(path)) {
        String nextPrefixComponent = prefix.substring(path.length()).split("/")[0];
        results.add(ChildEntry.directory(nextPrefixComponent));
      }
      else if (path.startsWith(prefix)) {
        // defer to the backing filesystem.
        VirtualFile location = VirtualFileManager.getInstance().findFileByUrl(entry.getLocation());
        if (location == null) {
          continue;
        }

        String unprefixedPath = path.substring(prefix.length());
        VirtualFile pathFile = location.findFileByRelativePath(unprefixedPath);
        if (pathFile == null) {
          continue;
        }

        VirtualFile[] children = pathFile.getChildren();
        if (children == null) {
          continue;
        }

        for (VirtualFile child : children) {
          if (PROTO_AND_DIRECTORY_FILTER.accept(child)) {
            results.add(new ChildEntry(child.getName(), child.isDirectory()));
          }
        }
      }
    }
    return results;
  }

  @Nullable
  @Override
  public VirtualFile getDescriptorFile(@NotNull Project project) {
    return findFile(BUNDLED_DESCRIPTOR, project);
  }

  @NotNull
  @Override
  public GlobalSearchScope getSearchScope(@NotNull Project project) {
    VirtualFile[] roots =
      computeDeterministicImportPathsStream(project, PbProjectSettings.getInstance(project))
        .map(ImportPathEntry::getLocation)
        .map(VirtualFileManager.getInstance()::findFileByUrl)
        .filter(Objects::nonNull)
        .toArray(VirtualFile[]::new);
    return GlobalSearchScopesCore.directoriesScope(project, /* withSubDirectories= */ true, roots);
  }

  private static List<ImportPathEntry> getImportPaths(Project project, PbProjectSettings pbSettings) {
    return computeDeterministicImportPathsStream(project, pbSettings).toList();
  }

  static VirtualFile findFileWithSettings(String path, Project project, PbProjectSettings settings) {
    for (ImportPathEntry entry : getImportPaths(project, settings)) {
      if (entry == null) continue;
      String prefix = normalizePath(entry.getPrefix());
      if (!path.startsWith(prefix)) {
        continue;
      }

      VirtualFile location = VirtualFileManager.getInstance().findFileByUrl(entry.getLocation());
      if (location == null) {
        continue;
      }
      String unprefixedPath = path.substring(prefix.length());
      VirtualFile imported = location.findFileByRelativePath(unprefixedPath);
      if (imported != null && PROTO_FILTER.accept(imported)) {
        return imported;
      }
    }
    return null;
  }

  /** For the given path, return a non-null string that, if not empty, ends with a slash. */
  private static String normalizePath(@Nullable String path) {
    if (path == null) {
      return "";
    }
    if (!path.isEmpty() && !path.endsWith("/")) {
      return path + "/";
    }
    return path;
  }
}
