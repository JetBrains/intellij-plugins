/*
 * Copyright (c) 2007-2009, Osmorc Development Team
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright notice, this list
 *       of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright notice, this
 *       list of conditions and the following disclaimer in the documentation and/or other
 *       materials provided with the distribution.
 *     * Neither the name of 'Osmorc Development Team' nor the names of its contributors may be
 *       used to endorse or promote products derived from this software without specific
 *       prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT
 * OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.osmorc.inspection;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;
import org.osmorc.BundleManager;
import org.osmorc.facet.OsmorcFacet;
import org.osmorc.i18n.OsmorcBundle;
import org.osmorc.manifest.BundleManifest;

/**
 * Inspection which checks if a package of a class is accessible inside the OSGi context.
 *
 * @author <a href="mailto:janthomae@janthomae.de">Jan Thom&auml;</a>
 * @author Robert F. Beeger (robert@beeger.net)
 */
public class PackageAccessibilityInspection extends LocalInspectionTool {
  @NotNull
  @Override
  public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
    return new JavaElementVisitor() {
      private final BundleManager myBundleManager = ServiceManager.getService(holder.getProject(), BundleManager.class);

      @Override
      public void visitReferenceElement(PsiJavaCodeReferenceElement reference) {
        checkReference(reference);
      }

      @Override
      public void visitImportStaticReferenceElement(PsiImportStaticReferenceElement reference) {
        checkReference(reference);
      }

      @Override
      public void visitReferenceExpression(PsiReferenceExpression expression) {
        checkReference(expression);
      }

      private void checkReference(PsiJavaCodeReferenceElement ref) {
        OsmorcFacet facet = OsmorcFacet.getInstance(ref);
        if (facet != null && facet.getConfiguration().isManifestManuallyEdited()) {
          PsiElement target = ref.resolve();
          if (target instanceof PsiClass && !isAccessible((PsiClass)target, facet.getModule())) {
            holder.registerProblem(ref, OsmorcBundle.message("PackageAccessibilityInspection.message"));
          }
        }
      }

      // OSGi Core Spec 3.5 "Class Loading Architecture"
      private boolean isAccessible(PsiClass aClass, Module requestorModule) {
        // The parent class loader (normally java.* packages from the boot class path)
        // todo[r.sh] allow to specify boot delegation packages
        String packageName = ((PsiJavaFile)aClass.getContainingFile()).getPackageName();
        if (packageName.isEmpty() || packageName.startsWith("java.")) {
          return true;
        }

        // The bundle's class path (private packages)
        // todo[r.sh] need to check actual bundle classpath
        Module targetModule = ModuleUtilCore.findModuleForPsiElement(aClass);
        if (targetModule == requestorModule) {
          return true;
        }

        BundleManifest manifest = myBundleManager.getManifestByObject(requestorModule);
        if (manifest != null) {
          // Imported packages
          if (manifest.isPackageImported(packageName)) {
            return true;
          }

          // Required bundles
          for (String bundleSpec : manifest.getRequiredBundles()) {
            BundleManifest bundle = myBundleManager.getManifestByBundleSpec(bundleSpec);
            if (bundle != null && bundle.isPackageExported(packageName)) {
              return true;
            }
          }

          // Attached fragments [AFAIK these should not be linked statically - r.sh]
        }

        return false;
      }
    };
  }
}
