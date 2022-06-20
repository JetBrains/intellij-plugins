// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.projectWizard;

import com.intellij.ide.util.projectWizard.ModuleBuilder;
import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.module.WebModuleBuilder;
import com.intellij.openapi.module.WebModuleTypeBase;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModifiableModelsProvider;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.libraries.LibraryTable;
import com.intellij.openapi.roots.libraries.LibraryTablesRegistrar;
import com.intellij.openapi.startup.StartupManager;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.ide.actions.DartPubGetAction;
import com.jetbrains.lang.dart.sdk.DartSdk;
import com.jetbrains.lang.dart.sdk.DartSdkLibUtil;
import com.jetbrains.lang.dart.sdk.DartSdkUtil;
import com.jetbrains.lang.dart.util.PubspecYamlUtil;
import icons.DartIcons;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.IOException;
import java.util.Collection;

public class DartModuleBuilder extends ModuleBuilder {
  private static final Key<Boolean> PUB_GET_SCHEDULED_KEY = Key.create("PUB_GET_SCHEDULED_KEY");

  private DartProjectWizardData myWizardData;

  @Override
  public @Nullable @NonNls String getBuilderId() {
    return "DartModuleBuilder";
  }

  @Override
  public String getName() {
    return DartBundle.message("dart.title");
  }

  @Override
  public String getPresentableName() {
    return DartBundle.message("dart.title");
  }

  @Override
  public String getDescription() {
    return DartBundle.message("dart.project.description");
  }

  @Override
  public Icon getNodeIcon() {
    return DartIcons.Dart_16;
  }

  @Override
  public ModuleType<?> getModuleType() {
    return WebModuleTypeBase.getInstance();
  }

  @Override
  public String getParentGroup() {
    return WebModuleBuilder.GROUP_NAME;
  }

  @Nullable
  @Override
  public ModuleWizardStep getCustomOptionsStep(final WizardContext context, final Disposable parentDisposable) {
    final DartModuleWizardStep step = new DartModuleWizardStep(context);
    Disposer.register(parentDisposable, step);
    return step;
  }

  void setWizardData(final DartProjectWizardData wizardData) {
    myWizardData = wizardData;
  }

  @Override
  public void setupRootModel(@NotNull final ModifiableRootModel modifiableRootModel) {
    final ContentEntry contentEntry = doAddContentEntry(modifiableRootModel);
    final VirtualFile baseDir = contentEntry == null ? null : contentEntry.getFile();
    if (baseDir != null) {
      setupProject(modifiableRootModel, baseDir, myWizardData);
    }
  }

  static void setupProject(@NotNull final ModifiableRootModel modifiableRootModel,
                           @NotNull final VirtualFile baseDir,
                           @NotNull final DartProjectWizardData wizardData) {
    setupSdk(modifiableRootModel, wizardData);

    if (wizardData.myTemplate != null) {
      ApplicationManager.getApplication().executeOnPooledThread(() -> {
        try {
          final Collection<VirtualFile> filesToOpen =
            wizardData.myTemplate.generateProject(wizardData.dartSdkPath, modifiableRootModel.getModule(), baseDir);
          if (!filesToOpen.isEmpty()) {
            scheduleFilesOpeningAndPubGet(modifiableRootModel.getModule(), filesToOpen);
          }
        }
        catch (IOException ignore) {/*unlucky*/}
      });
    }
  }

  private static void setupSdk(@NotNull final ModifiableRootModel modifiableRootModel,
                               @NotNull final DartProjectWizardData wizardData) {
    // similar to DartConfigurable.apply()
    if (DartSdkUtil.isDartSdkHome(wizardData.dartSdkPath)) {
      final Project project = modifiableRootModel.getProject();
      DartSdkUtil.updateKnownSdkPaths(project, wizardData.dartSdkPath);

      final LibraryTable.ModifiableModel libraryTableModifiableModel =
        ModifiableModelsProvider.getInstance().getLibraryTableModifiableModel(project);

      DartSdkLibUtil.ensureDartSdkConfigured(project, libraryTableModifiableModel, wizardData.dartSdkPath);

      if (libraryTableModifiableModel.isChanged()) {
        libraryTableModifiableModel.commit();
      }
      else {
        ModifiableModelsProvider.getInstance().disposeLibraryTableModifiableModel(libraryTableModifiableModel);
      }

      modifiableRootModel.addInvalidLibrary(DartSdk.DART_SDK_LIB_NAME, LibraryTablesRegistrar.PROJECT_LEVEL);
    }
  }

  public static boolean isPubGetScheduledForNewlyCreatedProject(@NotNull final Project project) {
    return project.getUserData(PUB_GET_SCHEDULED_KEY) == Boolean.TRUE;
  }

  private static void scheduleFilesOpeningAndPubGet(@NotNull final Module module, @NotNull final Collection<VirtualFile> files) {
    module.getProject().putUserData(PUB_GET_SCHEDULED_KEY, Boolean.TRUE);
    runWhenNonModalIfModuleNotDisposed(() -> {
      module.getProject().putUserData(PUB_GET_SCHEDULED_KEY, null);

      final FileEditorManager manager = FileEditorManager.getInstance(module.getProject());
      for (VirtualFile file : files) {
        manager.openFile(file, true);

        if (PubspecYamlUtil.PUBSPEC_YAML.equals(file.getName())) {
          final AnAction pubGetAction = ActionManager.getInstance().getAction("Dart.pub.get");
          if (pubGetAction instanceof DartPubGetAction) {
            ((DartPubGetAction)pubGetAction).performPubAction(module, file, false);
          }
        }
      }
    }, module);
  }

  static void runWhenNonModalIfModuleNotDisposed(@NotNull final Runnable runnable, @NotNull final Module module) {
    // runnable must not be executed immediately because the new project model might be not yet committed, so Dart SDK won't be found
    // In WebStorm we get already initialized project at this point, but in IntelliJ IDEA - not yet initialized.

    if (module.getProject().isInitialized()) {
      ApplicationManager.getApplication().invokeLater(runnable, ModalityState.NON_MODAL, module.getDisposed());
      return;
    }

    StartupManager.getInstance(module.getProject()).runWhenProjectIsInitialized(() -> {
      if (ApplicationManager.getApplication().getCurrentModalityState() == ModalityState.NON_MODAL) {
        runnable.run();
      }
      else {
        ApplicationManager.getApplication().invokeLater(runnable, ModalityState.NON_MODAL, o -> module.isDisposed());
      }
    });
  }
}
