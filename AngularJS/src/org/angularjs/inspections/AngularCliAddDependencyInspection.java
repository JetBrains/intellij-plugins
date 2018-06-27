package org.angularjs.inspections;

import com.intellij.codeInsight.intention.HighPriorityAction;
import com.intellij.codeInspection.*;
import com.intellij.javascript.nodejs.CompletionModuleInfo;
import com.intellij.javascript.nodejs.NodeModuleSearchUtil;
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreter;
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterManager;
import com.intellij.javascript.nodejs.interpreter.local.NodeJsLocalInterpreter;
import com.intellij.javascript.nodejs.packageJson.InstalledPackageVersion;
import com.intellij.javascript.nodejs.packageJson.NodeInstalledPackageFinder;
import com.intellij.javascript.nodejs.packageJson.codeInsight.PackageJsonMismatchedDependencyInspection;
import com.intellij.json.psi.*;
import com.intellij.lang.javascript.buildTools.npm.PackageJsonUtil;
import com.intellij.lang.javascript.modules.NodeModuleUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import com.intellij.util.ObjectUtils;
import org.angularjs.cli.AngularCLIProjectGenerator;
import org.angularjs.cli.AngularCliSchematicsRegistryService;
import org.angularjs.cli.AngularJSProjectConfigurator;
import org.angularjs.cli.actions.AngularCliAddDependencyAction;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class AngularCliAddDependencyInspection extends LocalInspectionTool {

  private static final long TIMEOUT = 2000;

  @NotNull
  @Override
  public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
    return new JsonElementVisitor() {
      @Override
      public void visitFile(PsiFile file) {
        if (PackageJsonUtil.isPackageJsonFile(file)
            && AngularJSProjectConfigurator.findCliJson(file.getVirtualFile().getParent()) != null
            && hasAngularCLIPackageInstalled(holder.getProject(), file.getVirtualFile().getParent())
        ) {
          annotate((JsonFile)file, holder);
        }
      }
    };
  }

  private static boolean hasAngularCLIPackageInstalled(@NotNull Project project, @NotNull VirtualFile cli) {
    NodeJsInterpreter interpreter = NodeJsInterpreterManager.getInstance(project).getInterpreter();
    NodeJsLocalInterpreter node = NodeJsLocalInterpreter.tryCast(interpreter);
    if (node == null) {
      return false;
    }
    List<CompletionModuleInfo> modules = new ArrayList<>();
    NodeModuleSearchUtil.findModulesWithName(modules, AngularCLIProjectGenerator.PACKAGE_NAME, cli,
                                             false, node);
    return !modules.isEmpty() && modules.get(0).getVirtualFile() != null;
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

      if ((pkgVersion != null && AngularCliSchematicsRegistryService.getInstance().supportsNgAdd(pkgVersion))
          || (pkgVersion == null && AngularCliSchematicsRegistryService.getInstance().supportsNgAdd(packageName, TIMEOUT))) {
        String message = StringUtil.wrapWithDoubleQuote(packageName) + " can be installed using 'ng add' command";
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
    private final String myVersionSpec;
    private final boolean myReinstall;

    public AngularCliAddQuickFix(@NotNull VirtualFile packageJson, @NotNull String packageName,
                                 @NotNull String versionSpec, boolean reinstall) {
      myPackageJson = packageJson;
      myPackageName = packageName;
      myVersionSpec = versionSpec;
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
      ApplicationManager.getApplication().executeOnPooledThread(() -> {
        Ref<String> versionSpec = new Ref<>(myVersionSpec);
        if (!myReinstall && !myVersionSpec.isEmpty() && !AngularCliSchematicsRegistryService.getInstance().supportsNgAdd(myPackageName, myVersionSpec, TIMEOUT)) {
          Ref<Integer> result = new Ref<>();
          //noinspection DialogTitleCapitalization
          ApplicationManager.getApplication().invokeAndWait(() ->
            result.set(Messages.showDialog(
              project,
              "It looks like specified version of package doesn't support 'ng add' or doesn't exist.\n\nWould you like to install the latest version of the package?",
              "Install with 'ng add'",
              new String[]{"Install latest version", "Try with current version", Messages.CANCEL_BUTTON}, 0, Messages.getQuestionIcon()))
          );

          switch (result.get()) {
            case 0:
              versionSpec.set("latest");
              break;
            case 1:
              break;
            case 2:
              return;
          }
        }
        ApplicationManager.getApplication().invokeLater(() ->
          AngularCliAddDependencyAction.runAndShowConsole(
            project, myPackageJson.getParent(), myPackageName + (versionSpec.get().isEmpty() ? "" : "@" + versionSpec.get()))
        );
      });
    }
  }
}
