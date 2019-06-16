// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.plugins.ruby.motion;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.startup.StartupManager;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.platform.HideableProjectGenerator;
import com.intellij.platform.ProjectGeneratorPeer;
import icons.RubyIcons;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.ruby.RBundle;
import org.jetbrains.plugins.ruby.motion.ui.RubyMotionSettingsHolder;
import org.jetbrains.plugins.ruby.ruby.RModuleUtil;
import org.jetbrains.plugins.ruby.wizard.RubyFrameworkProjectGenerator;
import org.jetbrains.plugins.ruby.wizard.RubyProjectSharedSettings;

import javax.swing.*;

/**
 * @author Dennis.Ushakov
 */
public class RubyMotionGenerator extends RubyFrameworkProjectGenerator<RubyMotionSettingsHolder> implements HideableProjectGenerator {
  @NotNull
  @Override
  protected ProjectGeneratorPeer<RubyMotionSettingsHolder> createPeer(@NotNull RubyProjectSharedSettings sharedSettings) {
    return new RubyMotionGeneratorPeer(sharedSettings);
  }

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

  @Override
  public boolean isHidden() {
    return !SystemInfo.isMac;
  }

  @NotNull
  @Override
  public String getParentGroupName() {
    return "Mobile";
  }
}