// Copyright 2000-2018 JetBrains s.r.o.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.intellij.lang.javascript.linter.tslint;

import com.intellij.lang.javascript.linter.JSLinterConfigFileUtil;
import com.intellij.lang.javascript.linter.tslint.config.TsLintSetupDetector;
import com.intellij.lang.javascript.linter.tslint.config.TsLintState;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.List;

public class TslintUtil {
  private TslintUtil() {
  }

  public static final Logger LOG = Logger.getInstance("#com.intellij.lang.javascript.linter.tslint.TsLint");
  public static final String PACKAGE_NAME = "tslint";
  public static final String TSLINT_JSON = "tslint.json";
  public static final String TYPESCRIPT_PLUGIN_OLD_PACKAGE_NAME = "tslint-language-service";
  public static final String TYPESCRIPT_PLUGIN_PACKAGE_NAME = "typescript-tslint-plugin";

  public static final String[] CONFIG_FILE_NAMES = new String[]{TSLINT_JSON, "tslint.yaml", "tslint.yml"};

  public static boolean isConfigFile(@NotNull VirtualFile file) {
    if (!file.isValid() || file.isDirectory()) {
      return false;
    }
    CharSequence name = file.getNameSequence();
    for (String fileName : CONFIG_FILE_NAMES) {
      if (StringUtil.equals(name, fileName)) {
        return true;
      }
    }
    return false;
  }

  public static boolean hasConfigFiles(@NotNull Project project) {
    return JSLinterConfigFileUtil.hasConfigFiles(project, CONFIG_FILE_NAMES);
  }

  @NotNull
  public static List<VirtualFile> findAllConfigsInScope(@NotNull Project project) {
    return JSLinterConfigFileUtil.findAllConfigs(project, CONFIG_FILE_NAMES);
  }

  @Nullable
  public static VirtualFile getConfig(@NotNull TsLintState state, @NotNull Project project, @NotNull VirtualFile virtualFile) {
    return doGetConfig(state, project, virtualFile);
  }

  /**
   * @deprecated Use {@link #lookupConfig(Project, VirtualFile)} instead
   */
  @Deprecated
  @ApiStatus.ScheduledForRemoval(inVersion = "2019.3")
  public static VirtualFile getConfig(@NotNull TsLintState state, @NotNull VirtualFile virtualFile) {
    return doGetConfig(state, null, virtualFile);
  }

  private static VirtualFile doGetConfig(@NotNull TsLintState state, @Nullable Project project, @NotNull VirtualFile virtualFile) {
    if (state.isCustomConfigFileUsed()) {
      final String configFilePath = state.getCustomConfigFilePath();
      if (StringUtil.isEmptyOrSpaces(configFilePath)) {
        return null;
      }
      final File configFile = new File(configFilePath);
      return VfsUtil.findFileByIoFile(configFile, false);
    }

    return project != null ? lookupConfig(project, virtualFile) : lookupConfig(virtualFile);
  }

  @Nullable
  public static VirtualFile lookupConfig(@NotNull Project project, @NotNull VirtualFile virtualFile) {
    for (TsLintSetupDetector detector : TsLintSetupDetector.TS_LINT_SETUP_DETECTOR_EP.getExtensionList()) {
      TsLintSetupDetector.TsLintSetup setup = detector.detectSetup(project, virtualFile);
      if (setup != null) {
        return setup.getTsLintConfig();
      }
    }
    return null;
  }

  /**
   * @deprecated Use {@link #lookupConfig(Project, VirtualFile)} instead
   */
  @Deprecated
  @ApiStatus.ScheduledForRemoval(inVersion = "2019.3")
  @Nullable
  public static VirtualFile lookupConfig(@NotNull VirtualFile virtualFile) {
    return JSLinterConfigFileUtil.findFileUpToFileSystemRoot(virtualFile, CONFIG_FILE_NAMES);
  }
}
