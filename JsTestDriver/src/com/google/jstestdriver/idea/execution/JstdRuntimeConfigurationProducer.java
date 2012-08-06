package com.google.jstestdriver.idea.execution;

import com.google.jstestdriver.idea.assertFramework.JstdRunElement;
import com.google.jstestdriver.idea.assertFramework.TestFileStructureManager;
import com.google.jstestdriver.idea.assertFramework.TestFileStructurePack;
import com.google.jstestdriver.idea.config.JstdConfigFileUtils;
import com.google.jstestdriver.idea.config.JstdTestFilePathIndex;
import com.google.jstestdriver.idea.execution.settings.JstdRunSettings;
import com.google.jstestdriver.idea.execution.settings.ServerType;
import com.google.jstestdriver.idea.execution.settings.TestType;
import com.intellij.execution.Location;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.junit.RuntimeConfigurationProducer;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.index.JSIndexEntry;
import com.intellij.lang.javascript.index.JavaScriptIndex;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
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

public class JstdRuntimeConfigurationProducer extends RuntimeConfigurationProducer {

  private static final Logger LOG = Logger.getInstance(JstdRuntimeConfigurationProducer.class);

  private static final JstdRunSettingsProvider[] RUN_SETTINGS_PROVIDERS = {
      new JstdDirectoryRunSettingsProvider(),
      new JstdConfigFileRunSettingsProvider(),
      new TestElementRunSettingsProvider(),
      new JsFileRunSettingsProvider()
  };

  private PsiElement mySourceElement;

  public JstdRuntimeConfigurationProducer() {
    super(JstdConfigurationType.getInstance());
  }

