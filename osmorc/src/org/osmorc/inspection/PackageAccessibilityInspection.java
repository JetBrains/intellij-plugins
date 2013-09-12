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
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.*;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.lang.manifest.psi.ManifestFile;
import org.osgi.framework.Constants;
import org.osmorc.BundleManager;
import org.osmorc.facet.OsmorcFacet;
import org.osmorc.i18n.OsmorcBundle;
import org.osmorc.manifest.BundleManifest;
import org.osmorc.util.OsgiPsiUtil;

import java.util.List;

/**
 * Inspection which checks if a package of a class is accessible inside the OSGi context.
 *
 * @author <a href="mailto:janthomae@janthomae.de">Jan Thom&auml;</a>
 * @author Robert F. Beeger (robert@beeger.net)
 */
public class PackageAccessibilityInspection extends LocalInspectionTool {
  private static final Logger LOG = Logger.getInstance(PackageAccessibilityInspection.class);

  @NotNull
  @Override
  public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
    return new JavaElementVisitor() {
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
          Module module = facet.getModule();
          if (target instanceof PsiClass && !isAccessible((PsiClass)target, module)) {
            LocalQuickFix fix = new ImportPackageFix();
            holder.registerProblem(ref, OsmorcBundle.message("PackageAccessibilityInspection.message"), fix);
          }
        }
      }

      // OSGi Core Spec 3.5 "Class Loading Architecture"
      private boolean isAccessible(PsiClass aClass, Module requestorModule) {
        // The parent class loader (normally java.* packages from the boot class path)
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

        BundleManager bundleManager = ServiceManager.getService(holder.getProject(), BundleManager.class);
        BundleManifest manifest = bundleManager.getManifestByObject(requestorModule);
        if (manifest != null) {
          // Imported packages
          if (manifest.isPackageImported(packageName)) {
            return true;
          }

          // Required bundles
          for (String bundleSpec : manifest.getRequiredBundles()) {
            BundleManifest bundle = bundleManager.getManifestByBundleSpec(bundleSpec);
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

  private static class ImportPackageFix extends AbstractOsgiQuickFix {
    @NotNull
    @Override
    public String getName() {
      return OsmorcBundle.message("PackageAccessibilityInspection.fix");
    }

    @Override
    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
      PsiElement element = descriptor.getPsiElement();
      PsiElement target = ((PsiJavaCodeReferenceElement)element).resolve();
      if (target == null) {
        LOG.info("cannot resolve: " + element.getText());
        return;
      }

      Module module = ModuleUtilCore.findModuleForPsiElement(element);
      assert module != null : descriptor;
      ModuleFileIndex index = ModuleRootManager.getInstance(module).getFileIndex();
      List<OrderEntry> entries = index.getOrderEntriesForFile(target.getContainingFile().getVirtualFile());
      assert !entries.isEmpty() : target;

      Object exporter = null;
      OrderEntry entry = entries.get(0);
      if (entry instanceof ModuleSourceOrderEntry) {
        exporter = ((ModuleSourceOrderEntry)entry).getRootModel().getModule();
      }
      else if (entry instanceof LibraryOrderEntry) {
        exporter = ((LibraryOrderEntry)entry).getLibrary();
        assert exporter != null : entry;
      }
      else if (!(entry instanceof JdkOrderEntry)) {
        LOG.info("unknown entry: " + entry);
        return;
      }

      String packageName = ((PsiJavaFile)target.getContainingFile()).getPackageName();
      if (exporter != null) {
        BundleManager bundleManager = ServiceManager.getService(project, BundleManager.class);
        BundleManifest exporterManifest = bundleManager.getManifestByObject(exporter);
        if (exporterManifest == null) {
          LOG.info("providing entity has no manifest: " + exporter);
          return;
        }
        String exportedPackage = exporterManifest.getExportedPackage(packageName);
        if (exportedPackage == null) {
          LOG.warn(packageName + " is not exported by " + exporterManifest);
          return;
        }
        packageName = exportedPackage;
      }

      ManifestFile manifestFile = getVerifiedManifestFile(element);
      if (manifestFile != null) {
        OsgiPsiUtil.appendToHeader(manifestFile, Constants.IMPORT_PACKAGE, packageName);
      }
    }
  }
}
