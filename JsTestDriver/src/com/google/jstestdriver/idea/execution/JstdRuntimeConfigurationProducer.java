package com.google.jstestdriver.idea.execution;

import com.google.jstestdriver.idea.assertFramework.JstdRunElement;
import com.google.jstestdriver.idea.assertFramework.TestFileStructureManager;
import com.google.jstestdriver.idea.assertFramework.TestFileStructurePack;
import com.google.jstestdriver.idea.config.JstdConfigFileUtils;
import com.google.jstestdriver.idea.execution.settings.JstdConfigType;
import com.google.jstestdriver.idea.execution.settings.JstdRunSettings;
import com.google.jstestdriver.idea.execution.settings.ServerType;
import com.google.jstestdriver.idea.execution.settings.TestType;
import com.google.jstestdriver.idea.util.CastUtils;
import com.intellij.execution.Location;
import com.intellij.execution.PsiLocation;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.junit.RuntimeConfigurationProducer;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Arrays;
import java.util.StringTokenizer;

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
    long endTimeNano = System.nanoTime();
    String message = String.format("[JsTD] Time taken by '" + actionName + "': %.2f ms, extra args: %s\n",
      (endTimeNano - startTimeNano) / 1000000.0,
      Arrays.toString(args)
    );
    LOG.info(message);
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
    if (!(runnerSettings.getConfiguration() instanceof JstdRunConfiguration)) {
      logDoneCreateConfigurationByElement(startTimeNano, "2");
      return null;
    }
    JstdRunConfiguration runConfiguration = (JstdRunConfiguration) runnerSettings.getConfiguration();

    runConfiguration.setRunSettings(runSettingsContext.runSettings);

    mySourceElement = runSettingsContext.psiElement;
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
    final JstdRunSettings runSettingsPattern = runSettingsContext.runSettings;
    RunnerAndConfigurationSettings bestRaCSettings = null;
    for (RunnerAndConfigurationSettings candidateRaCSettings : existingConfigurations) {
      JstdRunConfiguration runConfiguration = CastUtils.tryCast(candidateRaCSettings.getConfiguration(), JstdRunConfiguration.class);
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
              JstdRunConfiguration bestRunConfiguration = CastUtils.tryCast(bestRaCSettings.getConfiguration(), JstdRunConfiguration.class);
              if (bestRunConfiguration != null) {
                JstdRunSettings bestRunSettings = bestRunConfiguration.getRunSettings();
                if (bestRunSettings.getConfigType() == JstdConfigType.GENERATED) {
                  bestRaCSettings = candidateRaCSettings;
                } else if (!new File(bestRunSettings.getConfigFile()).isFile()) {
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
  private static RunSettingsContext buildRunSettingsContext(@Nullable Location<PsiElement> location) {
    if (location instanceof PsiLocation) {
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

  private static class RunSettingsContext {
    final JstdRunSettings runSettings;
    final PsiElement psiElement;

    private RunSettingsContext(JstdRunSettings runSettings, PsiElement psiElement) {
      this.runSettings = runSettings;
      this.psiElement = psiElement;
    }
  }

  private interface JstdRunSettingsProvider {
    @Nullable
    JstdRunSettings provideSettings(@NotNull PsiElement psiElement);
  }

  private static class JsFileRunSettingsProvider implements JstdRunSettingsProvider {

    private static boolean matchTestSource(String s) {
      StringTokenizer st = new StringTokenizer(s, "-_");
      while (st.hasMoreTokens()) {
        String token = st.nextToken().toLowerCase();
        if ("test".equals(token)) {
          return true;
        }
      }
      return false;
    }

    @Override
    public JstdRunSettings provideSettings(@NotNull PsiElement psiElement) {
      PsiFile psiFile = psiElement.getContainingFile();
      if (psiFile == null) {
        return null;
      }
      final VirtualFile virtualFile = psiFile.getVirtualFile();
      if (virtualFile == null || virtualFile.getFileType() != JavaScriptSupportLoader.JAVASCRIPT) {
        return null;
      }
      VirtualFile entity = virtualFile;
      VirtualFile projectDir = psiElement.getProject().getBaseDir();
      boolean testFound = false;
      while (entity != null) {
        if (matchTestSource(entity.getName())) {
          testFound = true;
        }
        if (entity.equals(projectDir)) {
          break;
        }
        entity = entity.getParent();
      }
      if (entity == projectDir && testFound) {
        JstdRunSettings.Builder builder = new JstdRunSettings.Builder();
        builder.setTestType(TestType.JS_FILE);
        builder.setConfigType(JstdConfigType.GENERATED);
        builder.setJSFilePath(virtualFile.getPath());
        builder.setServerType(ServerType.INTERNAL);
        return builder.build();
      }
      return null;
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
      if (!(psiElement instanceof PsiDirectory)) {
        return null;
      }
      PsiDirectory psiDirectory = (PsiDirectory) psiElement;
      VirtualFile directory = psiDirectory.getVirtualFile();
      boolean jstdConfigs = JstdClientCommandLineBuilder.areJstdConfigFilesInDirectory(psiDirectory.getProject(), directory);
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
      JSFile jsFile = CastUtils.tryCast(psiElement.getContainingFile(), JSFile.class);
      VirtualFile virtualFile = jsFile != null ? jsFile.getVirtualFile() : null;
      if (virtualFile == null) {
        return null;
      }
      TestFileStructurePack pack = TestFileStructureManager.fetchTestFileStructurePackByJsFile(jsFile);
      if (pack != null) {
        JstdRunElement jstdRunElement = pack.getJstdRunElement(psiElement);
        if (jstdRunElement != null) {
          String testMethodName = jstdRunElement.getTestMethodName();
          if (testMethodName != null) {
            return new JstdRunSettings.Builder()
                .setTestType(TestType.TEST_METHOD)
                .setJSFilePath(virtualFile.getPath())
                .setTestCaseName(jstdRunElement.getTestCaseName())
                .setTestMethodName(testMethodName)
                .build();
          } else {
            return new JstdRunSettings.Builder()
                .setTestType(TestType.TEST_CASE)
                .setJSFilePath(virtualFile.getPath())
                .setTestCaseName(jstdRunElement.getTestCaseName())
                .build();
          }
        }
      }
      return null;
    }

  }

  @Override
  public int compareTo(Object o) {
    return PREFERED;
  }
}
