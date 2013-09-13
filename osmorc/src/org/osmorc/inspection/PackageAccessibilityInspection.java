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
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.*;
import com.intellij.openapi.roots.libraries.Library;
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
  private static final String NOT_EXPORTED = "not exported";

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
        if (facet != null) {
          PsiElement target = ref.resolve();
          if (target instanceof PsiClass) {
            String toImport = checkAccessibility(target, facet);
            if (toImport == NOT_EXPORTED) {
              holder.registerProblem(ref, OsmorcBundle.message("WrongImportPackageInspection.message"));
            }
            else if (toImport != null) {
              LocalQuickFix fix = new ImportPackageFix(toImport);
              holder.registerProblem(ref, OsmorcBundle.message("PackageAccessibilityInspection.message"), fix);
            }
          }
        }
      }
    };
  }

  private static class ImportPackageFix extends AbstractOsgiQuickFix {
    private final String myPackageToImport;

    public ImportPackageFix(String packageToImport) {
      myPackageToImport = packageToImport;
    }

    @NotNull
    @Override
    public String getName() {
      return OsmorcBundle.message("PackageAccessibilityInspection.fix");
    }

    @Override
    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
      ManifestFile manifestFile = getVerifiedManifestFile(descriptor.getPsiElement());
      if (manifestFile != null) {
        OsgiPsiUtil.appendToHeader(manifestFile, Constants.IMPORT_PACKAGE, myPackageToImport);
      }
    }
  }

  // OSGi Core Spec 3.5 "Class Loading Architecture"
  private static String checkAccessibility(PsiElement targetClass, OsmorcFacet facet) {
    // The parent class loader (normally java.* packages from the boot class path)
    String packageName = ((PsiJavaFile)targetClass.getContainingFile()).getPackageName();
    if (packageName.isEmpty() || packageName.startsWith("java.")) {
      return null;
    }

    // The bundle's class path (private packages)
    // todo[r.sh] need to check actual bundle classpath
    Module requestorModule = facet.getModule();
    Module targetModule = ModuleUtilCore.findModuleForPsiElement(targetClass);
    if (targetModule == requestorModule) {
      return null;
    }

    // obtaining export name of the package from a providing manifest
    String exportedPackage;
    BundleManager bundleManager = ServiceManager.getService(targetClass.getProject(), BundleManager.class);
    ModuleFileIndex index = ModuleRootManager.getInstance(requestorModule).getFileIndex();
    List<OrderEntry> entries = index.getOrderEntriesForFile(targetClass.getContainingFile().getVirtualFile());
    OrderEntry entry = !entries.isEmpty() ? entries.get(0) : null;
    if (entry instanceof ModuleSourceOrderEntry) {
      BundleManifest manifest = bundleManager.getManifestByObject(((ModuleSourceOrderEntry)entry).getRootModel().getModule());
      exportedPackage = manifest != null ? manifest.getExportedPackage(packageName) : null;
    }
    else if (entry instanceof LibraryOrderEntry) {
      Library library = ((LibraryOrderEntry)entry).getLibrary();
      assert library != null : entry;
      BundleManifest manifest = bundleManager.getManifestByObject(library);
      exportedPackage = manifest != null ? manifest.getExportedPackage(packageName) : null;
    }
    else if (entry instanceof JdkOrderEntry) {
      exportedPackage = packageName;
    }
    else {
      return NOT_EXPORTED;
    }
    if (exportedPackage == null) {
      return NOT_EXPORTED;
    }

    if (!facet.getConfiguration().isManifestManuallyEdited()) {
      return null;
    }

    BundleManifest manifest = bundleManager.getManifestByObject(requestorModule);
    if (manifest != null) {
      // Imported packages
      if (manifest.isPackageImported(packageName)) {
        return null;
      }

      // Required bundles
      for (String bundleSpec : manifest.getRequiredBundles()) {
        BundleManifest bundle = bundleManager.getManifestByBundleSpec(bundleSpec);
        if (bundle != null && bundle.getExportedPackage(packageName) != null) {
          return null;
        }
      }

      // Attached fragments [AFAIK these should not be linked statically - r.sh]
    }

    return exportedPackage;
  }
}
