// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.lang.javascript.flex.flashbuilder;

import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.projectImport.ProjectImportBuilder;
import com.intellij.projectImport.ProjectOpenProcessorBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.File;
import java.util.Collections;

public final class FlashBuilderOpenProcessor extends ProjectOpenProcessorBase<FlashBuilderImporter> {
  @NotNull
  @Override
  protected FlashBuilderImporter doGetBuilder() {
    return ProjectImportBuilder.EXTENSIONS_POINT_NAME.findExtensionOrFail(FlashBuilderImporter.class);
  }

  @Override
  public String @NotNull [] getSupportedExtensions() {
    // This method is called from:
    // 1. OpenProjectAction#action (to make label text): we don't want *.zip to appear in label.
    // 2. OpenProjectProcessorBase#canOpenProject: not called, because overridden in this class
    // 3. OpenProjectProcessorBase#doOpenProject (to find FB project file in dir): we do not search for archives in this case, want to look only for .project file. And luckily it works exactly as we need because files can't be named with "*" symbol.
    return new String[]{FlashBuilderImporter.DOT_PROJECT, "*" + FlashBuilderImporter.DOT_FXP, "*" + FlashBuilderImporter.DOT_FXPL};
  }

  @Override
  @Nullable
  public Icon getIcon(@NotNull final VirtualFile file) {
    if ("zip".equalsIgnoreCase(file.getExtension())) return null; // standard icon is better for zip, it is not Flash Builder specific extension
    return super.getIcon(file);
  }

  @Override
  public boolean canOpenProject(@NotNull final VirtualFile file) {
    // do not look inside archives here - it may be too expensive, fail later if not a suitable archive
    return file.isDirectory()
           ? FlashBuilderProjectFinder.isFlashBuilderProject(file.findChild(FlashBuilderImporter.DOT_PROJECT))
           : FlashBuilderProjectFinder.isFlashBuilderProject(file) ||
             file.getName().endsWith(FlashBuilderImporter.DOT_FXP) ||
             file.getName().endsWith(FlashBuilderImporter.DOT_FXPL);
  }

  @Override
  protected boolean doQuickImport(@NotNull final VirtualFile file, @NotNull final WizardContext wizardContext) {
    assert !file.isDirectory() : file.getPath();
    final String title = FlexBundle.message("open.project.0", file.getName());

    if (FlashBuilderProjectFinder.hasArchiveExtension(file.getPath())) {
      try {
        FlashBuilderProjectFinder.checkArchiveContainsFBProject(file.getPath());
      }
      catch (ConfigurationException e) {
        Messages.showErrorDialog(wizardContext.getProject(), e.getMessage(), title);
        return false;
      }

      final String label = FlashBuilderProjectFinder.isMultiProjectArchive(file.getPath())
                           ? FlexBundle.message("folder.to.unzip.several.FB.projects")
                           : FlexBundle.message("folder.to.unzip.one.FB.project");
      final String path = FileUtil.toSystemDependentName(wizardContext.getProjectFileDirectory() + "/" + file.getNameWithoutExtension());
      final AskPathDialog dialog = new AskPathDialog(title, label, path);

      if (!dialog.showAndGet()) {
        return false;
      }

      final File projectDir = new File(dialog.getPath());
      if (!projectDir.exists() && !projectDir.mkdirs()) {
        Messages.showErrorDialog("Unable to create folder: " + dialog.getPath(), title);
        return false;
      }

      wizardContext.setProjectFileDirectory(dialog.getPath());
    }
    getBuilder().setInitiallySelectedPath(file.getPath());
    getBuilder().setList(Collections.singletonList(file.getPath()));
    wizardContext.setProjectName(getBuilder().getSuggestedProjectName());
    return true;
  }
}
