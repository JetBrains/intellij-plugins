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

import com.intellij.codeInsight.daemon.impl.analysis.AnnotationsHighlightUtil;
import com.intellij.codeInspection.*;
import com.intellij.codeInspection.ui.SingleCheckboxOptionsPanel;
import com.intellij.ide.projectView.impl.ProjectRootsUtil;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.packageDependencies.DependenciesBuilder;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.lang.manifest.psi.ManifestFile;
import org.jetbrains.osgi.project.BundleManifest;
import org.jetbrains.osgi.project.BundleManifestCache;
import org.osgi.framework.Constants;
import org.osmorc.facet.OsmorcFacet;
import org.osmorc.util.OsgiPsiUtil;

import javax.swing.*;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

import static org.osmorc.i18n.OsmorcBundle.message;

/**
 * Inspection which checks if a package of a class is accessible inside the OSGi context.
 *
 * @author <a href="mailto:janthomae@janthomae.de">Jan Thom&auml;</a>
 * @author Robert F. Beeger (robert@beeger.net)
 */
public class PackageAccessibilityInspection extends BaseJavaBatchLocalInspectionTool {
  public boolean checkTests = false;

  @Override
  public JComponent createOptionsPanel() {
    return new SingleCheckboxOptionsPanel(message("PackageAccessibilityInspection.ui.check.tests"), this, "checkTests");
  }

  @Nullable
  @Override
  public ProblemDescriptor[] checkFile(@NotNull PsiFile file, @NotNull final InspectionManager manager, final boolean isOnTheFly) {
    if (!checkTests && ProjectRootsUtil.isInTestSource(file)) {
      return null;
    }

    final OsmorcFacet facet = OsmorcFacet.getInstance(file);
    if (facet == null) {
      return null;
    }

    final List<ProblemDescriptor> problems = ContainerUtil.newSmartList();
    DependenciesBuilder.analyzeFileDependencies(file, new DependenciesBuilder.DependencyProcessor() {
      @Override
      public void process(PsiElement place, PsiElement dependency) {
        if (dependency instanceof PsiClass && PsiTreeUtil.getParentOfType(place, PsiImportStatement.class) == null) {
          Problem problem = checkAccessibility((PsiClass)dependency, facet);
          if (problem != null) {
            problems.add(manager.createProblemDescriptor(place, problem.message, isOnTheFly, problem.fixes, problem.type));
          }
        }
      }
    });
    return problems.isEmpty() ? null : problems.toArray(new ProblemDescriptor[problems.size()]);
  }

  private static class Problem {
    public final ProblemHighlightType type;
    public final String message;
    public final LocalQuickFix[] fixes;

    private Problem(ProblemHighlightType type, String message, LocalQuickFix... fixes) {
      this.type = type;
      this.message = message;
      this.fixes = fixes.length > 0 ? fixes : null;
    }

    public static Problem weak(String message, LocalQuickFix... fixes) {
      return new Problem(ProblemHighlightType.WEAK_WARNING, message, fixes);
    }

    public static Problem error(String message, LocalQuickFix... fixes) {
      return new Problem(ProblemHighlightType.GENERIC_ERROR_OR_WARNING, message, fixes);
    }
  }

  // OSGi Core Spec 3.5 "Class Loading Architecture"
  private static Problem checkAccessibility(PsiClass targetClass, OsmorcFacet facet) {
    if (targetClass.isAnnotationType()) {
      RetentionPolicy retention = AnnotationsHighlightUtil.getRetentionPolicy(targetClass);
      if (retention == RetentionPolicy.SOURCE || retention == RetentionPolicy.CLASS) {
        return null;
      }
    }

    PsiFile targetFile = targetClass.getContainingFile();
    if (!(targetFile instanceof PsiClassOwner)) {
      return null;  // alien file, ignore
    }

    // The parent class loader (normally java.* packages from the boot class path)
    String packageName = ((PsiClassOwner)targetFile).getPackageName();
    if (packageName.isEmpty() || packageName.startsWith("java.")) {
      return null;
    }

    Project project = targetClass.getProject();

    // The bundle's class path (private packages)
    Module requestorModule = facet.getModule();
    Module targetModule = ModuleUtilCore.findModuleForPsiElement(targetClass);
    if (targetModule == requestorModule) {
      return null;
    }

    BundleManifest manifest = BundleManifestCache.getInstance(project).getManifest(requestorModule);
    if (manifest != null && manifest.isPrivatePackage(packageName)) {
      return null;
    }

    // obtaining export name of the package from a providing manifest
    BundleManifest exporter = BundleManifestCache.getInstance(project).getManifest(targetClass);
    if (exporter == null || exporter.getBundleSymbolicName() == null) {
      return Problem.weak(message("PackageAccessibilityInspection.non.osgi", packageName));
    }

    String exportedPackage = exporter.getExportedPackage(packageName);
    if (exportedPackage == null) {
      return Problem.error(message("PackageAccessibilityInspection.not.exported", packageName));
    }

    // checking if the package is imported (only for manually-edited manifests)
    if (!facet.getConfiguration().isManifestManuallyEdited()) {
      return null;
    }

    if (manifest != null) {
      // Imported packages
      if (manifest.isPackageImported(packageName)) {
        return null;
      }

      // Required bundles
      if (manifest.isBundleRequired(exporter.getBundleSymbolicName())) {
        return null;
      }

      // Attached fragments [AFAIK these should not be linked statically - r.sh]
    }

    return Problem.error(message("PackageAccessibilityInspection.not.imported", packageName), new ImportPackageFix(exportedPackage));
  }

  private static class ImportPackageFix extends AbstractOsgiQuickFix {
    private final String myPackageToImport;

    public ImportPackageFix(String packageToImport) {
      myPackageToImport = packageToImport;
    }

    @NotNull
    @Override
    public String getName() {
      return message("PackageAccessibilityInspection.fix");
    }

    @Override
    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
      ManifestFile manifestFile = getVerifiedManifestFile(descriptor.getPsiElement());
      if (manifestFile != null) {
        OsgiPsiUtil.appendToHeader(manifestFile, Constants.IMPORT_PACKAGE, myPackageToImport);
      }
    }
  }
}
