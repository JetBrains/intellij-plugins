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
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.protobuf.lang.psi.PbFile;
import com.intellij.protobuf.lang.resolve.FileResolveProvider.ChildEntry;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;

/** A helper class for finding files given path names. */
public final class PbFileResolver {

  @NotNull
  public static List<PbFile> findFilesForContext(
      @NotNull String path, @NotNull PsiElement context) {
    Module module = ModuleUtilCore.findModuleForPsiElement(context);
    if (module != null) {
      return findFilesInModule(path, module);
    } else {
      return findFilesInProject(path, context.getProject());
    }
  }

  @NotNull
  public static List<PbFile> findFilesInModule(@NotNull String path, @NotNull Module module) {
    if (!isValidImportPath(path)) {
      return Collections.emptyList();
    }
    return findFiles(module.getProject(), (provider) -> provider.findFile(path, module));
  }

  @NotNull
  public static List<PbFile> findFilesInProject(@NotNull String path, @NotNull Project project) {
    if (!isValidImportPath(path)) {
      return Collections.emptyList();
    }
    return findFiles(project, (provider) -> provider.findFile(path, project));
  }

  @NotNull
  public static Collection<ChildEntry> getChildNamesForContext(
      @NotNull String path, @NotNull PsiElement context) {
    Module module = ModuleUtilCore.findModuleForPsiElement(context);
    if (module != null) {
      return getChildNamesInModule(path, module);
    } else {
      return getChildNamesInProject(path, context.getProject());
    }
  }

  @NotNull
  public static Collection<ChildEntry> getChildNamesInModule(
      @NotNull String path, @NotNull Module module) {
    if (!isValidImportPath(path)) {
      return Collections.emptyList();
    }
    return getChildEntries(module.getProject(), provider -> provider.getChildEntries(path, module));
  }

  @NotNull
  public static Collection<ChildEntry> getChildNamesInProject(
      @NotNull String path, @NotNull Project project) {
    if (!isValidImportPath(path)) {
      return Collections.emptyList();
    }
    return getChildEntries(project, provider -> provider.getChildEntries(path, project));
  }

  public static boolean isValidImportPath(@NotNull String path) {
    return !(path.contains("//")
        || path.contains("\\")
        || path.contains("/./")
        || path.contains("/../")
        || path.startsWith("./")
        || path.startsWith("../")
        || path.endsWith("/.")
        || path.endsWith("/..")
        || path.equals(".")
        || path.equals(".."));
  }

  @NotNull
  static FileResolveProvider @NotNull[] getProviders(@NotNull Project project) {
    return FileResolveProvider.EP_NAME.getExtensions(project);
  }

  static GlobalSearchScope getUnionScope(@NotNull Project project) {
    GlobalSearchScope scope = GlobalSearchScope.EMPTY_SCOPE;
    for (FileResolveProvider provider : getProviders(project)) {
      scope = scope.union(provider.getSearchScope(project));
    }
    return scope;
  }

  @NotNull
  private static List<PbFile> findFiles(
      Project project, Function<FileResolveProvider, VirtualFile> fn) {
    Set<PbFile> results = new LinkedHashSet<>();

    for (FileResolveProvider provider : getProviders(project)) {
      VirtualFile file = fn.apply(provider);
      if (file == null || !file.exists()) {
        continue;
      }
      PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
      if (psiFile instanceof PbFile) {
        results.add((PbFile) psiFile);
      }
    }
    return new ArrayList<>(results);
  }

  @NotNull
  private static Collection<ChildEntry> getChildEntries(
      Project project, Function<FileResolveProvider, Collection<ChildEntry>> fn) {
    Set<ChildEntry> results = new LinkedHashSet<>();

    for (FileResolveProvider provider : getProviders(project)) {
      results.addAll(fn.apply(provider));
    }
    return results;
  }
}
