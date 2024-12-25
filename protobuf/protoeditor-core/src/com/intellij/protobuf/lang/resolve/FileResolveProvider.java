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

import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileFilter;
import com.intellij.protobuf.lang.PbFileType;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Objects;

/** An extension interface that resolves import protobuf files and descriptors. */
public interface FileResolveProvider {
  ExtensionPointName<FileResolveProvider> EP_NAME =
      ExtensionPointName.create("com.intellij.protobuf.fileResolveProvider");

  VirtualFileFilter PROTO_AND_DIRECTORY_FILTER = file -> file.isDirectory() || file.getFileType() instanceof PbFileType;

  VirtualFileFilter PROTO_FILTER = file -> file.getFileType() instanceof PbFileType;

  /** A class representing a child element, and whether it is a directory. */
  class ChildEntry {
    private final boolean isDirectory;
    private final String name;

    public ChildEntry(@NotNull String name, boolean isDirectory) {
      this.name = name;
      this.isDirectory = isDirectory;
    }

    public boolean isDirectory() {
      return isDirectory;
    }

    public String getName() {
      return name;
    }

    @Override
    public String toString() {
      return name;
    }

    @Override
    public boolean equals(Object obj) {
      if (!(obj instanceof ChildEntry other)) {
        return false;
      }
      return Objects.equals(name, other.name) && Objects.equals(isDirectory, other.isDirectory);
    }

    @Override
    public int hashCode() {
      return Objects.hash(name, isDirectory);
    }

    public static ChildEntry file(String name) {
      return new ChildEntry(name, false);
    }

    public static ChildEntry directory(String name) {
      return new ChildEntry(name, true);
    }
  }

  /**
   * Find the file with the given path, scoped to the given project.
   *
   * @param path The path to find.
   * @param project The project whose child is performing the search.
   * @return The {@link VirtualFile} representing the located path, or <code>null</code>.
   */
  @Nullable
  VirtualFile findFile(@NotNull String path, @NotNull Project project);

  /**
   * Find the file with the given path, scoped to the given module.
   *
   * @param path The path to find.
   * @param module The module whose child is performing the search.
   * @return The {@link VirtualFile} representing the located path, or <code>null</code>.
   */
  default @Nullable VirtualFile findFile(@NotNull String path, @NotNull Module module) {
    return findFile(path, module.getProject());
  }

  /**
   * Find children of the given path, filtering to files that should be accessible from the given
   * project. If the path does not represent a directory, return an empty list.
   *
   * <p>Only directories and protobuf files are returned.
   *
   * @param path The path to find.
   * @param project The project whose child is performing the search.
   * @return A list of child proto file and directory names.
   */
  @NotNull
  Collection<ChildEntry> getChildEntries(@NotNull String path, @NotNull Project project);

  /**
   * Find children of the given path, filtering to files that should be accessible from the given
   * module. If the path does not represent a directory, return an empty list.
   *
   * <p>Only directories and protobuf files are returned.
   *
   * @param path The path to find.
   * @param module The module whose child is performing the search.
   * @return A list of child proto file and directory names.
   */
  default @NotNull Collection<ChildEntry> getChildEntries(@NotNull String path, @NotNull Module module) {
    return getChildEntries(path, module.getProject());
  }

  /**
   * Return the {@link VirtualFile} of the <code>descriptor.proto</code> file to use.
   *
   * @param project The project whose child is performing the search.
   * @return The descriptor file, or <code>null</code> if it cannot be found.
   */
  @Nullable
  VirtualFile getDescriptorFile(@NotNull Project project);

  /**
   * Return the {@link VirtualFile} of the <code>descriptor.proto</code> file to use.
   *
   * @param module The module whose child is performing the search.
   * @return The descriptor file, or <code>null</code> if it cannot be found.
   */
  default @Nullable VirtualFile getDescriptorFile(@NotNull Module module) {
    return getDescriptorFile(module.getProject());
  }

  /**
   * Returns <code>true</code> if it's possible for this {@link FileResolveProvider} to find the
   * given file. I.e., if the file exists somewhere within whatever roots the provider considers.
   *
   * @param project the project
   * @param file the file to find
   * @return <code>true</code> if the file can be resolved by this provider.
   */
  default boolean canFindFile(@NotNull Project project, @NotNull VirtualFile file) {
    return getSearchScope(project).contains(file);
  }

  /**
   * Returns a {@link GlobalSearchScope} covering the files that can be found by this provider.
   *
   * @param project the project
   * @return a {@link GlobalSearchScope}.
   */
  @NotNull
  GlobalSearchScope getSearchScope(@NotNull Project project);
}
