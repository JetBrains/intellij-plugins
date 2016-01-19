/*
 * Copyright 2000-2016 JetBrains s.r.o.
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
package com.jetbrains.lang.dart.ide.application.options;

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.openapi.application.ApplicationBundle;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.ui.IdeBorderFactory;

import javax.swing.*;

public class DartCodeStyleGenerationConfigurable implements Configurable {
  JPanel myPanel;

  private final CodeStyleSettings mySettings;

  private JCheckBox myInsertOverrideAnnotationCheckBox;

  public DartCodeStyleGenerationConfigurable(CodeStyleSettings settings) {
    mySettings = settings;
    myPanel.setBorder(IdeBorderFactory.createEmptyBorder(2, 2, 2, 2));
  }

  public JComponent createComponent() {
    return myPanel;
  }

  public void disposeUIResources() {
  }

  public String getDisplayName() {
    return ApplicationBundle.message("title.code.generation");
  }

  public String getHelpTopic() {
    return "reference.settingsdialog.IDE.globalcodestyle.codegen";
  }

  public void reset(CodeStyleSettings settings) {
    myInsertOverrideAnnotationCheckBox.setSelected(settings.INSERT_OVERRIDE_ANNOTATION);
  }

  public void reset() {
    reset(mySettings);
  }

  public void apply(CodeStyleSettings settings) throws ConfigurationException {
    settings.INSERT_OVERRIDE_ANNOTATION = myInsertOverrideAnnotationCheckBox.isSelected();

    for (Project project : ProjectManager.getInstance().getOpenProjects()) {
      DaemonCodeAnalyzer.getInstance(project).settingsChanged();
    }
  }

  public void apply() throws ConfigurationException {
    apply(mySettings);
  }

  public boolean isModified(CodeStyleSettings settings) {
    return isModified(myInsertOverrideAnnotationCheckBox, settings.INSERT_OVERRIDE_ANNOTATION);
  }

  public boolean isModified() {
    return isModified(mySettings);
  }

  private static boolean isModified(JCheckBox checkBox, boolean value) {
    return checkBox.isSelected() != value;
  }

}
