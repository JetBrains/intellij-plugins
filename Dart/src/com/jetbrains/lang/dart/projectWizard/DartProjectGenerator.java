package com.jetbrains.lang.dart.projectWizard;

import com.intellij.ide.util.projectWizard.WebProjectTemplate;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModifiableModelsProvider;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.vfs.VirtualFile;
import com.jetbrains.lang.dart.DartBundle;
import icons.DartIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class DartProjectGenerator extends WebProjectTemplate<DartProjectWizardData> implements Comparable<DartProjectGenerator> {

  @NotNull
  public final String getName() {
    return DartBundle.message("dart.title");
  }

  @NotNull
  public final String getDescription() {
    return DartBundle.message("dart.project.description");
  }

  public Icon getIcon() {
    return DartIcons.Dart_16;
  }

  @NotNull
  public GeneratorPeer<DartProjectWizardData> createPeer() {
    return new DartGeneratorPeer();
  }

  @Override
  public final void generateProject(@NotNull final Project project,
                                    @NotNull final VirtualFile baseDir,
                                    @NotNull final DartProjectWizardData data,
                                    @NotNull final Module module) {
    ApplicationManager.getApplication().runWriteAction(
      new Runnable() {
        public void run() {
          final ModifiableRootModel modifiableModel = ModifiableModelsProvider.SERVICE.getInstance().getModuleModifiableModel(module);
          DartModuleBuilder.setupProject(modifiableModel, baseDir, data);
          ModifiableModelsProvider.SERVICE.getInstance().commitModuleModifiableModel(modifiableModel);
        }
      });
  }

  @Override
  public int compareTo(@NotNull final DartProjectGenerator generator) {
    return getName().compareTo(generator.getName());
  }
}
