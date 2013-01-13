package com.jetbrains.lang.dart.ide.template;

import com.intellij.ide.util.projectWizard.WebProjectTemplate;
import com.intellij.lang.javascript.library.JSLibraryMappings;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.ide.DartSdkData;
import com.jetbrains.lang.dart.ide.settings.DartSettings;
import icons.DartIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.IOException;

public class DartWebApplicationGenerator extends WebProjectTemplate {
  @NotNull
  @Override
  public String getName() {
    return DartBundle.message("dart.module.web.application.name");
  }

  @Nullable
  @Override
  public String getDescription() {
    return DartBundle.message("dart.module.web.application.description");
  }

  @Override
  protected Icon getIcon() {
    return DartIcons.Dart_16;
  }

  @Override
  public void generateProject(@NotNull Project project,
                              @NotNull final VirtualFile baseDir,
                              @NotNull Object settings,
                              @NotNull final Module module) {
    if (!(settings instanceof DartSdkData)) return;
    DartSettings.updateOrCreateDartLibrary(project, ((DartSdkData)settings).getHomePath());
    final JSLibraryMappings mappings = ServiceManager.getService(project, JSLibraryMappings.class);
    mappings.associate(baseDir, DartBundle.message("dart.sdk.name"));
    ApplicationManager.getApplication().runWriteAction(new Runnable() {
      @Override
      public void run() {
        try {
          VirtualFile pubspec = baseDir.createChildData(this, "pubspec.yaml");
          pubspec.setBinaryContent(("name: " + module.getName() + "\n" +
                                    "dependencies:\n" +
                                    "\tbrowser: any").getBytes());
        }
        catch (IOException e) {
          // ignore
        }
      }
    });
  }

  @NotNull
  @Override
  public GeneratorPeer createPeer() {
    return new DartGeneratorPeer();
  }
}
