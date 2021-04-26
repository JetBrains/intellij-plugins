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
package com.intellij.protobuf.lang.descriptor;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.util.CachedValueProvider.Result;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiModificationTracker;
import com.intellij.protobuf.lang.psi.PbFile;
import com.intellij.protobuf.lang.resolve.FileResolveProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** A class used to find the <code>descriptor.proto</code> file for a project. */
public class Descriptor {

  private final PbFile file;

  private Descriptor(PbFile file) {
    this.file = file;
  }

  /**
   * Returns the descriptor {@link PbFile} object.
   *
   * @return the descriptor {@link PbFile} object.
   */
  @NotNull
  public PbFile getFile() {
    return file;
  }

  /**
   * Finds the descriptor for the given project.
   *
   * <p>The descriptor file is determined by iterating over all registered {@link
   * FileResolveProvider} extensions and returning the first non-null result from {@link
   * FileResolveProvider#getDescriptorFile(Project)}.
   *
   * <p>If no descriptor is found, <code>null</code> is returned.
   *
   * @param project The {@link Project}.
   * @param module The module whose child is performing the lookup. Possibly <code>null</code>.
   * @return A {@link Descriptor} object wrapping the proto descriptor file, or <code>null</code>.
   */
  @Nullable
  private static Descriptor locate(@NotNull Project project, @Nullable Module module) {
    for (FileResolveProvider provider : FileResolveProvider.EP_NAME.getExtensionList(project)) {
      VirtualFile file =
          module != null ? provider.getDescriptorFile(module) : provider.getDescriptorFile(project);
      if (file != null) {
        PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
        if (psiFile instanceof PbFile) {
          return new Descriptor((PbFile) psiFile);
        }
      }
    }
    return null;
  }

  /** Finds the descriptor for the given file. */
  @Nullable
  public static Descriptor locate(@NotNull PbFile file) {
    return CachedValuesManager.getCachedValue(
        file,
        () ->
            Result.create(
                Descriptor.locate(file.getProject(), ModuleUtilCore.findModuleForPsiElement(file)),
                PsiModificationTracker.MODIFICATION_COUNT));
  }
}
