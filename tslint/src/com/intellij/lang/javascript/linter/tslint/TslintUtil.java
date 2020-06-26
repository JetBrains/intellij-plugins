// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.lang.javascript.linter.tslint;

import com.intellij.lang.javascript.linter.JSLinterConfigFileUtil;
import com.intellij.lang.javascript.linter.tslint.config.TsLintConfigDetector;
import com.intellij.lang.javascript.linter.tslint.config.TsLintState;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.List;

public final class TslintUtil {
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

  private static VirtualFile doGetConfig(@NotNull TsLintState state, @NotNull Project project, @NotNull VirtualFile virtualFile) {
    if (state.isCustomConfigFileUsed()) {
      final String configFilePath = state.getCustomConfigFilePath();
      if (StringUtil.isEmptyOrSpaces(configFilePath)) {
        return null;
      }
      final File configFile = new File(configFilePath);
      return VfsUtil.findFileByIoFile(configFile, false);
    }

    return lookupConfig(project, virtualFile);
  }

  @Nullable
  public static VirtualFile lookupConfig(@NotNull Project project, @NotNull VirtualFile virtualFile) {
    for (TsLintConfigDetector detector : TsLintConfigDetector.TS_LINT_CONFIG_DETECTOR_EP.getExtensionList()) {
      TsLintConfigDetector.TsLintConfigs setup = detector.detectConfigs(project, virtualFile);
      if (setup != null) {
        return setup.getTsLintConfig();
      }
    }
    return null;
  }
}
