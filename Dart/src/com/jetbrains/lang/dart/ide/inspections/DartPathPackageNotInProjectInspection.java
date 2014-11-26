package com.jetbrains.lang.dart.ide.inspections;

import com.intellij.CommonBundle;
import com.intellij.codeInspection.IntentionAndQuickFixAction;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationListener;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.progress.ProgressIndicatorProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectBundle;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.roots.ui.configuration.ProjectSettingsService;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import com.intellij.util.PlatformUtils;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.DartProjectComponent;
import com.jetbrains.lang.dart.sdk.DartSdk;
import com.jetbrains.lang.dart.sdk.DartSdkGlobalLibUtil;
import com.jetbrains.lang.dart.util.DartResolveUtil;
import com.jetbrains.lang.dart.util.PubspecYamlUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.yaml.psi.YAMLCompoundValue;
import org.jetbrains.yaml.psi.YAMLDocument;
import org.jetbrains.yaml.psi.YAMLKeyValue;

import javax.swing.event.HyperlinkEvent;

public class DartPathPackageNotInProjectInspection extends LocalInspectionTool {

  private static final String GROUP_DISPLAY_ID = "pubspec.yaml inspection";

  @Override
  @NotNull
  public String getGroupDisplayName() {
    return DartBundle.message("inspections.group.name");
  }

  @Override
  @Nls
  @NotNull
  public String getDisplayName() {
    return DartBundle.message("path.package.not.in.project.inspection.name");
  }

