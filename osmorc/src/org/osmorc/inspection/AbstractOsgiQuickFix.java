/*
 * Copyright 2000-2015 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.osmorc.inspection;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.refactoring.util.CommonRefactoringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.lang.manifest.psi.ManifestFile;
import org.osmorc.facet.OsmorcFacet;
import org.osmorc.facet.OsmorcFacetConfiguration;
import org.osmorc.i18n.OsmorcBundle;

public abstract class AbstractOsgiQuickFix implements LocalQuickFix {
  @NotNull
  @Override
  public final String getFamilyName() {
    return OsmorcBundle.message("inspection.group");
  }

  @Override
  public boolean startInWriteAction() {
    return false;
  }

  @Nullable
  protected ManifestFile getVerifiedManifestFile(@NotNull PsiElement element) {
    Module module = ModuleUtilCore.findModuleForPsiElement(element);
    assert module != null : element;

    OsmorcFacet facet = OsmorcFacet.getInstance(module);
    if (facet != null) {
      OsmorcFacetConfiguration configuration = facet.getConfiguration();
      for (VirtualFile root : ModuleRootManager.getInstance(module).getContentRoots()) {
        VirtualFile file = root.findFileByRelativePath(configuration.getManifestLocation());
        if (file != null) {
          PsiFile psiFile = element.getManager().findFile(file);
          if (psiFile instanceof ManifestFile && CommonRefactoringUtil.checkReadOnlyStatus(psiFile)) {
            return (ManifestFile)psiFile;
          }
        }
      }
    }

    String message = OsmorcBundle.message("inspection.fix.no.manifest");
    Notifications.Bus.notify(new Notification("osmorc", getFamilyName(), message, NotificationType.WARNING));
    return null;
  }
}
