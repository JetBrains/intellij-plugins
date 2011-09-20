/*
 * Copyright 2009 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.jstestdriver.idea.javascript;

import com.google.jstestdriver.idea.javascript.navigation.NavigationRegistryBuilderImpl;
import com.intellij.lang.javascript.library.JSLibraryManager;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.libraries.scripting.ScriptingLibraryModel;
import com.intellij.openapi.startup.StartupManager;
import org.jetbrains.annotations.NotNull;

public class JSUnitTestingSupport implements ProjectComponent {

  private Project myProject;

  public JSUnitTestingSupport(Project project) {
    myProject = project;
  }

  @NotNull
  @Override
  public String getComponentName() {
    return JSUnitTestingSupport.class.getName();
  }

  @Override
  public void projectOpened() {
    StartupManager.getInstance(myProject).registerPostStartupActivity(new Runnable() {
      @Override
      public void run() {
        installLibrary();
        NavigationRegistryBuilderImpl.register();
      }
    });
  }

  private void installLibrary() {
    final JSLibraryManager libraryManager = ServiceManager.getService(myProject, JSLibraryManager.class);
    String libraryName = "JsTD Assertion Framework";
    ScriptingLibraryModel scriptingLibraryModel = libraryManager.getLibraryByName(libraryName);
    if (scriptingLibraryModel != null) {
      libraryManager.removeLibrary(scriptingLibraryModel);
      ApplicationManager.getApplication().runWriteAction(new Runnable() {
        @Override
        public void run() {
          libraryManager.commitChanges();
        }
      });
    }
  }

  @Override
  public void projectClosed() {}

  @Override
  public void initComponent() {}

  @Override
  public void disposeComponent() {}
}
