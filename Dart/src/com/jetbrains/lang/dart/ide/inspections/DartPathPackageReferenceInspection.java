// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.lang.dart.ide.inspections;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.progress.ProgressIndicatorProvider;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFileSystemItem;
import com.intellij.psi.PsiReference;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReference;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.psi.PubspecYamlReferenceContributor;
import com.jetbrains.lang.dart.sdk.DartSdk;
import com.jetbrains.lang.dart.sdk.DartSdkLibUtil;
import com.jetbrains.lang.dart.util.DartResolveUtil;
import com.jetbrains.lang.dart.util.PubspecYamlUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.yaml.psi.YAMLKeyValue;
import org.jetbrains.yaml.psi.YAMLValue;

public final class DartPathPackageReferenceInspection extends LocalInspectionTool {
  @Override
  public @NotNull PsiElementVisitor buildVisitor(final @NotNull ProblemsHolder holder, final boolean isOnTheFly) {
    if (!PubspecYamlUtil.PUBSPEC_YAML.equals(holder.getFile().getName())) return super.buildVisitor(holder, isOnTheFly);

    final Module module = ModuleUtilCore.findModuleForPsiElement(holder.getFile());
    final DartSdk sdk = DartSdk.getDartSdk(holder.getProject());
    if (module == null || sdk == null || !DartSdkLibUtil.isDartSdkEnabled(module)) {
      return super.buildVisitor(holder, isOnTheFly);
    }

    return new PsiElementVisitor() {
      @Override
      public void visitElement(final @NotNull PsiElement element) {
        ProgressIndicatorProvider.checkCanceled();

        if (!(element instanceof YAMLKeyValue) ||
            !PubspecYamlReferenceContributor.isPathPackageDefinition((YAMLKeyValue)element)) {
          return;
        }

        YAMLValue yamlValue = ((YAMLKeyValue)element).getValue();
        if (yamlValue == null) {
          return;
        }

        final VirtualFile packageDir = checkReferences(holder, (YAMLKeyValue)element);
        if (packageDir == null) {
          return;
        }

        if (packageDir.findChild(PubspecYamlUtil.PUBSPEC_YAML) == null) {
          final String message = DartBundle.message("pubspec.yaml.not.found.in", FileUtil.toSystemDependentName(packageDir.getPath()));
          holder.registerProblem(yamlValue, message);
          return;
        }

        final VirtualFile file = DartResolveUtil.getRealVirtualFile(element.getContainingFile());
        if (file != null && packageDir.equals(file.getParent())) {
          holder.registerProblem(yamlValue, DartBundle.message("path.package.reference.to.itself"));
        }
      }
    };
  }

  private static @Nullable VirtualFile checkReferences(final @NotNull ProblemsHolder holder, final @NotNull YAMLKeyValue element) {
    for (PsiReference reference : element.getReferences()) {
      if (reference instanceof FileReference && !reference.isSoft()) {
        final PsiFileSystemItem resolve = ((FileReference)reference).resolve();
        if (resolve == null) {
          holder.registerProblem(reference.getElement(), ((FileReference)reference).getUnresolvedMessagePattern(),
                                 ProblemHighlightType.GENERIC_ERROR, reference.getRangeInElement());
          return null;
        }
        else if (((FileReference)reference).isLast()) {
          final VirtualFile dir = resolve.getVirtualFile();
          if (dir != null && dir.isDirectory()) {
            return dir;
          }
        }
      }
    }

    return null;
  }
}