  @Override
  public PsiElement getSourceElement() {
    return mySourceElement;
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

  private static void logDoneFindExistingByElement(long startTimeNano, String... args) {
    logTakenTime("findExistingByElement", startTimeNano, args);
  }

  @Override
  protected RunnerAndConfigurationSettings createConfigurationByElement(Location location, ConfigurationContext context) {
    long startTimeNano = System.nanoTime();
    @SuppressWarnings({"unchecked"})
    RunSettingsContext runSettingsContext = buildRunSettingsContext(location);
    if (runSettingsContext == null) {
      logDoneCreateConfigurationByElement(startTimeNano, "1");
      return null;
    }

    final RunnerAndConfigurationSettings runnerSettings = cloneTemplateConfiguration(location.getProject(), context);
    JstdRunConfiguration runConfiguration = ObjectUtils.tryCast(runnerSettings.getConfiguration(), JstdRunConfiguration.class);
    if (runConfiguration == null) {
      logDoneCreateConfigurationByElement(startTimeNano, "2");
      return null;
    }

    JstdRunSettings settings = runSettingsContext.myRunSettings;
    if (settings.getConfigFile().isEmpty()) {
      JstdRunSettings clonedSettings = runConfiguration.getRunSettings();
      JstdRunSettings.Builder builder = new JstdRunSettings.Builder(settings);
      builder.setConfigFile(clonedSettings.getConfigFile());
      settings = builder.build();
    }
    runConfiguration.setRunSettings(settings);

    mySourceElement = runSettingsContext.myPsiElement;
    runnerSettings.setName(runConfiguration.suggestedName());
    logDoneCreateConfigurationByElement(startTimeNano, "3");
    return runnerSettings;
  }

  @SuppressWarnings({"RawUseOfParameterizedType"})
  @Override
  protected RunnerAndConfigurationSettings findExistingByElement(final Location location,
                                                                 @NotNull final RunnerAndConfigurationSettings[] existingConfigurations,
                                                                 ConfigurationContext context) {
    long startTimeNano = System.nanoTime();
    RunSettingsContext runSettingsContext = buildRunSettingsContext(location);
    if (runSettingsContext == null) {
      logDoneFindExistingByElement(startTimeNano, "1");
      return null;
    }
    final JstdRunSettings runSettingsPattern = runSettingsContext.myRunSettings;
    RunnerAndConfigurationSettings bestRaCSettings = null;
    for (RunnerAndConfigurationSettings candidateRaCSettings : existingConfigurations) {
      JstdRunConfiguration runConfiguration = ObjectUtils.tryCast(candidateRaCSettings.getConfiguration(), JstdRunConfiguration.class);
      if (runConfiguration == null) {
        continue;
      }
      JstdRunSettings runSettingsCandidate = runConfiguration.getRunSettings();
      TestType patternTestType = runSettingsPattern.getTestType();
      if (patternTestType == runSettingsCandidate.getTestType()) {
        if (runSettingsPattern.getTestType() == TestType.ALL_CONFIGS_IN_DIRECTORY) {
          File dir1 = new File(runSettingsPattern.getDirectory());
          File dir2 = new File(runSettingsCandidate.getDirectory());
          if (dir1.isDirectory() && dir2.isDirectory() && dir1.equals(dir2)) {
            bestRaCSettings = candidateRaCSettings;
            break;
          }
        } else if (patternTestType == TestType.CONFIG_FILE) {
          File configFilePattern = new File(runSettingsPattern.getConfigFile());
          File configFileCandidate = new File(runSettingsCandidate.getConfigFile());
          if (configFilePattern.isFile() && configFileCandidate.isFile() && configFilePattern.equals(configFileCandidate)) {
            bestRaCSettings = candidateRaCSettings;
            break;
          }
        } else if (patternTestType == TestType.JS_FILE || patternTestType == TestType.TEST_CASE || patternTestType == TestType.TEST_METHOD) {
          File jsFilePattern = new File(runSettingsPattern.getJsFilePath());
          File jsFileCandidate = new File(runSettingsCandidate.getJsFilePath());
          boolean eq = jsFileCandidate.isFile() && jsFilePattern.equals(jsFileCandidate);
          if (patternTestType == TestType.TEST_CASE) {
            eq = eq && runSettingsPattern.getTestCaseName().equals(runSettingsCandidate.getTestCaseName());
          }
          if (patternTestType == TestType.TEST_METHOD) {
            eq = eq && runSettingsPattern.getTestMethodName().equals(runSettingsCandidate.getTestMethodName());
          }
          if (eq) {
            if (bestRaCSettings == null) {
              bestRaCSettings = candidateRaCSettings;
            } else {
              JstdRunConfiguration bestRunConfiguration = ObjectUtils.tryCast(bestRaCSettings.getConfiguration(), JstdRunConfiguration.class);
              if (bestRunConfiguration != null) {
                JstdRunSettings bestRunSettings = bestRunConfiguration.getRunSettings();
                if (!new File(bestRunSettings.getConfigFile()).isFile()) {
                  bestRaCSettings = candidateRaCSettings;
                }
              }
            }
          }
        }
      }
    }
    logDoneFindExistingByElement(startTimeNano, "2");
    return bestRaCSettings;
  }

  @Nullable
  private static RunSettingsContext buildRunSettingsContext(@Nullable Location<?> location) {
    if (location != null) {
      PsiElement element = location.getPsiElement();
      JstdRunSettings runSettings = findJstdRunSettings(element);
      if (runSettings != null) {
        return new RunSettingsContext(runSettings, element);
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
  public int compareTo(Object o) {
    return PREFERED;
  }

  private static class RunSettingsContext {
    private final JstdRunSettings myRunSettings;
    private final PsiElement myPsiElement;

    private RunSettingsContext(JstdRunSettings runSettings, PsiElement psiElement) {
      myRunSettings = runSettings;
      myPsiElement = psiElement;
    }
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
      JSIndexEntry entry = JavaScriptIndex.getInstance(project).getEntryForFile(psiFile);
      if (entry == null || !entry.isTestFile()) {
        return null;
      }
      JstdRunSettings.Builder builder = new JstdRunSettings.Builder();
      builder.setTestType(TestType.JS_FILE);
      List<VirtualFile> jstdConfigFiles = JstdTestFilePathIndex.findConfigFilesInProject(virtualFile, project);
      if (jstdConfigFiles.size() == 1) {
        builder.setConfigFile(jstdConfigFiles.get(0).getPath());
      }
      builder.setJSFilePath(virtualFile.getPath());
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
          builder.setConfigFile(virtualFile.getPath())
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
             .setDirectory(directory.getPath())
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
        builder.setJSFilePath(virtualFile.getPath());
        builder.setTestCaseName(jstdRunElement.getTestCaseName());
        List<VirtualFile> jstdConfigs = JstdTestFilePathIndex.findConfigFilesInProject(virtualFile, project);
        if (jstdConfigs.size() == 1) {
          builder.setConfigFile(jstdConfigs.get(0).getPath());
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

}
