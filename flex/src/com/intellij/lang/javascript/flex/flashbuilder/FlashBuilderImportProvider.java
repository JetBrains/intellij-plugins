// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.lang.javascript.flex.flashbuilder;

import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.projectImport.ProjectImportBuilder;
import com.intellij.projectImport.ProjectImportProvider;
import org.jetbrains.annotations.NotNull;

final class FlashBuilderImportProvider extends ProjectImportProvider {
  private final FlashBuilderOpenProcessor myProcessor = new FlashBuilderOpenProcessor();

  @Override
  protected ProjectImportBuilder doGetBuilder() {
    return ProjectImportBuilder.EXTENSIONS_POINT_NAME.findExtensionOrFail(FlashBuilderImporter.class);
  }

  @Override
  public ModuleWizardStep[] createSteps(final WizardContext context) {
    return new ModuleWizardStep[]{new SelectDirWithFlashBuilderProjectsStep(context), new SelectFlashBuilderImportedProjectsStep(context)};
  }

  @Override
  protected boolean canImportFromFile(VirtualFile file) {
    return myProcessor.canOpenProject(file);
  }

  @Override
  public String getPathToBeImported(VirtualFile file) {
    return file.getPath();
  }

  @NotNull
  @Override
  public String getFileSample() {
    return FlexBundle.message("flash.builder.project.file", StringUtil.join(myProcessor.getSupportedExtensions(), ", "));
  }
}