  @NotNull
  @Override
  public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, final boolean isOnTheFly) {
    if (!PubspecYamlUtil.PUBSPEC_YAML.equals(holder.getFile().getName())) return super.buildVisitor(holder, isOnTheFly);

    final Module module = ModuleUtilCore.findModuleForPsiElement(holder.getFile());
    final DartSdk sdk = DartSdk.getGlobalDartSdk();
    if (module == null || sdk == null || !DartSdkGlobalLibUtil.isDartSdkGlobalLibAttached(module, sdk.getGlobalLibName())) {
      return super.buildVisitor(holder, isOnTheFly);
    }

    return new PsiElementVisitor() {
      @Override
      public void visitElement(final PsiElement element) {
        ProgressIndicatorProvider.checkCanceled();

        if (!(element instanceof YAMLKeyValue) || !PubspecYamlUtil.PATH.equals(((YAMLKeyValue)element).getKeyText())) return;

        final VirtualFile file = DartResolveUtil.getRealVirtualFile(element.getContainingFile());
        final String path = ((YAMLKeyValue)element).getValueText() + "/" + PubspecYamlUtil.LIB_DIR_NAME;
        final VirtualFile packageDir = file == null ? null : VfsUtilCore.findRelativeFile(path, file.getParent());
        if (packageDir == null || !packageDir.isDirectory()) return;

        final PsiElement parent1 = element.getParent();
        final PsiElement parent2 = parent1 instanceof YAMLCompoundValue ? parent1.getParent() : null;
        final String packageName = parent2 instanceof YAMLKeyValue ? ((YAMLKeyValue)parent2).getKeyText() : null;
        if (packageName == null) return;

        final PsiElement parent3 = parent2.getParent();
        final PsiElement parent4 = parent3 instanceof YAMLCompoundValue ? parent3.getParent() : null;
        if (!(parent4 instanceof YAMLKeyValue) ||
            !(parent4.getParent() instanceof YAMLDocument) ||
            (!PubspecYamlUtil.DEPENDENCIES.equals(((YAMLKeyValue)parent4).getKeyText()) &&
             !PubspecYamlUtil.DEV_DEPENDENCIES.equals(((YAMLKeyValue)parent4).getKeyText()))) {
          return;
        }

        if (!ProjectRootManager.getInstance(element.getProject()).getFileIndex().isInContent(packageDir)) {
          final String message = DartBundle.message("folder.0.not.in.project.content",
                                                    FileUtil.toSystemDependentName(packageDir.getPath()));
          holder.registerProblem(((YAMLKeyValue)element).getValue(), message, new AddContentRootFix(module, packageDir));
        }
      }
    };
  }

  private static class AddContentRootFix extends IntentionAndQuickFixAction {
    @NotNull private final Module myModule;
    @NotNull private final VirtualFile myContentRoot;

    private AddContentRootFix(@NotNull final Module module, @NotNull final VirtualFile contentRoot) {
      myModule = module;
      myContentRoot = contentRoot;
    }

    @Override
    @NotNull
    public String getName() {
      return DartBundle.message("configure.folder.0.as.content.root", FileUtil.toSystemDependentName(myContentRoot.getPath()));
    }

    @Override
    @NotNull
    public String getFamilyName() {
      return DartBundle.message("configure.folder.as.content.root");
    }

    @Override
    public boolean startInWriteAction() {
      return false;
    }

    @Override
    public void applyFix(@NotNull final Project project, final PsiFile psiFile, @Nullable final Editor editor) {
      try {
        checkCanAddContentRoot(myModule, myContentRoot);
      }
      catch (Exception e) {
        showErrorDialog(myModule, e);
        return;
      }

      final ModifiableRootModel modifiableModel = ModuleRootManager.getInstance(myModule).getModifiableModel();
      try {
        modifiableModel.addContentEntry(myContentRoot);

        ApplicationManager.getApplication().runWriteAction(new Runnable() {
          @Override
          public void run() {
            modifiableModel.commit();
          }
        });

        showSuccessNotification(myModule, myContentRoot);
      }
      catch (Exception e) {
        showErrorDialog(myModule, e);
      }
      finally {
        if (!modifiableModel.isDisposed()) {
          modifiableModel.dispose();
        }
      }

      final VirtualFile yamlFile = DartResolveUtil.getRealVirtualFile(psiFile);
      if (yamlFile != null && PubspecYamlUtil.PUBSPEC_YAML.equals(yamlFile.getName())) {
        DartProjectComponent.excludeBuildAndPackagesFolders(myModule, yamlFile);
      }
    }

    private static void showSuccessNotification(@NotNull final Module module, @NotNull final VirtualFile root) {
      final String title = DartBundle.message("content.root.added.title");
      final String message =
        DartSdkGlobalLibUtil.isIdeWithMultipleModuleSupport()
        ? DartBundle.message("content.root.added.to.module", module.getName(), FileUtil.toSystemDependentName(root.getPath()))
        : DartBundle.message("content.root.added.to.project", FileUtil.toSystemDependentName(root.getPath()), CommonBundle.settingsTitle(),
                             module.getProject().getName(), getProjectRootsConfigurableName());

      Notifications.Bus.notify(new Notification(GROUP_DISPLAY_ID, title, message, NotificationType.INFORMATION,
                                                new NotificationListener.Adapter() {
                                                  @Override
                                                  protected void hyperlinkActivated(@NotNull final Notification notification,
                                                                                    @NotNull final HyperlinkEvent e) {
                                                    openProjectRootsConfigurable(module);
                                                  }
                                                }));
    }

    private static void showErrorDialog(@NotNull final Module module, @NotNull final Exception e) {
      final String message = DartBundle.message("can.not.add.content.root", e.getMessage());
      final String title = DartBundle.message("add.content.root.title");
      final String okText = DartBundle.message("configure.project.roots");
      final String cancelText = CommonBundle.getCancelButtonText();

      final int choice =
        Messages.showOkCancelDialog(module.getProject(), message, title, okText, cancelText, Messages.getWarningIcon());

      if (choice == Messages.OK) {
        openProjectRootsConfigurable(module);
      }
    }

    // similar to com.intellij.openapi.roots.ui.configuration.CommonContentEntriesEditor.AddContentEntryAction.validateContentEntriesCandidates()
    private static void checkCanAddContentRoot(@NotNull final Module module, @NotNull final VirtualFile contentRoot) throws Exception {
      for (final VirtualFile contentEntryFile : ModuleRootManager.getInstance(module).getContentRoots()) {
        if (contentEntryFile.equals(contentRoot)) {
          throw new Exception(ProjectBundle.message("module.paths.add.content.already.exists.error", contentRoot.getPresentableUrl()));
        }

        if (VfsUtilCore.isAncestor(contentEntryFile, contentRoot, true)) {
          // intersection not allowed
          throw new Exception(
            ProjectBundle.message("module.paths.add.content.intersect.error", contentRoot.getPresentableUrl(),
                                  contentEntryFile.getPresentableUrl()));
        }

        if (VfsUtilCore.isAncestor(contentRoot, contentEntryFile, true)) {
          // intersection not allowed
          throw new Exception(
            ProjectBundle.message("module.paths.add.content.dominate.error", contentRoot.getPresentableUrl(),
                                  contentEntryFile.getPresentableUrl()));
        }
      }

      for (final Module otherModule : ModuleManager.getInstance(module.getProject()).getModules()) {
        if (module.equals(otherModule)) {
          continue;
        }

        for (VirtualFile moduleContentRoot : ModuleRootManager.getInstance(otherModule).getContentRoots()) {
          if (contentRoot.equals(moduleContentRoot)) {
            throw new Exception(
              ProjectBundle.message("module.paths.add.content.duplicate.error", contentRoot.getPresentableUrl(), otherModule.getName()));
          }
        }
      }
    }
  }

  private static void openProjectRootsConfigurable(final Module module) {
    if (PlatformUtils.isWebStorm() || PlatformUtils.isPhpStorm() || PlatformUtils.isPyCharm() || PlatformUtils.isRubyMine()) {
      ShowSettingsUtil.getInstance().showSettingsDialog(module.getProject(), getProjectRootsConfigurableName());
    }
    else {
      ProjectSettingsService.getInstance(module.getProject()).openContentEntriesSettings(module);
    }
  }


  @SuppressWarnings("IfStatementWithIdenticalBranches")
  private static String getProjectRootsConfigurableName() {
    if (PlatformUtils.isWebStorm() || PlatformUtils.isPhpStorm()) {
      // "Directories" comes from com.intellij.webcore.resourceRoots.WebIdeProjectStructureConfigurable.getDisplayName()
      return "Directories";
    }
    else if (PlatformUtils.isRubyMine()) {
      // "Project Structure" comes from org.jetbrains.plugins.ruby.settings.RubyProjectStructureConfigurable.getDisplayName()
      return "Project Structure";
    }
    else if (PlatformUtils.isPyCharm()) {
      // "Project Structure" comes from com.jetbrains.python.configuration.PyContentEntriesModuleConfigurable.getDisplayName()
      return "Project Structure";
    }

    // com.intellij.openapi.roots.ui.configuration.PlatformContentEntriesConfigurable.getDisplayName()
    return "Project Structure";
  }
}
