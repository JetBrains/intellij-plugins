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
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.extensions.Extensions;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.ModuleListener;
import com.intellij.openapi.project.Project;
import com.intellij.util.messages.MessageBusConnection;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Robert F. Beeger (robert@beeger.net)
 */
public class ModuleChangeHandler implements ProjectComponent {
  @NotNull
  private final Project project;
  @NotNull
  private final Map<Module, String> moduleNames;
  @NotNull
  private final ModuleChangeListener[] moduleChangeListeners;

  public ModuleChangeHandler(@NotNull Project project) {
    this.project = project;
    moduleNames = new HashMap<Module, String>();
    moduleChangeListeners = Extensions
      .getExtensions(new ExtensionPointName<ModuleChangeListener>("Osmorc.moduleChangeListener"));
  }

  public void projectOpened() {
  }

  public void projectClosed() {
  }

  @NotNull
  public String getComponentName() {
    return "ModuleChangeHandler";
  }

  public void initComponent() {
    MessageBusConnection connection = project.getMessageBus().connect(project);
    connection.subscribe(ProjectTopics.MODULES, new ModuleListener() {
      public void moduleAdded(Project project, Module module) {
        if (ModuleChangeHandler.this.project == project) {
          moduleNames.put(module, module.getName());
        }
      }

      public void beforeModuleRemoved(Project project, Module module) {
      }

      public void moduleRemoved(Project project, Module module) {
        if (ModuleChangeHandler.this.project == project) {
          moduleNames.remove(module);
          fireModuleRemoved(module);
        }
      }

      public void modulesRenamed(Project project, List<Module> modules) {
        assert modules != null;
        if (ModuleChangeHandler.this.project == project) {
          for (Module module : modules) {
            String oldName = moduleNames.get(module);
            if (oldName != null) {
              fireModuleRenamed(module, oldName);
            }
            else {
              throw new RuntimeException("Unknown module renamed " + module.getName());
            }
            moduleNames.put(module, module.getName());
          }
        }
      }
    });
  }

  private void fireModuleRemoved(@NotNull Module module) {
    for (ModuleChangeListener listener : moduleChangeListeners) {
      listener.moduleRemoved(module);
    }
  }

  private void fireModuleRenamed(@NotNull Module module, @NotNull String oldName) {
    for (ModuleChangeListener listener : moduleChangeListeners) {
      listener.moduleRenamed(module, oldName);
    }
  }

  public void disposeComponent() {
  }
}
