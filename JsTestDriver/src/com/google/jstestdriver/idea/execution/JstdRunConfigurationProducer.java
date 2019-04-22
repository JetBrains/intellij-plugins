// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.google.jstestdriver.idea.execution;

import com.google.jstestdriver.idea.config.JstdConfigFileUtils;
import com.google.jstestdriver.idea.config.JstdTestFilePathIndex;
import com.google.jstestdriver.idea.execution.settings.JstdRunSettings;
import com.google.jstestdriver.idea.execution.settings.ServerType;
import com.google.jstestdriver.idea.execution.settings.TestType;
import com.intellij.execution.Location;
import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.actions.LazyRunConfigurationProducer;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.javascript.testFramework.JstdRunElement;
import com.intellij.javascript.testFramework.TestFileStructureManager;
import com.intellij.javascript.testFramework.TestFileStructurePack;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.ObjectUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public final class JstdRunConfigurationProducer extends LazyRunConfigurationProducer<JstdRunConfiguration> {
  private static final Logger LOG = Logger.getInstance(JstdRunConfigurationProducer.class);

  private static final JstdRunSettingsProvider[] RUN_SETTINGS_PROVIDERS = {
      new JstdDirectoryRunSettingsProvider(),
      new JstdConfigFileRunSettingsProvider(),
      new TestElementRunSettingsProvider(),
      new JsFileRunSettingsProvider()
  };

  @NotNull
  @Override
  public ConfigurationFactory getConfigurationFactory() {
    return JstdConfigurationType.getInstance().getConfigurationFactories()[0];
  }

  private static void logTakenTime(String actionName, long startTimeNano, String... args) {
    final long NANO_IN_MS = 1000000;
    long durationNano = System.nanoTime() - startTimeNano;
    if (durationNano > 100 * NANO_IN_MS) {
      String message = String.format("[JsTD] Time taken by '" + actionName + "': %.2f ms, extra args: %s\n",
                                     durationNano / (1.0 * NANO_IN_MS),
                                     Arrays.toString(args)
      );
      LOG.info(message);
    }
  }

  private static void logDoneCreateConfigurationByElement(long startTimeNano, String... args) {
    logTakenTime("createConfigurationByElement", startTimeNano, args);
  }

  @Nullable
  private static JstdRunSettings buildRunSettingsContext(@Nullable Location<?> location) {
    if (location != null) {
      PsiElement element = location.getPsiElement();
      JstdRunSettings runSettings = findJstdRunSettings(element);
      if (runSettings != null) {
        return runSettings;
      }
    }
    return null;
  }

  @Nullable
  private static JstdRunSettings findJstdRunSettings(@NotNull PsiElement element) {
    for (JstdRunSettingsProvider jstdRunSettingsProvider : RUN_SETTINGS_PROVIDERS) {
      JstdRunSettings runSettings = jstdRunSettingsProvider.provideSettings(element);
      if (runSettings != null) {
        return runSettings;
      }
    }
    return null;
  }

  @Override
  protected boolean setupConfigurationFromContext(@NotNull JstdRunConfiguration configuration,
                                                  @NotNull ConfigurationContext context,
                                                  @NotNull Ref<PsiElement> sourceElement) {
    Project project = configuration.getProject();
    if (!JstdSettingsUtil.areJstdConfigFilesInProjectCached(project)) {
      return false;
    }
    RunConfiguration original = context.getOriginalConfiguration(null);
    if (original != null && original.getType() != JstdConfigurationType.getInstance()) {
      return false;
    }
    long startTimeNano = System.nanoTime();
    JstdRunSettings settings = buildRunSettingsContext(context.getLocation());
    if (settings == null) {
      logDoneCreateConfigurationByElement(startTimeNano, "1");
      return false;
    }

    if (settings.getConfigFile().isEmpty()) {
      JstdRunSettings clonedSettings = configuration.getRunSettings();
      JstdRunSettings.Builder builder = new JstdRunSettings.Builder(settings);
      builder.setConfigFile(clonedSettings.getConfigFile());
      settings = builder.build();
    }
    configuration.setRunSettings(settings);

    String configurationName = configuration.resetGeneratedName();
    configuration.setName(configurationName);

    logDoneCreateConfigurationByElement(startTimeNano, "3");

    return true;
  }

  @Override
  public boolean isConfigurationFromContext(@NotNull JstdRunConfiguration configuration, @NotNull ConfigurationContext context) {
    Project project = configuration.getProject();
    if (!JstdSettingsUtil.areJstdConfigFilesInProjectCached(project)) {
      return false;
    }
    JstdRunSettings patternRunSettings = buildRunSettingsContext(context.getLocation());
    if (patternRunSettings == null) {
      return false;
    }
    JstdRunSettings candidateRunSettings = configuration.getRunSettings();
    TestType patternTestType = patternRunSettings.getTestType();
    if (patternTestType != candidateRunSettings.getTestType()) {
      return false;
    }
    if (patternTestType == TestType.ALL_CONFIGS_IN_DIRECTORY) {
      File dir1 = new File(patternRunSettings.getDirectory());
      File dir2 = new File(candidateRunSettings.getDirectory());
      if (dir1.isDirectory() && dir2.isDirectory() && FileUtil.filesEqual(dir1, dir2)) {
        return true;
      }
    } else if (patternTestType == TestType.CONFIG_FILE) {
      File configFilePattern = new File(patternRunSettings.getConfigFile());
      File configFileCandidate = new File(candidateRunSettings.getConfigFile());
      if (configFilePattern.isFile()
          && configFileCandidate.isFile()
          && FileUtil.filesEqual(configFilePattern, configFileCandidate)) {
        return true;
      }
    } else if (patternTestType == TestType.JS_FILE
               || patternTestType == TestType.TEST_CASE
               || patternTestType == TestType.TEST_METHOD) {
      File patternJsFile = new File(patternRunSettings.getJsFilePath());
      File candidateJsFile = new File(candidateRunSettings.getJsFilePath());
      boolean eq = candidateJsFile.isFile() && FileUtil.filesEqual(patternJsFile, candidateJsFile);
      if (patternTestType == TestType.TEST_CASE) {
        eq = eq && patternRunSettings.getTestCaseName().equals(candidateRunSettings.getTestCaseName());
      }
      if (patternTestType == TestType.TEST_METHOD) {
        eq = eq && patternRunSettings.getTestCaseName().equals(candidateRunSettings.getTestCaseName());
        eq = eq && patternRunSettings.getTestMethodName().equals(candidateRunSettings.getTestMethodName());
      }
      if (eq) {
        return true;
       }
    }
    return false;
  }

  private interface JstdRunSettingsProvider {
    @Nullable
    JstdRunSettings provideSettings(@NotNull PsiElement psiElement);
  }

  private static class JsFileRunSettingsProvider implements JstdRunSettingsProvider {

    @Override
    @Nullable
    public JstdRunSettings provideSettings(@NotNull PsiElement psiElement) {
      PsiFile psiFile = psiElement.getContainingFile();
      if (psiFile == null) {
        return null;
      }
      final VirtualFile virtualFile = psiFile.getVirtualFile();
      if (virtualFile == null || virtualFile.getFileType() != JavaScriptSupportLoader.JAVASCRIPT) {
        return null;
      }
      Project project = psiFile.getProject();
      if (!(psiFile instanceof JSFile && ((JSFile)psiFile).isTestFile())) {
        return null;
      }
      JstdRunSettings.Builder builder = new JstdRunSettings.Builder();
      builder.setTestType(TestType.JS_FILE);
      List<VirtualFile> jstdConfigFiles = JstdTestFilePathIndex.findConfigFilesInProject(virtualFile, project);
      if (jstdConfigFiles.size() == 1) {
        builder.setConfigFile(getPath(jstdConfigFiles.get(0)));
      }
      builder.setJSFilePath(getPath(virtualFile));
      builder.setServerType(ServerType.INTERNAL);
      return builder.build();
    }
  }

  private static class JstdConfigFileRunSettingsProvider implements JstdRunSettingsProvider {

    @Override
    public JstdRunSettings provideSettings(@NotNull PsiElement psiElement) {
      PsiFile psiFile = psiElement.getContainingFile();
      if (psiFile != null) {
        VirtualFile virtualFile = psiFile.getVirtualFile();
        if (virtualFile != null && JstdConfigFileUtils.isJstdConfigFile(virtualFile)) {
          JstdRunSettings.Builder builder = new JstdRunSettings.Builder();
          builder.setConfigFile(getPath(virtualFile))
                 .setServerType(ServerType.INTERNAL);
          return builder.build();
        }
      }
      return null;
    }
  }

  private static class JstdDirectoryRunSettingsProvider implements JstdRunSettingsProvider {

    @Override
    public JstdRunSettings provideSettings(@NotNull PsiElement psiElement) {
      PsiDirectory psiDirectory = ObjectUtils.tryCast(psiElement, PsiDirectory.class);
      if (psiDirectory == null) {
        return null;
      }
      VirtualFile directory = psiDirectory.getVirtualFile();
      boolean jstdConfigs = JstdSettingsUtil.areJstdConfigFilesInDirectory(psiDirectory.getProject(), directory);
      if (!jstdConfigs) {
        return null;
      }
      JstdRunSettings.Builder builder = new JstdRunSettings.Builder();
      builder.setTestType(TestType.ALL_CONFIGS_IN_DIRECTORY)
             .setDirectory(getPath(directory))
             .setServerType(ServerType.INTERNAL);
      return builder.build();
    }
  }

  private static class TestElementRunSettingsProvider implements JstdRunSettingsProvider {

    @Override
    public JstdRunSettings provideSettings(@NotNull PsiElement psiElement) {
      JSFile jsFile = ObjectUtils.tryCast(psiElement.getContainingFile(), JSFile.class);
      if (jsFile == null) {
        return null;
      }
      VirtualFile virtualFile = jsFile.getVirtualFile();
      if (virtualFile == null || virtualFile.getFileType() != JavaScriptSupportLoader.JAVASCRIPT) {
        return null;
      }
      TestFileStructurePack pack = TestFileStructureManager.fetchTestFileStructurePackByJsFile(jsFile);
      if (pack == null) {
        return null;
      }
      JstdRunElement jstdRunElement = pack.getJstdRunElement(psiElement);
      if (jstdRunElement != null) {
        Project project = jsFile.getProject();
        JstdRunSettings.Builder builder = new JstdRunSettings.Builder();
        builder.setJSFilePath(getPath(virtualFile));
        builder.setTestCaseName(jstdRunElement.getTestCaseName());
        List<VirtualFile> jstdConfigs = JstdTestFilePathIndex.findConfigFilesInProject(virtualFile, project);
        if (jstdConfigs.size() == 1) {
          builder.setConfigFile(getPath(jstdConfigs.get(0)));
        }
        String testMethodName = jstdRunElement.getTestMethodName();
        if (testMethodName != null) {
          builder.setTestType(TestType.TEST_METHOD);
          builder.setTestMethodName(testMethodName);
        } else {
          builder.setTestType(TestType.TEST_CASE);
        }
        return builder.build();
      }
      return null;
    }

  }

  private static String getPath(@NotNull VirtualFile virtualFile) {
    return FileUtil.toSystemDependentName(virtualFile.getPath());
  }

}
