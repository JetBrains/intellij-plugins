package com.jetbrains.lang.dart.ide.template;

import com.intellij.ide.util.projectWizard.WebProjectTemplate;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModifiableModelsProvider;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryTable;
import com.intellij.openapi.util.io.FileUtilRt;
import com.intellij.openapi.vfs.VirtualFile;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.ide.runner.client.DartiumUtil;
import com.jetbrains.lang.dart.sdk.DartSdk;
import com.jetbrains.lang.dart.sdk.DartSdkGlobalLibUtil;
import com.jetbrains.lang.dart.sdk.DartSdkUtil;
import icons.DartIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.IOException;

import static com.jetbrains.lang.dart.util.PubspecYamlUtil.PUBSPEC_YAML;

public class DartApplicationGenerator extends WebProjectTemplate<DartProjectWizardData> {

  @NotNull
  public String getName() {
    return DartBundle.message("dart.web.application.title");
  }

  public String getDescription() {
    return DartBundle.message("dart.web.application.description");
  }

  public Icon getIcon() {
    return DartIcons.Dart_16;
  }

  public void generateProject(final @NotNull Project project,
                              final @NotNull VirtualFile baseDir,
                              final @NotNull DartProjectWizardData data,
                              final @NotNull Module module) {
    ApplicationManager.getApplication().runWriteAction(new Runnable() {
      public void run() {
        setupSdkAndDartium(module, data);

        try {
          baseDir.createChildDirectory(DartApplicationGenerator.this, "web");
          baseDir.createChildDirectory(DartApplicationGenerator.this, "lib");
          final VirtualFile pubspecYamlFile = baseDir.createChildData(DartApplicationGenerator.this, PUBSPEC_YAML);
          pubspecYamlFile.setBinaryContent(("name: " + module.getName() + "\n" +
                                            "dependencies:\n" +
                                            "  browser: any").getBytes());
        }
        catch (IOException ignore) {/* unlucky */}
      }
    });
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

  @NotNull
  public GeneratorPeer<DartProjectWizardData> createPeer() {
    return new DartGeneratorPeer();
  }
}
