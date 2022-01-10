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

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/** {@link FileResolveProvider} implementation that uses settings from {@link PbProjectSettings}. */
public class SettingsFileResolveProvider implements FileResolveProvider {

  private final PbProjectSettings staticSettings;

  /** No-op constructor. Settings will be resolved by looking up the PbProjectSettings service. */
  public SettingsFileResolveProvider() {
    this.staticSettings = null;
  }

  /**
   * Constructs a SettingsFileResolveProvider backed by the static {@link PbProjectSettings} object.
   * Updates to the configuration in the {@link PbProjectSettings} service will not be reflected.
   * This constructor can be used to preview the effects of settings that have not yet been
   * persisted.
   *
   * @param staticSettings A {@link PbProjectSettings} object to use.
   */
  public SettingsFileResolveProvider(PbProjectSettings staticSettings) {
    this.staticSettings = staticSettings;
  }

  @Nullable
  @Override
  public VirtualFile findFile(@NotNull String path, @NotNull Project project) {
    PbProjectSettings settings = getSettings(project);
    for (ImportPathEntry entry : settings.getImportPathEntries()) {
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

  @NotNull
  @Override
  public Collection<ChildEntry> getChildEntries(@NotNull String path, @NotNull Project project) {
    PbProjectSettings settings = getSettings(project);
    Set<ChildEntry> results = new HashSet<>();
    for (ImportPathEntry entry : settings.getImportPathEntries()) {
      String prefix = normalizePath(entry.getPrefix());
      path = normalizePath(path);

      if (prefix.startsWith(path) && !prefix.equals(path)) {
        String nextPrefixComponent = prefix.substring(path.length()).split("/")[0];
        results.add(ChildEntry.directory(nextPrefixComponent));
      } else if (path.startsWith(prefix)) {
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
    PbProjectSettings settings = getSettings(project);
    String descriptorPath = settings.getDescriptorPath();
    if (descriptorPath != null) {
      return findFile(descriptorPath, project);
    }
    return null;
  }

  @NotNull
  @Override
  public GlobalSearchScope getSearchScope(@NotNull Project project) {
    PbProjectSettings settings = getSettings(project);
    VirtualFile[] roots =
        settings
            .getImportPathEntries()
            .stream()
            .map(ImportPathEntry::getLocation)
            .map(VirtualFileManager.getInstance()::findFileByUrl)
            .filter(Objects::nonNull)
            .toArray(VirtualFile[]::new);
    return GlobalSearchScopesCore.directoriesScope(project, /* withSubDirectories= */ true, roots);
  }

  @NotNull
  private PbProjectSettings getSettings(Project project) {
    if (staticSettings != null) {
      return staticSettings;
    }
    return PbProjectSettings.getInstance(project);
  }

  /** For the given path, return a non-null string that, if not empty, ends with a slash. */
  private String normalizePath(@Nullable String path) {
    // TODO(volkman): better way to handle multi-platform differences with filesystem separators?
    if (path == null) {
      return "";
    }
    if (!path.isEmpty() && !path.endsWith("/")) {
      return path + "/";
    }
    return path;
  }
}
