package jetbrains.plugins.yeoman.projectGenerator.builder;

import com.intellij.ide.util.projectWizard.ModuleBuilder;
import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.*;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ui.configuration.ModulesProvider;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;
import icons.YeomanIcons;
import jetbrains.plugins.yeoman.YeomanBundle;
import jetbrains.plugins.yeoman.projectGenerator.template.YeomanProjectGenerator;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;


public class YeomanModuleBuilder extends ModuleBuilder {
  private final YeomanProjectGenerator.Settings mySettings = new YeomanProjectGenerator.Settings();


  public YeomanModuleBuilder() {
    mySettings.tempPath = YeomanProjectGenerator.createTemp().getAbsolutePath();
  }

  @Override
  public void setupRootModel(@NotNull ModifiableRootModel modifiableRootModel) throws ConfigurationException {
    doAddContentEntry(modifiableRootModel);
  }


  @Override
  public ModuleWizardStep[] createWizardSteps(@NotNull WizardContext wizardContext, @NotNull ModulesProvider modulesProvider) {
    return new ModuleWizardStep[]{
      new YeomanRunGeneratorWizardStep(this, wizardContext)
    };
  }

  @Override
  public String getName() {
    return "Yeoman";
  }

  @Override
  public String getPresentableName() {
    return YeomanBundle.message("module.presentable.name.yeoman");
  }

  @Override
  public String getDescription() {
    return YeomanBundle.message("module.builder.description.yeoman.project.type");
  }

  @Nullable
  @Override
  public @NonNls String getBuilderId() {
    return "Yeoman";
  }

  @Override
  public Icon getNodeIcon() {
    return YeomanIcons.Yeoman;
  }

  @Override
  public ModuleType getModuleType() {
    return WebModuleTypeBase.getInstance();
  }

  @Override
  public String getParentGroup() {
    return WebModuleBuilder.GROUP_NAME;
  }

  @Nullable
  @Override
  public Module commitModule(@NotNull Project project, @Nullable ModifiableModuleModel model) {
    ApplicationManager.getApplication().assertIsDispatchThread();
    Module module = super.commitModule(project, model);
    if (module != null) {
      ModuleRootManager moduleRootManager = ModuleRootManager.getInstance(module);
      VirtualFile[] contentRoots = moduleRootManager.getContentRoots();
      VirtualFile toStoreDirectory = project.getBaseDir();
      if (contentRoots.length > 0 && contentRoots[0] != null) {
        toStoreDirectory = contentRoots[0];
      }

      YeomanProjectGenerator.generateProject(project, toStoreDirectory, mySettings);
    }
    return module;
  }

  @Nullable
  @Override
  public ModuleWizardStep getCustomOptionsStep(WizardContext context, Disposable parentDisposable) {
    final YeomanSelectGeneratorWizardStep step = new YeomanSelectGeneratorWizardStep(this, context);
    Disposer.register(parentDisposable, step);
    return step;
  }

  public YeomanProjectGenerator.Settings getSettings() {
    return mySettings;
  }
}
