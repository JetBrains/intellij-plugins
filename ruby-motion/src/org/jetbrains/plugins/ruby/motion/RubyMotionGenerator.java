package org.jetbrains.plugins.ruby.motion;

import com.intellij.facet.ui.ValidationResult;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.startup.StartupManager;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.platform.HideableProjectGenerator;
import icons.RubyIcons;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.ruby.RBundle;
import org.jetbrains.plugins.ruby.motion.ui.RubyMotionGeneratorTabBase;
import org.jetbrains.plugins.ruby.motion.ui.RubyMotionSettingsHolder;
import org.jetbrains.plugins.ruby.ruby.RModuleUtil;
import org.jetbrains.plugins.ruby.wizard.RubyFrameworkProjectGenerator;

import javax.swing.*;

/**
 * @author Dennis.Ushakov
 */
public class RubyMotionGenerator extends RubyFrameworkProjectGenerator<RubyMotionSettingsHolder>
  implements HideableProjectGenerator {
  private static final String TITLE = RBundle.message("ruby.motion.application.settings");
  private RubyMotionSettingsHolder mySettings;
  private RubyMotionGeneratorTabBase myGeneratorTab;

  @NotNull
  @Nls
  @Override
  public String getName() {
    return RBundle.message("ruby.motion.wizard.tab.project.generator.title");
  }

  @Nullable
  @Override
  public Icon getLogo() {
    return RubyIcons.RubyMotion.RubyMotion;
  }

  @Override
  public void generateProjectInner(
    @NotNull final Project project,
    @NotNull final VirtualFile baseDir,
    @NotNull final RubyMotionSettingsHolder settings,
    @NotNull final Module module) {
    final RubyMotionUtilImpl.ProjectType projectType = settings.getProjectType();

    module.putUserData(RubyMotionUtilImpl.PROJECT_TYPE, projectType);
    RubyMotionFacetConfigurator.configure(baseDir, module);
    StartupManager.getInstance(project).runWhenProjectIsInitialized(() -> {
      ((RubyMotionUtilImpl)RubyMotionUtil.getInstance())
        .generateApp(baseDir, module, ModuleRootManager.getInstance(module).getSdk(), projectType);
      RModuleUtil.getInstance().refreshRubyModuleTypeContent(module);
    });
  }

  @NotNull
  @Override
  public ValidationResult doValidate(@NotNull String baseDirPath) {
    return RubyMotionUtil.getInstance().rubyMotionPresent() ? ValidationResult.OK : new ValidationResult("RubyMotion is not installed");
  }

  @Override
  public boolean isHidden() {
    return !SystemInfo.isMac;
  }

  @NotNull
  @Override
  public RubyMotionSettingsHolder createSettings() {
    return new RubyMotionSettingsHolder();
  }

  @NotNull
  @Override
  public JPanel getSettingsPanel(@NotNull RubyMotionSettingsHolder settings, @NotNull Disposable parentDisposable) {
    if (myGeneratorTab == null) {
      myGeneratorTab = new RubyMotionGeneratorTabBase(settings);
      Disposer.register(parentDisposable, () -> myGeneratorTab = null);
    }
    mySettings = settings;
    final JComponent generatorTabComponent = myGeneratorTab.createComponent();
    return (JPanel)generatorTabComponent;
  }

  @NotNull
  @Override
  public String getParentGroupName() {
    return "Mobile";
  }
}