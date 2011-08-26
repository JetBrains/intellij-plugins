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

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.jstestdriver.idea.PluginResources;
import com.google.jstestdriver.idea.execution.JstdConfigurationType;
import com.google.jstestdriver.idea.execution.JstdRunConfiguration;
import com.intellij.execution.RunManager;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.fileTypes.ex.FileTypeIdentifiableByVirtualFile;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.YAMLLanguage;

import javax.swing.*;
import java.io.File;
import java.util.Arrays;
import java.util.Set;

public class JstdConfigFileType extends LanguageFileType implements FileTypeIdentifiableByVirtualFile {

  public static final JstdConfigFileType INSTANCE = new JstdConfigFileType();

  private static final Set<String> SUITABLE_FILE_NAMES_WITHOUT_EXTENSION;
  private static final Set<String> SUITABLE_FILE_EXTENSIONS;

  static {
    Function<String, String> lower = new Function<String, String>() {
      @Override
      public String apply(String s) {
        return s.toLowerCase();
      }
    };
    SUITABLE_FILE_NAMES_WITHOUT_EXTENSION = ImmutableSet.copyOf(Iterables.transform(Arrays.asList(
        "jsTestDriver", "js-test-driver", "js_test_driver"
    ), lower));
    SUITABLE_FILE_EXTENSIONS = ImmutableSet.copyOf(Iterables.transform(Arrays.asList(
        "conf", "yml", "yaml"
    ), lower));
  }

  /**
   * Creates a language file type for the specified language.
   */
  protected JstdConfigFileType() {
    super(YAMLLanguage.INSTANCE);
  }

  @NotNull
  @Override
  public String getName() {
    return "JsTestDriver";
  }

  @NotNull
  @Override
  public String getDescription() {
    return "JsTestDriver config file";
  }

  @NotNull
  @Override
  public String getDefaultExtension() {
    return "jstd";
  }

  @NotNull
  @Override
  public Icon getIcon() {
    return PluginResources.getJstdSmallIcon();
  }

  @Override
  public boolean isMyFileType(VirtualFile file) {
    if (SUITABLE_FILE_EXTENSIONS.contains(file.getExtension())) {
      if (SUITABLE_FILE_NAMES_WITHOUT_EXTENSION.contains(StringUtil.toLowerCase(file.getNameWithoutExtension()))) {
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

}
