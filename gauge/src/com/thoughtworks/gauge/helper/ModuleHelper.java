/*
 * Copyright (C) 2020 ThoughtWorks, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.thoughtworks.gauge.helper;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.thoughtworks.gauge.util.GaugeUtil;

public final class ModuleHelper {
  public boolean isGaugeModule(PsiElement element) {
    Module module = GaugeUtil.moduleForPsiElement(element);
    if (module == null) return false;

    return isGaugeModule(module);
  }

  public boolean isGaugeModule(Module module) {
    if (GaugeUtil.isGaugeModule(module)) return true;

    String moduleName = module.getName();
    if (moduleName.endsWith(".test") || moduleName.endsWith(".main")) {
      String parentModuleName = StringUtil.substringBeforeLast(moduleName, ".");
      ModuleManager moduleManager = ModuleManager.getInstance(module.getProject());
      Module parentModule = moduleManager.findModuleByName(parentModuleName);

      return parentModule != null && GaugeUtil.isGaugeModule(parentModule);
    }
    return false;
  }

  public Module getModule(PsiElement step) {
    return GaugeUtil.moduleForPsiElement(step);
  }

  public Module getModule(VirtualFile file, Project project) {
    return ModuleUtilCore.findModuleForFile(file, project);
  }

  public boolean isGaugeModule(VirtualFile file, Project project) {
    Module module = ModuleUtilCore.findModuleForFile(file, project);
    return module != null && isGaugeModule(module);
  }
}