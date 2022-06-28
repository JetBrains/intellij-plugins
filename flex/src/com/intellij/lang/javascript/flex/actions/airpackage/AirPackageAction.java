// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.lang.javascript.flex.actions.airpackage;

import com.intellij.flex.model.bc.BuildConfigurationNature;
import com.intellij.flex.model.bc.TargetPlatform;
import com.intellij.ide.actions.RevealFileAction;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.flex.actions.ExternalTask;
import com.intellij.lang.javascript.flex.build.FlexResourceBuildTargetScopeProvider;
import com.intellij.lang.javascript.flex.projectStructure.model.*;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationListener;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.compiler.CompileContext;
import com.intellij.openapi.compiler.CompileScope;
import com.intellij.openapi.compiler.CompileStatusNotification;
import com.intellij.openapi.compiler.CompilerManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.text.HtmlBuilder;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.Consumer;
import com.intellij.util.PathUtil;
import gnu.trove.THashMap;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.event.HyperlinkEvent;
import java.io.File;
import java.util.*;

import static com.intellij.lang.javascript.flex.actions.airpackage.AirPackageProjectParameters.DesktopPackageType;

public class AirPackageAction extends DumbAwareAction {
  public static final NotificationGroup NOTIFICATION_GROUP = NotificationGroup.balloonGroup("AIR Packaging");

  @Override
  public void update(@NotNull final AnActionEvent e) {
    final Project project = e.getProject();

    boolean flexModulePresent = false;
    boolean airAppPresent = false;

    if (project != null) {
      final FlexModuleType flexModuleType = FlexModuleType.getInstance();

      MODULES_LOOP:
      for (Module module : ModuleManager.getInstance(project).getModules()) {
        if (ModuleType.get(module) == flexModuleType) {
          flexModulePresent = true;

          for (FlexBuildConfiguration bc : FlexBuildConfigurationManager.getInstance(module).getBuildConfigurations()) {
            final BuildConfigurationNature nature = bc.getNature();
            if (nature.isApp() && !nature.isWebPlatform()) {
              airAppPresent = true;
              break MODULES_LOOP;
            }
          }
        }
      }
    }

    e.getPresentation().setVisible(flexModulePresent);
    e.getPresentation().setEnabled(airAppPresent &&
                                   !CompilerManager.getInstance(project).isCompilationActive() &&
                                   !AirPackageProjectParameters.getInstance(project).isPackagingInProgress());
  }

  @Override
  public @NotNull ActionUpdateThread getActionUpdateThread() {
    return ActionUpdateThread.BGT;
  }

  @Override
  public void actionPerformed(@NotNull final AnActionEvent e) {
    final Project project = e.getProject();
    if (project == null) return;

    final AirPackageDialog dialog = new AirPackageDialog(project);
    if (!dialog.showAndGet()) {
      return;
    }

    final Collection<Pair<Module, FlexBuildConfiguration>> modulesAndBCs = dialog.getSelectedBCs();
    final Set<Module> modules = new THashSet<>();
    for (Pair<Module, FlexBuildConfiguration> bc : modulesAndBCs) {
      modules.add(bc.first);
    }

    final CompilerManager compilerManager = CompilerManager.getInstance(project);
    final CompileScope compileScope = compilerManager.createModulesCompileScope(modules.toArray(Module.EMPTY_ARRAY), false);
    FlexResourceBuildTargetScopeProvider.setBCsToCompileForPackaging(compileScope, modulesAndBCs);

    compilerManager.make(compileScope, new CompileStatusNotification() {
      @Override
      public void finished(final boolean aborted, final int errors, final int warnings, @NotNull final CompileContext compileContext) {
        if (!aborted && errors == 0) {
          createPackages(project, modulesAndBCs, dialog.getPasswords());
        }
      }
    });
  }

  private static void createPackages(final Project project,
                                     final Collection<Pair<Module, FlexBuildConfiguration>> modulesAndBCs,
                                     final PasswordStore passwords) {
    final Collection<Pair<ExternalTask, String>> tasksAndPackagePaths = new ArrayList<>();

    final AirPackageProjectParameters params = AirPackageProjectParameters.getInstance(project);

    for (Pair<Module, FlexBuildConfiguration> moduleAndBC : modulesAndBCs) {
      final FlexBuildConfiguration bc = moduleAndBC.second;
      final String outputFolder = PathUtil.getParentPath(bc.getActualOutputFilePath());

      if (bc.getTargetPlatform() == TargetPlatform.Desktop) {
        final DesktopPackageType packageType = params.desktopPackageType;
        final ExternalTask task = AirPackageUtil.createAirDesktopTask(moduleAndBC.first, bc, packageType, passwords);
        final String packagePath = outputFolder + "/" +
                                   bc.getAirDesktopPackagingOptions().getPackageFileName() + packageType.getFileExtension();
        tasksAndPackagePaths.add(Pair.create(task, packagePath));
      }
      else {
        if (bc.getAndroidPackagingOptions().isEnabled()) {
          final AndroidPackagingOptions packagingOptions = bc.getAndroidPackagingOptions();
          final ExternalTask task = AirPackageUtil.createAndroidPackageTask(moduleAndBC.first, bc, params.androidPackageType,
                                                                            params.apkCaptiveRuntime, params.apkDebugListenPort, passwords);
          final String packagePath = outputFolder + "/" + packagingOptions.getPackageFileName() + ".apk";
          tasksAndPackagePaths.add(Pair.create(task, packagePath));
        }

        if (bc.getIosPackagingOptions().isEnabled()) {
          final IosPackagingOptions packagingOptions = bc.getIosPackagingOptions();
          final ExternalTask task = AirPackageUtil
            .createIOSPackageTask(moduleAndBC.first, bc, params.iosPackageType, params.iosFastPackaging,
                                  bc.getIosPackagingOptions().getSigningOptions().getIOSSdkPath(), 0, passwords);
          final String packagePath = outputFolder + "/" + packagingOptions.getPackageFileName() + ".ipa";
          tasksAndPackagePaths.add(Pair.create(task, packagePath));
        }
      }
    }

    createPackages(project, tasksAndPackagePaths);
  }

