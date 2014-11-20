package com.jetbrains.lang.dart.projectWizard;

import com.intellij.ide.util.projectWizard.WebProjectTemplate;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModifiableModelsProvider;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryTable;
import com.intellij.openapi.startup.StartupManager;
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

import javax.swing.*;
import java.io.IOException;

public class DartEmptyProjectGenerator extends WebProjectTemplate<DartProjectWizardData> implements Comparable<DartEmptyProjectGenerator> {

  private final @NotNull String myName;
  private final @NotNull String myDescription;

  public DartEmptyProjectGenerator() {
    this(DartBundle.message("empty.dart.project.description.webstorm"));
  }

  public DartEmptyProjectGenerator(@NotNull final String description) {
    this(DartBundle.message("empty.dart.project.title"), description);
  }

  protected DartEmptyProjectGenerator(@NotNull final String name, @NotNull final String description) {
    myName = name;
    myDescription = description;
  }

  @NotNull
  public final String getName() {
    return myName;
  }

  @NotNull
  public final String getDescription() {
    return myDescription;
  }

  public Icon getIcon() {
    return DartIcons.Dart_16;
  }

  @NotNull
  public final GeneratorPeer<DartProjectWizardData> createPeer() {
    return new DartGeneratorPeer();
  }

  @Override
  public final void generateProject(@NotNull final Project project,
                                    @NotNull final VirtualFile baseDir,
                                    @NotNull final DartProjectWizardData data,
                                    @NotNull final Module module) {
    ApplicationManager.getApplication().runWriteAction(new Runnable() {
      public void run() {
        setupSdkAndDartium(module, data);
        try {
          final VirtualFile[] filesToOpen = doGenerateProject(module, baseDir);
          if (filesToOpen.length > 0) {
            scheduleFilesOpeningAndPubGet(project, filesToOpen);
          }
        }
        catch (IOException ignore) {/* unlucky */}
      }
    });
  }

  @Override
  public int compareTo(@NotNull final DartEmptyProjectGenerator generator) {
    return getName().compareTo(generator.getName());
  }

  @NotNull
  protected VirtualFile[] doGenerateProject(@NotNull final Module module, final VirtualFile baseDir) throws IOException {
    return VirtualFile.EMPTY_ARRAY;
  }

  private static void setupSdkAndDartium(final Module module, final DartProjectWizardData data) {
    // similar to DartConfigurable.apply()
    final ModifiableModelsProvider modifiableModelsProvider = ModifiableModelsProvider.SERVICE.getInstance();
    if (DartSdkUtil.isDartSdkHome(data.dartSdkPath)) {
      final LibraryTable.ModifiableModel libraryTableModifiableModel = modifiableModelsProvider.getLibraryTableModifiableModel();

      final DartSdk sdk = DartSdk.findDartSdkAmongGlobalLibs(libraryTableModifiableModel.getLibraries());
      final String dartSdkLibName;

      if (sdk == null) {
        dartSdkLibName = DartSdkGlobalLibUtil.createDartSdkGlobalLib(libraryTableModifiableModel, data.dartSdkPath);
      }
      else {
        dartSdkLibName = sdk.getGlobalLibName();

        if (!data.dartSdkPath.equals(sdk.getHomePath())) {
          DartSdkGlobalLibUtil.updateDartSdkGlobalLib(libraryTableModifiableModel, dartSdkLibName, data.dartSdkPath);
        }
      }

      final Library dartSdkGlobalLib = libraryTableModifiableModel.getLibraryByName(dartSdkLibName);
      assert dartSdkGlobalLib != null;

      if (libraryTableModifiableModel.isChanged()) {
        libraryTableModifiableModel.commit();
      }

      // similar to DartSdkGlobalLibUtil.configureDependencyOnGlobalLib
      final ModifiableRootModel moduleModifiableModel = modifiableModelsProvider.getModuleModifiableModel(module);
      moduleModifiableModel.addLibraryEntry(dartSdkGlobalLib);
      modifiableModelsProvider.commitModuleModifiableModel(moduleModifiableModel);
    }

    DartiumUtil.applyDartiumSettings(FileUtilRt.toSystemIndependentName(data.dartiumPath), data.dartiumSettings);
  }

  private static void scheduleFilesOpeningAndPubGet(final @NotNull Project project, final @NotNull VirtualFile[] files) {
    StartupManager.getInstance(project).runWhenProjectIsInitialized(new Runnable() {
      @Override
      public void run() {
        final FileEditorManager manager = FileEditorManager.getInstance(project);
        for (VirtualFile file : files) {
          manager.openFile(file, true);

          if (PubspecYamlUtil.PUBSPEC_YAML.equals(file.getName())) {
            final AnAction pubGetAction = ActionManager.getInstance().getAction("Dart.pub.get");
            final Module module = ModuleUtilCore.findModuleForFile(file, project);
            if (pubGetAction instanceof DartPubGetAction && module != null) {
              ((DartPubGetAction)pubGetAction).performPubAction(module, file, false);
            }
          }
        }
      }
    });
  }
}
