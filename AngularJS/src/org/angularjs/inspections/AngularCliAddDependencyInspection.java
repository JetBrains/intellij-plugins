package org.angularjs.inspections;

import com.intellij.codeInsight.intention.HighPriorityAction;
import com.intellij.codeInspection.*;
import com.intellij.javascript.nodejs.packageJson.InstalledPackageVersion;
import com.intellij.javascript.nodejs.packageJson.NodeInstalledPackageFinder;
import com.intellij.javascript.nodejs.packageJson.codeInsight.PackageJsonMismatchedDependencyInspection;
import com.intellij.json.psi.*;
import com.intellij.lang.javascript.buildTools.npm.PackageJsonUtil;
import com.intellij.lang.javascript.modules.NodeModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import com.intellij.util.ObjectUtils;
import org.angularjs.cli.AngularCliSchematicsRegistryService;
import org.angularjs.cli.AngularJSProjectConfigurator;
import org.angularjs.cli.actions.AngularCliAddDependencyAction;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class AngularCliAddDependencyInspection extends LocalInspectionTool {

  @NotNull
  @Override
  public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
    return new JsonElementVisitor() {
      @Override
      public void visitFile(PsiFile file) {
        if (PackageJsonUtil.isPackageJsonFile(file)
            && AngularJSProjectConfigurator.findCliJson(file.getVirtualFile().getParent()) != null) {
          annotate((JsonFile)file, holder);
        }
      }
    };
  }

  private static void annotate(@NotNull JsonFile file, @NotNull ProblemsHolder holder) {
    VirtualFile packageJson = file.getVirtualFile();
    if (packageJson == null) return;
    Project project = file.getProject();
    VirtualFile contentRoot = ProjectFileIndex.getInstance(project).getContentRootForFile(packageJson, false);
    if (contentRoot != null && NodeModuleUtil.hasNodeModulesDirInPath(packageJson, contentRoot)) {
      return;
    }

    List<JsonProperty> properties = PackageJsonMismatchedDependencyInspection.getDependencies(file);
    if (properties.isEmpty()) return;
    NodeInstalledPackageFinder finder = new NodeInstalledPackageFinder(project, packageJson);
    for (JsonProperty property: properties) {
      JsonStringLiteral nameLiteral = ObjectUtils.tryCast(property.getNameElement(), JsonStringLiteral.class);
      JsonStringLiteral versionLiteral = ObjectUtils.tryCast(property.getValue(), JsonStringLiteral.class);
      if (nameLiteral == null) {
        continue;
      }

      String packageName = property.getName();
      String version = versionLiteral == null ? "" : versionLiteral.getValue();
      InstalledPackageVersion pkgVersion = finder.findInstalledPackage(packageName);

      if (AngularCliSchematicsRegistryService.getInstance().supportsNgAdd(packageName, version, 1000)) {
        String message = StringUtil.wrapWithDoubleQuote(packageName) + " can be installed using 'ng add' command";
        LocalQuickFix quickFix = new AngularCliAddQuickFix(packageJson, packageName, version, pkgVersion != null);
        if (versionLiteral != null) {
          if (pkgVersion == null) {
            holder.registerProblem(versionLiteral, getTextRange(versionLiteral), message, quickFix);
          }
          else {
            holder.registerProblem(versionLiteral, message, ProblemHighlightType.INFORMATION, quickFix);
          }
        }
        holder.registerProblem(nameLiteral, message, ProblemHighlightType.INFORMATION, quickFix);
      }
    }
  }

  @NotNull
  private static TextRange getTextRange(@NotNull JsonValue element) {
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

  private static class AngularCliAddQuickFix implements LocalQuickFix, HighPriorityAction {
    private final VirtualFile myPackageJson;
    private final String myPackageName;
    private final String myPackageSpec;
    private final boolean myReinstall;

    public AngularCliAddQuickFix(@NotNull VirtualFile packageJson, @NotNull String packageName,
                                 @NotNull String versionSpec, boolean reinstall) {
      myPackageJson = packageJson;
      myPackageName = packageName;
      myPackageSpec = packageName + (versionSpec.isEmpty() ? "" : "@" + versionSpec);
      myReinstall = reinstall;
    }

    @Nls
    @NotNull
    @Override
    public String getName() {
      return (myReinstall ? "Reinstall with" : "Run") + " 'ng add " + myPackageName + "'";
    }

    @Nls
    @NotNull
    @Override
    public String getFamilyName() {
      return "Run 'ng add'";
    }

    @Override
    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
      AngularCliAddDependencyAction.runAndShowConsole(
        project, myPackageJson.getParent(), myPackageSpec);
    }
  }
}
