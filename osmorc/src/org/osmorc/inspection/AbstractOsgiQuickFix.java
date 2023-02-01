// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.osmorc.inspection;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.psi.PsiElement;
import com.intellij.refactoring.util.CommonRefactoringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.lang.manifest.psi.ManifestFile;
import org.osmorc.facet.OsmorcFacet;
import org.osmorc.i18n.OsmorcBundle;

public abstract class AbstractOsgiQuickFix implements LocalQuickFix {
  @Override
  public final @NotNull String getFamilyName() {
    return OsmorcBundle.message("inspection.group");
  }

  @Override
  public boolean startInWriteAction() {
    return false;
  }

  protected @Nullable ManifestFile getVerifiedManifestFile(@NotNull PsiElement element) {
    var manifestFile = getManifestFile(element);
    if (manifestFile != null && CommonRefactoringUtil.checkReadOnlyStatus(manifestFile)) {
      return manifestFile;
    }
    else {
      OsmorcBundle.notification(getFamilyName(), OsmorcBundle.message("inspection.fix.no.manifest"), NotificationType.WARNING).notify(element.getProject());
      return null;
    }
  }

  protected @Nullable ManifestFile getManifestFile(@NotNull PsiElement element) {
    var module = ModuleUtilCore.findModuleForPsiElement(element);
    assert module != null : element;

    var facet = OsmorcFacet.getInstance(module);
    if (facet != null) {
      var configuration = facet.getConfiguration();
      for (var root : ModuleRootManager.getInstance(module).getContentRoots()) {
        var file = root.findFileByRelativePath(configuration.getManifestLocation());
        if (file != null) {
          var psiFile = element.getManager().findFile(file);
          if (psiFile instanceof ManifestFile) {
            return (ManifestFile)psiFile;
          }
        }
      }
    }

    return null;
  }
}
