/*
 * Copyright 2000-2013 JetBrains s.r.o.
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
package com.intellij.coldFusion.UI.config;

import com.intellij.coldFusion.CfmlBundle;
import com.intellij.coldFusion.model.files.CfmlFileType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.NonDefaultProjectConfigurable;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ContentIterator;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.FileContentUtil;
import com.intellij.util.indexing.FileBasedIndex;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collection;

/**
 * @author vnikolaenko
 */
public class CfmlProjectConfigurable
  implements SearchableConfigurable, NonDefaultProjectConfigurable, Configurable.NoScroll {

  private final Project myProject;
  private CfmlMappingsForm myForm;

  public CfmlProjectConfigurable(Project project) {
    myProject = project;
  }

  @NotNull
  public String getId() {
    return getHelpTopic();
  }

  public Runnable enableSearch(String option) {
    return null;
  }

  @Nls
  public String getDisplayName() {
    return "ColdFusion";
  }

  @NotNull
  public String getHelpTopic() {
    return "reference.plugin.settings.project.settings.cfml";
  }

  public JComponent createComponent() {
    myForm = new CfmlMappingsForm(myProject);
    return myForm.getContentPane();
  }

  public boolean isModified() {
    CfmlProjectConfiguration.State originalState = CfmlProjectConfiguration.getInstance(myProject).getState();
    CfmlProjectConfiguration.State currentState = new CfmlProjectConfiguration.State();
    myForm.applyTo(currentState);

    return !currentState.equals(originalState);
  }

  public void apply() throws ConfigurationException {
    CfmlProjectConfiguration.State currentState = new CfmlProjectConfiguration.State();
    myForm.applyTo(currentState);
    CfmlProjectConfiguration.getInstance(myProject).loadState(currentState);
    storeLanguageVersionWithProgress(myProject);
  }

  public void reset() {
    myForm.reset(CfmlProjectConfiguration.getInstance(myProject).getState());
  }

  public void disposeUIResources() {
    myForm = null;
  }

  public static void storeLanguageVersionWithProgress(final Project project) {
    Task.Backgroundable task = new Task.Backgroundable(project, CfmlBundle.message("applying.new.language.version.task.name"), false) {
      public void run(@NotNull ProgressIndicator indicator) {
        final Collection<VirtualFile> cfmlFiles = new ArrayList<VirtualFile>();
        final VirtualFile baseDir = project.getBaseDir();
        if (baseDir != null) {
          FileBasedIndex.getInstance().iterateIndexableFiles(new ContentIterator() {
            public boolean processFile(VirtualFile file) {
              if (CfmlFileType.INSTANCE == file.getFileType()) {
                cfmlFiles.add(file);
              }
              return true;
            }
          }, project, indicator);
        }
        ApplicationManager.getApplication().invokeAndWait(new Runnable() {
          public void run() {
            FileContentUtil.reparseFiles(project, cfmlFiles, true);
          }
        }, ModalityState.NON_MODAL);
      }
    };
    ProgressManager.getInstance().run(task);
  }
}
