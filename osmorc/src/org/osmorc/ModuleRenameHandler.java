/*
 * Copyright (c) 2007-2009, Osmorc Development Team
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright notice, this list
 *       of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright notice, this
 *       list of conditions and the following disclaimer in the documentation and/or other
 *       materials provided with the distribution.
 *     * Neither the name of 'Osmorc Development Team' nor the names of its contributors may be
 *       used to endorse or promote products derived from this software without specific
 *       prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT
 * OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.osmorc;

import com.intellij.ProjectTopics;
import com.intellij.execution.RunManager;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.ModuleAdapter;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.util.Function;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.osmorc.run.OsgiConfigurationType;
import org.osmorc.run.OsgiRunConfiguration;
import org.osmorc.run.ui.SelectedBundle;

import java.util.List;

/**
 * @author Robert F. Beeger (robert@beeger.net)
 */
public class ModuleRenameHandler implements ProjectComponent {
  private final Project myProject;
  private final OsgiConfigurationType myConfigurationType;

  public ModuleRenameHandler(@NotNull Project project, @NotNull OsgiConfigurationType type) {
    myProject = project;
    myConfigurationType = type;
  }

  @Override
  public void projectOpened() { }

  @Override
  public void projectClosed() { }

  @NotNull
  @Override
  public String getComponentName() {
    return "ModuleChangeHandler";
  }

  @Override
  public void initComponent() {
    myProject.getMessageBus().connect(myProject).subscribe(ProjectTopics.MODULES, new ModuleAdapter() {
      @Override
      public void modulesRenamed(Project project, List<Module> modules, Function<Module, String> oldNameProvider) {
        final List<Pair<SelectedBundle, String>> pairs = ContainerUtil.newSmartList();

        for (Module module : modules) {
          String oldName = oldNameProvider.fun(module);
          for (RunConfiguration runConfiguration : RunManager.getInstance(myProject).getConfigurations(myConfigurationType)) {
            for (SelectedBundle bundle : ((OsgiRunConfiguration)runConfiguration).getBundlesToDeploy()) {
              if (bundle.isModule() && bundle.getName().equals(oldName)) {
                pairs.add(Pair.create(bundle, module.getName()));
                break;
              }
            }
          }
        }

        if (!pairs.isEmpty()) {
          ApplicationManager.getApplication().runWriteAction(new Runnable() {
            @Override
            public void run() {
              for (Pair<SelectedBundle, String> pair : pairs) {
                pair.first.setName(pair.second);
              }
            }
          });
        }
      }
    });
  }

  @Override
  public void disposeComponent() { }
}
