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
import com.intellij.codeInspection.options.OptPane;
import com.intellij.codeInspection.ui.SingleCheckboxOptionsPanel;
import com.intellij.codeInspection.util.InspectionMessage;
import com.intellij.ide.projectView.impl.ProjectRootsUtil;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.packageDependencies.DependenciesBuilder;
import com.intellij.packageDependencies.DependencyVisitorFactory;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiClassOwner;
import com.intellij.psi.PsiFile;
import com.intellij.util.SmartList;
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

import static com.intellij.codeInspection.options.OptPane.*;
import static org.osmorc.i18n.OsmorcBundle.message;

/**
 * Inspection which checks if a package of a class is accessible inside the OSGi context.
 *
 * @author <a href="mailto:janthomae@janthomae.de">Jan Thomä</a>
 * @author <a href="mailto:robert@beeger.net">Robert F. Beeger</a>
 */
public class PackageAccessibilityInspection extends AbstractBaseJavaLocalInspectionTool {
  public boolean checkTests = false;

  @Override
  public @NotNull OptPane getOptionsPane() {
    return pane(
      checkbox("checkTests", message("PackageAccessibilityInspection.ui.check.tests")));
  }

  @Override
  public ProblemDescriptor @Nullable [] checkFile(@NotNull PsiFile file, @NotNull final InspectionManager manager, final boolean isOnTheFly) {
    if (!(file instanceof PsiClassOwner) || !checkTests && ProjectRootsUtil.isInTestSource(file)) {
      return null;
    }

    final OsmorcFacet facet = OsmorcFacet.getInstance(file);
    if (facet == null) {
      return null;
    }

    final List<ProblemDescriptor> problems = new SmartList<>();
    DependenciesBuilder.analyzeFileDependencies(file, (place, dependency) -> {
      if (dependency instanceof PsiClass) {
        Problem problem = checkAccessibility((PsiClass)dependency, facet);
        if (problem != null) {
          problems.add(manager.createProblemDescriptor(place, problem.message, isOnTheFly, problem.fixes, problem.type));
        }
      }
    }, DependencyVisitorFactory.VisitorOptions.SKIP_IMPORTS);
    return problems.isEmpty() ? null : problems.toArray(ProblemDescriptor.EMPTY_ARRAY);
  }

  private static final class Problem {
    final ProblemHighlightType type;
    final @InspectionMessage String message;
    final LocalQuickFix[] fixes;

    Problem(ProblemHighlightType type, @InspectionMessage String message, LocalQuickFix... fixes) {
      this.type = type;
      this.message = message;
      this.fixes = fixes.length > 0 ? fixes : null;
    }

    static Problem weak(@InspectionMessage String message, LocalQuickFix... fixes) {
      return new Problem(ProblemHighlightType.WEAK_WARNING, message, fixes);
    }

    static Problem error(@InspectionMessage String message, LocalQuickFix... fixes) {
      return new Problem(ProblemHighlightType.GENERIC_ERROR_OR_WARNING, message, fixes);
    }
  }

  // OSGi Core Spec 3.5 "Class Loading Architecture"
  private static Problem checkAccessibility(PsiClass targetClass, OsmorcFacet facet) {
    // ignores annotations invisible at runtime
    if (targetClass.isAnnotationType()) {
      RetentionPolicy retention = AnnotationsHighlightUtil.getRetentionPolicy(targetClass);
      if (retention == RetentionPolicy.SOURCE || retention == RetentionPolicy.CLASS) {
        return null;
      }
    }

    // ignores files of unsupported type
    PsiFile targetFile = targetClass.getContainingFile();
    if (!(targetFile instanceof PsiClassOwner)) {
      return null;
    }

    // accepts classes from the parent class loader (normally java.* packages from the boot class path)
    String packageName = ((PsiClassOwner)targetFile).getPackageName();
    if (packageName.isEmpty() || packageName.startsWith("java.")) {
      return null;
    }

    // accepts classes from the bundle's class path (private packages)
    Module requestorModule = facet.getModule();
    Module targetModule = ModuleUtilCore.findModuleForPsiElement(targetClass);
    if (targetModule == requestorModule) {
      return null;
    }

    BundleManifest importer = BundleManifestCache.getInstance().getManifest(requestorModule);
    if (importer != null && (importer.isPrivatePackage(packageName) || importer.getExportedPackage(packageName) != null)) {
      return null;
    }

    // rejects non-exported classes (manifest missing, or a package isn't listed as exported)
    BundleManifest exporter = BundleManifestCache.getInstance().getManifest(targetClass);
    if (exporter == null || exporter.getBundleSymbolicName() == null) {
      return Problem.weak(message("PackageAccessibilityInspection.non.osgi", packageName));
    }

    String exportedPackage = exporter.getExportedPackage(packageName);
    if (exportedPackage == null) {
      return Problem.error(message("PackageAccessibilityInspection.not.exported", packageName));
    }

    // ignores facets other than manually-edited manifests (most probably, they will have their import list correctly generated)
    if (!facet.getConfiguration().isManifestManuallyEdited()) {
      return null;
    }

    // accepts packages listed as imported or required
    if (importer != null) {
      if (importer.isPackageImported(packageName)) {
        return null;
      }

      if (importer.isBundleRequired(exporter.getBundleSymbolicName())) {
        return null;
      }

      // Attached fragments [AFAIK these should not be linked statically - r.sh]
    }

    return Problem.error(message("PackageAccessibilityInspection.not.imported", packageName), new ImportPackageFix(exportedPackage));
  }

  private static class ImportPackageFix extends AbstractOsgiQuickFix {
    private final String myPackageToImport;

    ImportPackageFix(String packageToImport) {
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
        WriteCommandAction.writeCommandAction(project, manifestFile).run(() -> OsgiPsiUtil.appendToHeader(manifestFile, Constants.IMPORT_PACKAGE, myPackageToImport));
      }
    }
  }
}
