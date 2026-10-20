// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.javascript.flex.projectStructure.model.impl;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.ModuleListener;
import com.intellij.openapi.project.Project;
import com.intellij.serviceContainer.AlreadyDisposedException;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service(Service.Level.PROJECT)
public final class FlexBuildConfigurationManagerFactory implements Disposable {
  private final Map<Module, FlexBuildConfigurationManagerImpl> myManagers = new ConcurrentHashMap<>();

  public FlexBuildConfigurationManagerFactory(@NotNull Project project) {
    project.getMessageBus().connect(this).subscribe(ModuleListener.TOPIC, new ModuleListener() {
      @Override
      public void beforeModuleRemoved(@NotNull Project project, @NotNull Module module) {
        for (Map.Entry<Module, FlexBuildConfigurationManagerImpl> entry : myManagers.entrySet()) {
          if (entry.getKey() != module) {
            entry.getValue().removeDependenciesOn(module);
          }
        }
      }

      @Override
      public void moduleRemoved(@NotNull Project project, @NotNull Module module) {
        myManagers.remove(module);
      }
    });
  }

  public @NotNull FlexBuildConfigurationManagerImpl getService(@NotNull Module module) {
    if (module.isDisposed()) {
      throw new AlreadyDisposedException("Module is already disposed: " + module);
    }
    return myManagers.computeIfAbsent(module, FlexBuildConfigurationManagerImpl::new);
  }

  @Override
  public void dispose() {
  }

  public static @NotNull FlexBuildConfigurationManagerFactory getInstance(@NotNull Project project) {
    return project.getService(FlexBuildConfigurationManagerFactory.class);
  }
}
