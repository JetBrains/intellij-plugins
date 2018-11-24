package com.jetbrains.lang.dart.projectWizard;

import com.intellij.ide.util.projectWizard.ModuleBuilder;
import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.internal.statistic.UsageTrigger;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.module.WebModuleBuilder;
import com.intellij.openapi.module.WebModuleType;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModifiableModelsProvider;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.libraries.LibraryTable;
import com.intellij.openapi.roots.libraries.LibraryTablesRegistrar;
import com.intellij.openapi.startup.StartupManager;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.io.FileUtilRt;
import com.intellij.openapi.vfs.VirtualFile;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.ide.actions.DartPubGetAction;
import com.jetbrains.lang.dart.ide.runner.client.DartiumUtil;
import com.jetbrains.lang.dart.sdk.DartSdk;
import com.jetbrains.lang.dart.sdk.DartSdkGlobalLibUtil;
import com.jetbrains.lang.dart.sdk.DartSdkUtil;
import com.jetbrains.lang.dart.util.PubspecYamlUtil;
import icons.DartIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.IOException;
import java.util.Collection;

public class DartModuleBuilder extends ModuleBuilder {

  private DartProjectWizardData myWizardData;

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
  public Icon getBigIcon() {
    return DartIcons.Dart_16;
  }

  @Override
  public Icon getNodeIcon() {
    return DartIcons.Dart_16;
  }

  @Override
  public ModuleType getModuleType() {
    return WebModuleType.getInstance();
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
  public void setupRootModel(final ModifiableRootModel modifiableRootModel) throws ConfigurationException {
    final ContentEntry contentEntry = doAddContentEntry(modifiableRootModel);
    final VirtualFile baseDir = contentEntry == null ? null : contentEntry.getFile();
    if (baseDir != null) {
      setupProject(modifiableRootModel, baseDir, myWizardData);
    }
  }

  static void setupProject(@NotNull final ModifiableRootModel modifiableRootModel,
                           @NotNull final VirtualFile baseDir,
                           @NotNull final DartProjectWizardData wizardData) {
    final String templateName = wizardData.myTemplate == null ? "Empty project" : wizardData.myTemplate.getName();
    UsageTrigger.trigger("DartProjectWizard." + templateName);

    setupSdkAndDartium(modifiableRootModel, wizardData);
    if (wizardData.myTemplate != null) {
      try {
        final Collection<VirtualFile> filesToOpen =
          wizardData.myTemplate.generateProject(wizardData.dartSdkPath, modifiableRootModel.getModule(), baseDir);
        if (!filesToOpen.isEmpty()) {
          scheduleFilesOpeningAndPubGet(modifiableRootModel.getModule(), filesToOpen);
        }
      }
      catch (IOException ignore) {/*unlucky*/}
    }
  }

  private static void setupSdkAndDartium(@NotNull final ModifiableRootModel modifiableRootModel,
                                         @NotNull final DartProjectWizardData wizardData) {
    // similar to DartConfigurable.apply()
    if (DartSdkUtil.isDartSdkHome(wizardData.dartSdkPath)) {
      DartSdkUtil.updateKnownSdkPaths(modifiableRootModel.getProject(), wizardData.dartSdkPath);

      final LibraryTable.ModifiableModel libraryTableModifiableModel =
        ModifiableModelsProvider.SERVICE.getInstance().getLibraryTableModifiableModel();

      DartSdkGlobalLibUtil.ensureDartSdkConfigured(libraryTableModifiableModel, wizardData.dartSdkPath);

      if (libraryTableModifiableModel.isChanged()) {
        libraryTableModifiableModel.commit();
      }
      else {
        ModifiableModelsProvider.SERVICE.getInstance().disposeLibraryTableModifiableModel(libraryTableModifiableModel);
      }

      modifiableRootModel.addInvalidLibrary(DartSdk.DART_SDK_GLOBAL_LIB_NAME, LibraryTablesRegistrar.APPLICATION_LEVEL);
    }

    DartiumUtil.applyDartiumSettings(FileUtilRt.toSystemIndependentName(wizardData.dartiumPath), wizardData.dartiumSettings);
  }

  private static void scheduleFilesOpeningAndPubGet(@NotNull final Module module, @NotNull final Collection<VirtualFile> files) {
    runWhenNonModalIfModuleNotDisposed(() -> {
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
