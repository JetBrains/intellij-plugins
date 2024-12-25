// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.osmorc.inspection;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.lang.manifest.psi.Header;
import org.jetbrains.lang.manifest.psi.HeaderValue;
import org.jetbrains.lang.manifest.psi.HeaderValuePart;
import org.jetbrains.osgi.project.BundleManifest;
import org.jetbrains.osgi.project.BundleManifestCache;
import org.osgi.framework.Constants;
import org.osmorc.facet.OsmorcFacet;
import org.osmorc.i18n.OsmorcBundle;
import org.osmorc.manifest.lang.psi.Clause;
import org.osmorc.util.OsgiPsiUtil;

/**
 * @author Vladislav.Soroka
 */
public final class WrongImportPackageInspection extends AbstractOsgiVisitor {
  @Override
  protected @NotNull PsiElementVisitor buildVisitor(OsmorcFacet facet, final ProblemsHolder holder, boolean isOnTheFly) {
    return new PsiElementVisitor() {
      @Override
      public void visitElement(@NotNull PsiElement element) {
        if (OsgiPsiUtil.isHeader(element, Constants.IMPORT_PACKAGE)) {
          nextValue:
          for (HeaderValue value : ((Header)element).getHeaderValues()) {
            if (value instanceof Clause) {
              HeaderValuePart valuePart = ((Clause)value).getValue();
              if (valuePart != null) {
                String packageName = valuePart.getUnwrappedText();
                packageName = StringUtil.trimEnd(packageName, ".*");
                if (StringUtil.isEmptyOrSpaces(packageName)) continue;

                PsiDirectory[] directories = OsgiPsiUtil.resolvePackage(element, packageName);
                if (directories.length == 0) continue;

                for (PsiDirectory directory : directories) {
                  BundleManifest manifest = BundleManifestCache.getInstance().getManifest(directory);
                  if (manifest != null && manifest.getExportedPackage(packageName) != null) {
                    continue nextValue;
                  }
                }

                String message = OsmorcBundle.message("WrongImportPackageInspection.message");
                TextRange range = valuePart.getHighlightingRange().shiftRight(-valuePart.getTextOffset());
                holder.registerProblem(valuePart, range, message);
              }
            }
          }
        }
      }
    };
  }
}
