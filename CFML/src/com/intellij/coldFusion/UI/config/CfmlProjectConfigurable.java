// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.coldFusion.UI.config;

import com.intellij.coldFusion.CfmlBundle;
import com.intellij.coldFusion.model.files.CfmlFileType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.fileTypes.FileTypeRegistry;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.FileContentUtil;
import com.intellij.util.indexing.FileBasedIndex;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collection;

public class CfmlProjectConfigurable implements SearchableConfigurable, Configurable.NoScroll {

  private final Project myProject;
  private CfmlMappingsForm myForm;

  public CfmlProjectConfigurable(Project project) {
    myProject = project;
  }

  @Override
  @NotNull
  public String getId() {
    return getHelpTopic();
  }

  @Override
  public String getDisplayName() {
    return CfmlBundle.message("configurable.CfmlProjectConfigurable.display.name");
  }

  @Override
  @NotNull
  public String getHelpTopic() {
    return "reference.plugin.settings.project.settings.cfml";
  }

  @Override
  public JComponent createComponent() {
    myForm = new CfmlMappingsForm(myProject);
    return myForm.getContentPane();
  }

  @Override
  public boolean isModified() {
    CfmlProjectConfiguration.State originalState = CfmlProjectConfiguration.getInstance(myProject).getState();
    CfmlProjectConfiguration.State currentState = new CfmlProjectConfiguration.State();
    myForm.applyTo(currentState);

    return !currentState.equals(originalState);
  }

  @Override
  public void apply() throws ConfigurationException {
    CfmlProjectConfiguration.State currentState = new CfmlProjectConfiguration.State();
    myForm.applyTo(currentState);
    CfmlProjectConfiguration.getInstance(myProject).loadState(currentState);
    storeLanguageVersionWithProgress(myProject);
  }

  @Override
  public void reset() {
    myForm.reset(CfmlProjectConfiguration.getInstance(myProject).getState());
  }

  @Override
  public void disposeUIResources() {
    myForm = null;
  }

  public static void storeLanguageVersionWithProgress(final Project project) {
    Task.Backgroundable task = new Task.Backgroundable(project, CfmlBundle.message("applying.new.language.version.task.name"), false) {
      @Override
      public void run(@NotNull ProgressIndicator indicator) {
        final Collection<VirtualFile> cfmlFiles = new ArrayList<>();
        final VirtualFile baseDir = project.getBaseDir();
        if (baseDir != null) {
          FileBasedIndex.getInstance().iterateIndexableFiles(file -> {
            if (FileTypeRegistry.getInstance().isFileOfType(file, CfmlFileType.INSTANCE)) {
              cfmlFiles.add(file);
            }
            return true;
          }, project, indicator);
        }
        ApplicationManager.getApplication().invokeAndWait(() -> FileContentUtil.reparseFiles(project, cfmlFiles, true), ModalityState.NON_MODAL);
      }
    };
    ProgressManager.getInstance().run(task);
  }
}
