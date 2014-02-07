package com.jetbrains.lang.dart.ide.template;

import com.intellij.ide.util.projectWizard.WebProjectTemplate;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.sdk.DartSdk;
import com.jetbrains.lang.dart.sdk.DartSdkGlobalLibUtil;
import com.jetbrains.lang.dart.sdk.DartSdkUtil;
import icons.DartIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.IOException;

public class DartWebApplicationGenerator extends WebProjectTemplate<String> {

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
                              final @NotNull String dartSdkPath,
                              final @NotNull Module module) {
    ApplicationManager.getApplication().runWriteAction(new Runnable() {
      public void run() {
        if (DartSdkUtil.isDartSdkHome(dartSdkPath)) {
          final DartSdk sdk = DartSdk.getGlobalDartSdk();

          final String dartSdkLibName;
          if (sdk == null) {
            dartSdkLibName = DartSdkGlobalLibUtil.createDartSdkGlobalLib(dartSdkPath);
          }
          else {
            dartSdkLibName = sdk.getGlobalLibName();

            if (!dartSdkPath.equals(sdk.getHomePath())) {
              DartSdkGlobalLibUtil.updateDartSdkGlobalLib(dartSdkLibName, dartSdkPath);
            }
          }

          DartSdkGlobalLibUtil.configureDependencyOnGlobalLib(module, dartSdkLibName);
        }

        try {
          baseDir.createChildDirectory(this, "web");
          baseDir.createChildDirectory(this, "lib");
          final VirtualFile pubspecYamlFile = baseDir.createChildData(this, "pubspec.yaml");
          pubspecYamlFile.setBinaryContent(("name: " + module.getName() + "\n" +
                                            "dependencies:\n" +
                                            "  browser: any").getBytes());
        }
        catch (IOException ignore) {/* unlucky */}
      }
    });
  }

  @NotNull
  public GeneratorPeer<String> createPeer() {
    return new DartGeneratorPeer();
  }
}
