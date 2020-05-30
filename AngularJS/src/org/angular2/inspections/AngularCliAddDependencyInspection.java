// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.inspections;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.javascript.nodejs.packageJson.InstalledPackageVersion;
import com.intellij.javascript.nodejs.packageJson.NodeInstalledPackageFinder;
import com.intellij.javascript.nodejs.packageJson.codeInsight.PackageJsonMismatchedDependencyInspection;
import com.intellij.json.psi.*;
import com.intellij.lang.javascript.buildTools.npm.PackageJsonUtil;
import com.intellij.lang.javascript.library.JSLibraryUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import com.intellij.util.ObjectUtils;
import org.angular2.cli.AngularCliSchematicsRegistryService;
import org.angular2.cli.AngularCliUtil;
import org.angular2.inspections.quickfixes.AngularCliAddQuickFix;
import org.angular2.lang.Angular2Bundle;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class AngularCliAddDependencyInspection extends LocalInspectionTool {

  private static final long TIMEOUT = 2000;

  @Override
  public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
    return new JsonElementVisitor() {
      @Override
      public void visitFile(@NotNull PsiFile file) {
        if (PackageJsonUtil.isPackageJsonFile(file)
            && AngularCliUtil.findCliJson(file.getVirtualFile().getParent()) != null) {
          annotate((JsonFile)file, holder);
        }
      }
    };
  }

  private static void annotate(@NotNull JsonFile file, @NotNull ProblemsHolder holder) {
    VirtualFile packageJson = file.getVirtualFile();
    Project project = file.getProject();
    if (packageJson == null || !JSLibraryUtil.isUnderContentRootsAndOutsideOfLibraryRoots(project, packageJson)) return;

    List<JsonProperty> properties = PackageJsonMismatchedDependencyInspection.getDependencies(file);
    if (properties.isEmpty()) return;
    NodeInstalledPackageFinder finder = new NodeInstalledPackageFinder(project, packageJson);
    for (JsonProperty property : properties) {
      JsonStringLiteral nameLiteral = ObjectUtils.tryCast(property.getNameElement(), JsonStringLiteral.class);
      JsonStringLiteral versionLiteral = ObjectUtils.tryCast(property.getValue(), JsonStringLiteral.class);
      if (nameLiteral == null) {
        continue;
      }

      String packageName = property.getName();
      String version = versionLiteral == null ? "" : versionLiteral.getValue();
      InstalledPackageVersion pkgVersion = finder.findInstalledPackage(packageName);

      if ((pkgVersion != null && AngularCliSchematicsRegistryService.getInstance().supportsNgAdd(pkgVersion))
          || (pkgVersion == null && AngularCliSchematicsRegistryService.getInstance().supportsNgAdd(packageName, TIMEOUT))) {
        String message = Angular2Bundle.message("angular.inspection.install-with-ng-add.message",
                                                StringUtil.wrapWithDoubleQuote(packageName));
        LocalQuickFix quickFix = new AngularCliAddQuickFix(packageJson, packageName, version, pkgVersion != null);
        if (versionLiteral != null) {
          if (pkgVersion == null) {
            holder.registerProblem(versionLiteral, getTextRange(versionLiteral), message, quickFix);
          }
          else if (holder.isOnTheFly()) {
            holder.registerProblem(versionLiteral, message, ProblemHighlightType.INFORMATION, quickFix);
          }
        }
        if (holder.isOnTheFly()) {
          holder.registerProblem(nameLiteral, message, ProblemHighlightType.INFORMATION, quickFix);
        }
      }
    }
  }

  private static @NotNull TextRange getTextRange(@NotNull JsonValue element) {
    TextRange range = element.getTextRange();
    if (element instanceof JsonStringLiteral && range.getLength() > 2 &&
        StringUtil.isQuotedString(element.getText())) {
      return new TextRange(1, range.getLength() - 1);
    }
    return TextRange.create(0, range.getLength());
  }

  @Override
  public boolean isEnabledByDefault() {
    return true;
  }
}
