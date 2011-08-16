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
package com.google.jstestdriver.idea.config;

import java.io.File;
import javax.swing.Icon;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.YAMLLanguage;

import com.google.jstestdriver.idea.execution.JstdRunConfiguration;
import com.google.jstestdriver.idea.execution.JstdConfigurationType;
import com.intellij.execution.RunManager;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.fileTypes.ex.FileTypeIdentifiableByVirtualFile;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;

public class OtherConfigFileType extends LanguageFileType implements FileTypeIdentifiableByVirtualFile {

  public static final OtherConfigFileType INSTANCE = new OtherConfigFileType();

  private static final String REQUIRED_FILE_EXTENSION = "conf";
  private static final String[] SUITABLE_FILE_NAMES = new String[]{"JsTestDriver", "js-test-driver"};

  protected OtherConfigFileType() {
    super(YAMLLanguage.INSTANCE);
  }

  @Override
  public boolean isMyFileType(VirtualFile file) {
    if (REQUIRED_FILE_EXTENSION.equals(file.getExtension())) {
      if (isSuitableFileName(file.getNameWithoutExtension())) {
        return true;
      }
      if (isReferencedByJstdRunConfiguration(file)) {
        return true;
      }
    }
    return false;
  }

  private static boolean isReferencedByJstdRunConfiguration(VirtualFile file) {
    Project[] projects = ProjectManager.getInstance().getOpenProjects();
    for (Project project : projects) {
      RunManager runManager = RunManager.getInstance(project);
      RunConfiguration[] runConfigurations = runManager.getConfigurations(JstdConfigurationType.getInstance());
      for (RunConfiguration runConfiguration : runConfigurations) {
        if (runConfiguration instanceof JstdRunConfiguration) {
          JstdRunConfiguration jstdConfiguration = (JstdRunConfiguration)runConfiguration;
          File targetIOFile = new File(jstdConfiguration.getRunSettings().getConfigFile());
          VirtualFile vf = LocalFileSystem.getInstance().findFileByIoFile(targetIOFile);
          if (vf == file) {
            return true;
          }
        }
      }
    }
    return false;
  }

  private static boolean isSuitableFileName(String fileNameWithoutExtension) {
    for (String name : SUITABLE_FILE_NAMES) {
      if (name.equalsIgnoreCase(fileNameWithoutExtension)) {
        return true;
      }
    }
    return false;
  }

  @NotNull
  @Override
  public String getName() {
    return "used JsTestDriver config";
  }

  @NotNull
  @Override
  public String getDescription() {
    return "JsTestDriver configuration file referred by JsTestDriver Run Configuration";
  }

  @NotNull
  @Override
  public String getDefaultExtension() {
    return "fakeJstdConfigExtension";
  }

  @Override
  public Icon getIcon() {
    return JstdConfigFileType.INSTANCE.getIcon();
  }
}
