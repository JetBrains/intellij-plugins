package org.jetbrains.plugins.ruby.motion;

import com.intellij.facet.ui.ValidationResult;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.startup.StartupManager;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.platform.DirectoryProjectGenerator;
import com.intellij.platform.HideableProjectGenerator;
import icons.RubyIcons;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.ruby.RBundle;
import org.jetbrains.plugins.ruby.RubyFrameworkProjectGenerator;
import org.jetbrains.plugins.ruby.motion.ui.RubyMotionGeneratorTab;
import org.jetbrains.plugins.ruby.motion.ui.RubyMotionSettingsHolder;
import org.jetbrains.plugins.ruby.rails.facet.ui.wizard.ui.TabbedSettingsDialog;
import org.jetbrains.plugins.ruby.rails.facet.ui.wizard.ui.TabbedSettingsEditorTab;
import org.jetbrains.plugins.ruby.ruby.RModuleUtil;

import javax.swing.*;

/**
 * @author Dennis.Ushakov
 */
public class RubyMotionGenerator extends RubyFrameworkProjectGenerator<RubyMotionSettingsHolder>
  implements DirectoryProjectGenerator<RubyMotionSettingsHolder>, HideableProjectGenerator {
  private static final String TITLE = RBundle.message("ruby.motion.application.settings");
  private RubyMotionSettingsHolder mySettings;

  @NotNull
  @Nls
  @Override
  public String getName() {
    return RBundle.message("ruby.motion.wizard.tab.project.generator.title");
  }

  @Nullable
  @Override
  public RubyMotionSettingsHolder showGenerationSettings(VirtualFile baseDir) throws ProcessCanceledException {
    final RubyMotionSettingsHolder settings = initSettings();
    final RubyMotionGeneratorTab generatorTab = new RubyMotionGeneratorTab(settings);

    final TabbedSettingsEditorTab[] tabs =
      new TabbedSettingsEditorTab[]{
        generatorTab
      };

    if (TabbedSettingsDialog.showDialog(TITLE, tabs) != DialogWrapper.OK_EXIT_CODE) {
      throw new ProcessCanceledException();
    }

    return settings;
  }

  @Nullable
  @Override
  public Icon getLogo() {
    return RubyIcons.RubyMotion.RubyMotion;
  }

  @Override
  public void generateProject(
    @NotNull final Project project,
    @NotNull final VirtualFile baseDir,
    final RubyMotionSettingsHolder settings,
    @NotNull final Module module)
  {
    final RubyMotionUtilImpl.ProjectType projectType = settings.getProjectType();
    final Sdk sdk = settings.getSdk();
    RModuleUtil.getInstance().changeModuleSdk(sdk, module);

    module.putUserData(RubyMotionUtilImpl.PROJECT_TYPE, projectType);
    RubyMotionFacetConfigurator.configure(baseDir, module);
    StartupManager.getInstance(project).runWhenProjectIsInitialized(new Runnable() {
      @Override
      public void run() {
        ((RubyMotionUtilImpl)RubyMotionUtil.getInstance()).generateApp(baseDir, module, sdk, projectType, settings.isUseCalabash());
        RModuleUtil.getInstance().refreshRubyModuleTypeContent(module);
      }
    });
  }

  @NotNull
  @Override
  public ValidationResult validate(@NotNull String baseDirPath) {
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
  public JPanel createSettingsPanel(RubyMotionSettingsHolder settings) {
    final RubyMotionGeneratorTab generatorTab = new RubyMotionGeneratorTab(settings);
    mySettings = settings;
    final JComponent generatorTabComponent = generatorTab.createComponent();
    return (JPanel)generatorTabComponent;
  }

  @NotNull
  @Override
  public RubyMotionSettingsHolder getSavedSettings() {
    return mySettings;
  }
}