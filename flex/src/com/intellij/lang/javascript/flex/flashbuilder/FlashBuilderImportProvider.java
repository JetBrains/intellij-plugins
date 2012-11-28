package com.intellij.lang.javascript.flex.flashbuilder;

import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.projectImport.ProjectImportProvider;
import org.jetbrains.annotations.Nullable;

public class FlashBuilderImportProvider extends ProjectImportProvider {

  private final FlashBuilderOpenProcessor myProcessor;

  public FlashBuilderImportProvider(final FlashBuilderImporter builder) {
    super(builder);
    myProcessor = new FlashBuilderOpenProcessor(builder);
  }

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

  @Nullable
  @Override
  public String getFileSample() {
    return "<b>Flash Builder</b> project file (" + StringUtil.join(myProcessor.getSupportedExtensions(), ", ") + ")";
  }
}