  private static void createPackages(final Project project, final Collection<Pair<ExternalTask, String>> tasksAndPackagePaths) {
    final Iterator<Pair<ExternalTask, String>> iterator = tasksAndPackagePaths.iterator();
    final Pair<ExternalTask, String> taskAndPackagePath = iterator.next();
    final ExternalTask task = taskAndPackagePath.first;
    final String packagePath = taskAndPackagePath.second;
    final Consumer<List<String>> onSuccessRunnable =
      createSuccessConsumer(project, iterator, packagePath, new THashMap<>());
    ExternalTask
      .runInBackground(task, FlexBundle.message("packaging.air.application", PathUtil.getFileName(packagePath)),
                       onSuccessRunnable, createFailureConsumer(project, packagePath, task));
  }

  private static Consumer<List<String>> createSuccessConsumer(final Project project,
                                                              final Iterator<Pair<ExternalTask, String>> iterator,
                                                              final String createdPackagePath,
                                                              final Map<String, List<String>> packagePathsToWarnings) {
    return messages -> {
      packagePathsToWarnings.put(createdPackagePath, messages);

      if (iterator.hasNext()) {
        final Pair<ExternalTask, String> taskAndPackagePath = iterator.next();
        final ExternalTask task = taskAndPackagePath.first;
        final String packagePath = taskAndPackagePath.second;
        final Consumer<List<String>> onSuccessRunnable = createSuccessConsumer(project, iterator, packagePath, packagePathsToWarnings);
        ExternalTask
          .runInBackground(task, FlexBundle.message("packaging.air.application", PathUtil.getFileName(packagePath)),
                           onSuccessRunnable, createFailureConsumer(project, packagePath, task));
      }
      else {
        final HtmlBuilder hrefs = new HtmlBuilder();
        for (Map.Entry<String, List<String>> entry : packagePathsToWarnings.entrySet()) {
          final String packagePath = entry.getKey();
          final List<String> warnings = entry.getValue();

          if (!hrefs.isEmpty()) {
            hrefs.br();
          }

          hrefs.appendLink(packagePath, PathUtil.getFileName(packagePath));

          if (!warnings.isEmpty()) {
            hrefs.br();
            for (String warning : warnings) {
              hrefs.append(warning).br();
            }
          }
        }

        final String message = FlexBundle.message("air.application.created", packagePathsToWarnings.size(), hrefs);

        final NotificationListener listener = new NotificationListener() {
          @Override
          public void hyperlinkUpdate(@NotNull final Notification notification, @NotNull final HyperlinkEvent event) {
            if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
              notification.expire();
              final String packagePath = event.getDescription();
              RevealFileAction.openFile(new File(packagePath));
            }
          }
        };

        NOTIFICATION_GROUP.createNotification(message, NotificationType.INFORMATION).setListener(listener).notify(project);
      }
    };
  }

  private static Consumer<List<String>> createFailureConsumer(final Project project, final String packagePath, final ExternalTask task) {
    return messages -> {
      final String reason = StringUtil.join(messages, "<br>");

      final NotificationListener listener = new NotificationListener.Adapter() {
        @Override
        protected void hyperlinkActivated(@NotNull Notification notification, @NotNull HyperlinkEvent e) {
          if ("full.error.message".equals(e.getDescription())) {
            Messages.showIdeaMessageDialog(project, reason, "Error Message",
                                           new String[]{Messages.getOkButton()}, 0, null, null);
          }

          if ("adt.command.line".equals(e.getDescription())) {
            Messages.showIdeaMessageDialog(project, task.getCommandLine(), "ADT Command Line",
                                           new String[]{Messages.getOkButton()}, 0, null, null);
          }
        }
      };

      final String message;
      if (reason.length() > 500) {
        message = FlexBundle
          .message("failed.to.create.air.package.truncated", PathUtil.getFileName(packagePath), reason.substring(0, 500) + "...");
      }
      else {
        message = FlexBundle.message("failed.to.create.air.package", PathUtil.getFileName(packagePath), reason);
      }

      NOTIFICATION_GROUP.createNotification(message, NotificationType.ERROR).setListener(listener).notify(project);
    };
  }

  @Nullable
  public static PasswordStore getPasswords(final Project project,
                                           final Collection<? extends AirPackagingOptions> allPackagingOptions) {
    final Collection<AirSigningOptions> signingOptionsWithUnknownPasswords = new ArrayList<>();

    for (AirPackagingOptions packagingOptions : allPackagingOptions) {
      final AirSigningOptions signingOptions = packagingOptions.getSigningOptions();
      final boolean tempCertificate = !(packagingOptions instanceof IosPackagingOptions) && signingOptions.isUseTempCertificate();
      if (!tempCertificate && !PasswordStore.isPasswordKnown(project, signingOptions)) {
        signingOptionsWithUnknownPasswords.add(signingOptions);
      }
    }

    if (!signingOptionsWithUnknownPasswords.isEmpty()) {
      final KeystorePasswordDialog dialog = new KeystorePasswordDialog(project, signingOptionsWithUnknownPasswords);
      return dialog.showAndGet() ? dialog.getPasswords() : null;
    }

    return PasswordStore.getInstance(project);
  }
}
