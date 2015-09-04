package com.jetbrains.lang.dart.ide.inspections;

import com.intellij.CommonBundle;
import com.intellij.codeInspection.IntentionAndQuickFixAction;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationListener;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
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
import com.intellij.psi.*;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReference;
import com.intellij.util.PlatformUtils;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.DartProjectComponent;
import com.jetbrains.lang.dart.ide.actions.DartPubGetAction;
import com.jetbrains.lang.dart.psi.PubspecYamlReferenceContributor;
import com.jetbrains.lang.dart.sdk.DartSdk;
import com.jetbrains.lang.dart.sdk.DartSdkGlobalLibUtil;
import com.jetbrains.lang.dart.util.DartResolveUtil;
import com.jetbrains.lang.dart.util.PubspecYamlUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.yaml.psi.YAMLKeyValue;

import javax.swing.event.HyperlinkEvent;

public class DartPathPackageReferenceInspection extends LocalInspectionTool {

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
    return DartBundle.message("path.package.reference.inspection.name");
  }

  @NotNull
  @Override
  public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, final boolean isOnTheFly) {
    if (!PubspecYamlUtil.PUBSPEC_YAML.equals(holder.getFile().getName())) return super.buildVisitor(holder, isOnTheFly);

    final Module module = ModuleUtilCore.findModuleForPsiElement(holder.getFile());
    final DartSdk sdk = DartSdk.getDartSdk(holder.getProject());
    if (module == null || sdk == null || !DartSdkGlobalLibUtil.isDartSdkEnabled(module)) {
      return super.buildVisitor(holder, isOnTheFly);
    }

    return new PsiElementVisitor() {
      @Override
      public void visitElement(final PsiElement element) {
        ProgressIndicatorProvider.checkCanceled();

        if (!(element instanceof YAMLKeyValue) || !PubspecYamlReferenceContributor.isPathPackageDefinition((YAMLKeyValue)element)) {
          return;
        }

        final VirtualFile packageDir = checkReferences(holder, (YAMLKeyValue)element);
        if (packageDir == null) {
          return;
        }

        if (packageDir.findChild(PubspecYamlUtil.PUBSPEC_YAML) == null) {
          final String message = DartBundle.message("pubspec.yaml.not.found.in", FileUtil.toSystemDependentName(packageDir.getPath()));
          holder.registerProblem(((YAMLKeyValue)element).getValue(), message);
          return;
        }

        final VirtualFile file = DartResolveUtil.getRealVirtualFile(element.getContainingFile());
        if (file != null && packageDir.equals(file.getParent())) {
          holder.registerProblem(((YAMLKeyValue)element).getValue(), DartBundle.message("path.package.reference.to.itself"));
          return;
        }

        final VirtualFile libDir = packageDir.findChild(PubspecYamlUtil.LIB_DIR_NAME);
        if (libDir != null && libDir.isDirectory() &&
            !ProjectRootManager.getInstance(element.getProject()).getFileIndex().isInContent(libDir)) {
          final String message = DartBundle.message("folder.0.not.in.project.content",
                                                    FileUtil.toSystemDependentName(packageDir.getPath()));
          holder.registerProblem(((YAMLKeyValue)element).getValue(), message, new AddContentRootFix(module, packageDir));
        }
      }
    };
  }

  @Nullable
  private static VirtualFile checkReferences(@NotNull final ProblemsHolder holder, @NotNull final YAMLKeyValue element) {
    for (PsiReference reference : element.getReferences()) {
      if (reference instanceof FileReference && !reference.isSoft()) {
        final PsiFileSystemItem resolve = ((FileReference)reference).resolve();
        if (resolve == null) {
          holder.registerProblem(reference.getElement(), ((FileReference)reference).getUnresolvedMessagePattern(),
                                 ProblemHighlightType.GENERIC_ERROR, reference.getRangeInElement());
          return null;
        }
        else if (((FileReference)reference).isLast()) {
          final VirtualFile dir = resolve.getVirtualFile();
          if (dir != null && dir.isDirectory()) {
            return dir;
          }
        }
      }
    }

    return null;
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

        final VirtualFile otherPubspec = myContentRoot.findChild(PubspecYamlUtil.PUBSPEC_YAML);
        if (otherPubspec != null) {
          // exclude before indexing started
          DartProjectComponent.excludeBuildAndPackagesFolders(myModule, otherPubspec);

          final AnAction pubGetAction = ActionManager.getInstance().getAction("Dart.pub.get");
          if (pubGetAction instanceof DartPubGetAction) {
            ((DartPubGetAction)pubGetAction).performPubAction(myModule, otherPubspec, false);
          }
        }

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
