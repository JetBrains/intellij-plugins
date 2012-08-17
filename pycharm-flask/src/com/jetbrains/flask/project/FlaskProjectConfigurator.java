/*
 * Copyright 2000-2012 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jetbrains.flask.project;

import com.intellij.openapi.fileEditor.impl.LoadTextUtil;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.platform.DirectoryProjectConfigurator;
import com.jetbrains.flask.codeInsight.FlaskNames;
import com.jetbrains.python.PythonFileType;
import com.jetbrains.python.packaging.PyPackageManagers;
import com.jetbrains.python.packaging.PyRequirement;
import com.jetbrains.python.run.PyRunConfigurationFactory;
import com.jetbrains.python.templateLanguages.TemplatesService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * @author yole
 */
public class FlaskProjectConfigurator implements DirectoryProjectConfigurator {
  @Override
  public void configureProject(Project project, @NotNull VirtualFile baseDir, Ref<Module> moduleRef) {
    Module module = moduleRef.get();
    if (module == null) return;
    VirtualFile appFile = findFlaskAppFile(baseDir);
    if (projectRequiresFlask(module) || appFile != null) {
      TemplatesService templatesService = TemplatesService.getInstance(module);
      templatesService.setTemplateLanguage(TemplatesService.JINJA2);
      VirtualFile templatesDir = baseDir.findChild(FlaskNames.TEMPLATES);
      if (templatesDir != null) {
        templatesService.setTemplateFolders(templatesDir);
      }
      if (appFile != null) {
        PyRunConfigurationFactory.getInstance().createPythonScriptRunConfiguration(module, appFile.getPath());
      }
    }
  }

  @Nullable
  public static VirtualFile findFlaskAppFile(VirtualFile dir) {
    VirtualFile[] children = dir.getChildren();
    for (VirtualFile child : children) {
      if (!child.isDirectory() && child.getFileType() instanceof PythonFileType) {
        CharSequence text = LoadTextUtil.loadText(child);
        if (text.toString().contains("Flask(__name__)")) {
          return child;
        }
      }
    }
    return null;
  }

  private static boolean projectRequiresFlask(Module module) {
    List<PyRequirement> requirements = PyPackageManagers.getInstance().getRequirementsFromTxt(module);
    if (requirements != null) {
      for (PyRequirement requirement : requirements) {
        if (requirement.getName().equals("Flask")) {
          return true;
        }
      }
    }
    return false;
  }
}
