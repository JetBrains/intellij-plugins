// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.plugins.groovy.griffon;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.module.Module;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.groovy.mvc.MvcActionBase;
import org.jetbrains.plugins.groovy.mvc.MvcFramework;

/**
 * @author peter
 */
public class UpdateGriffonSettingsAction extends MvcActionBase {

  @Override
  protected boolean isFrameworkSupported(@NotNull MvcFramework framework) {
    return framework == GriffonFramework.getInstance();
  }

  @Override
  protected void actionPerformed(@NotNull AnActionEvent e, @NotNull final Module module, @NotNull MvcFramework framework) {
    GriffonFramework.getInstance().updateProjectStructure(module);
  }

  @Override
  protected void updateView(AnActionEvent event, @NotNull MvcFramework framework, @NotNull Module module) {
    event.getPresentation().setIcon(AllIcons.Actions.Refresh);
  }

}
