// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.lang.dart.projectWizard;

import com.intellij.ide.util.projectWizard.WebProjectTemplate;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModifiableModelsProvider;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.platform.ProjectGeneratorPeer;
import com.jetbrains.lang.dart.DartBundle;
import icons.DartIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public final class DartProjectGenerator extends WebProjectTemplate<DartProjectWizardData> implements Comparable<DartProjectGenerator> {

  @Override
  public String getId() {
    return "Dart";
  }

  @Override
  public @NotNull String getName() {
    return DartBundle.message("dart.title");
  }

  @Override
  public @NotNull String getDescription() {
    return DartBundle.message("dart.project.description");
  }

  @Override
  public Icon getIcon() {
    return DartIcons.Dart_16;
  }

  @Override
  public @NotNull ProjectGeneratorPeer<DartProjectWizardData> createPeer() {
    return new DartGeneratorPeer();
  }

  @Override
  public void generateProject(final @NotNull Project project,
                              final @NotNull VirtualFile baseDir,
                              final @NotNull DartProjectWizardData data,
                              final @NotNull Module module) {
    ApplicationManager.getApplication().runWriteAction(
      () -> {
        final ModifiableRootModel modifiableModel = ModifiableModelsProvider.getInstance().getModuleModifiableModel(module);
        DartModuleBuilder.setupProject(modifiableModel, baseDir, data);
        ModifiableModelsProvider.getInstance().commitModuleModifiableModel(modifiableModel);
      });
  }

  @Override
  public int compareTo(final @NotNull DartProjectGenerator generator) {
    return getName().compareTo(generator.getName());
  }
}
