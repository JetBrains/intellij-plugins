package com.jetbrains.lang.dart.ide.template;

import com.intellij.ide.util.projectWizard.WebProjectTemplate;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
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

public class DartWebApplicationGenerator extends WebProjectTemplate<DartProjectWizardData> {

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
    // similar to DartConfigurable.apply()
    ApplicationManager.getApplication().runWriteAction(new Runnable() {
      public void run() {
        if (DartSdkUtil.isDartSdkHome(data.dartSdkPath)) {
          final DartSdk sdk = DartSdk.getGlobalDartSdk();

          final String dartSdkLibName;
          if (sdk == null) {
            dartSdkLibName = DartSdkGlobalLibUtil.createDartSdkGlobalLib(project, data.dartSdkPath);
          }
          else {
            dartSdkLibName = sdk.getGlobalLibName();

            if (!data.dartSdkPath.equals(sdk.getHomePath())) {
              DartSdkGlobalLibUtil.updateDartSdkGlobalLib(project, dartSdkLibName, data.dartSdkPath);
            }
          }

          DartSdkGlobalLibUtil.configureDependencyOnGlobalLib(module, dartSdkLibName);
        }

        DartiumUtil.applyDartiumSettings(FileUtilRt.toSystemIndependentName(data.dartiumPath), data.dartiumSettings);

        try {
          baseDir.createChildDirectory(this, "web");
          baseDir.createChildDirectory(this, "lib");
          final VirtualFile pubspecYamlFile = baseDir.createChildData(this, PUBSPEC_YAML);
          pubspecYamlFile.setBinaryContent(("name: " + module.getName() + "\n" +
                                            "dependencies:\n" +
                                            "  browser: any").getBytes());
        }
        catch (IOException ignore) {/* unlucky */}
      }
    });
  }

  @NotNull
  public GeneratorPeer<DartProjectWizardData> createPeer() {
    return new DartGeneratorPeer();
  }
}
