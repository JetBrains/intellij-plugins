package com.google.jstestdriver.idea.execution;

import com.google.jstestdriver.idea.assertFramework.TestFileStructureManager;
import com.google.jstestdriver.idea.assertFramework.TestFileStructurePack;
import com.google.jstestdriver.idea.config.JstdConfigFileUtils;
import com.google.jstestdriver.idea.execution.settings.JstdConfigType;
import com.google.jstestdriver.idea.execution.settings.JstdRunSettings;
import com.google.jstestdriver.idea.execution.settings.ServerType;
import com.google.jstestdriver.idea.execution.settings.TestType;
import com.google.jstestdriver.idea.javascript.navigation.NavigationRegistry;
import com.google.jstestdriver.idea.javascript.navigation.NavigationUtils;
import com.google.jstestdriver.idea.javascript.navigation.Test;
import com.google.jstestdriver.idea.javascript.navigation.TestCase;
import com.google.jstestdriver.idea.util.CastUtils;
import com.intellij.execution.Location;
import com.intellij.execution.PsiLocation;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.junit.RuntimeConfigurationProducer;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.List;
import java.util.StringTokenizer;

public class JstdRuntimeConfigurationProducer extends RuntimeConfigurationProducer {

  private static final JstdRunSettingsProvider[] RUN_SETTINGS_PROVIDERS = {
      new JstdDirectoryRunSettingsProvider(),
      new JstdConfigFileRunSettingsProvider(),
      new TestCaseRunSettingsProvider(),
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

  @Override
  protected RunnerAndConfigurationSettings createConfigurationByElement(Location location, ConfigurationContext context) {
    @SuppressWarnings({"unchecked"})
    RunSettingsContext runSettingsContext = buildRunSettingsContext(location);
    if (runSettingsContext == null) {
      return null;
    }

    final RunnerAndConfigurationSettings runnerSettings = cloneTemplateConfiguration(location.getProject(), context);
    if (!(runnerSettings.getConfiguration() instanceof JstdRunConfiguration)) {
      return null;
    }
    JstdRunConfiguration runConfiguration = (JstdRunConfiguration) runnerSettings.getConfiguration();

    runConfiguration.setRunSettings(runSettingsContext.runSettings);

    mySourceElement = runSettingsContext.psiElement;
    runnerSettings.setName(runConfiguration.suggestedName());
    return runnerSettings;
  }

  @SuppressWarnings({"RawUseOfParameterizedType"})
  @Override
  protected RunnerAndConfigurationSettings findExistingByElement(final Location location,
                                                                 @NotNull final RunnerAndConfigurationSettings[] existingConfigurations,
                                                                 ConfigurationContext context) {
    RunSettingsContext runSettingsContext = buildRunSettingsContext(location);
    if (runSettingsContext == null) {
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
    return bestRaCSettings;
  }

  @Nullable
  private RunSettingsContext buildRunSettingsContext(Location<PsiElement> location) {
    if (!(location instanceof PsiLocation)) return null;

    PsiElement element = location.getPsiElement();

    JstdRunSettings runSettings = findJstdRunSettings(element);
    if (runSettings != null) {
      return new RunSettingsContext(runSettings, element);
    }
    return null;
  }

  private JstdRunSettings findJstdRunSettings(PsiElement element) {
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

    private boolean matchTestSource(String s) {
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
      if (virtualFile == null || FileTypeManager.getInstance().getFileTypeByFile(virtualFile) != JavaScriptSupportLoader.JAVASCRIPT) {
        return null;
      }
      VirtualFile entity = virtualFile;
      VirtualFile projectDir = psiElement.getProject().getBaseDir();
      boolean testFound = false;
      while (entity != null) {
        if (matchTestSource(entity.getName())) {
          testFound = true;
        }
        System.out.println("Checking " + entity.getPath() + " ... testFound: " + testFound);
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
      if (psiFile == null) return null;
      final VirtualFile virtualFile = psiFile.getVirtualFile();
      if (virtualFile == null || !JstdConfigFileUtils.isJstdConfigFile(virtualFile)) {
        return null;
      }
      JstdRunSettings.Builder builder = new JstdRunSettings.Builder();
      builder.setConfigFile(virtualFile.getPath())
             .setServerType(ServerType.INTERNAL);
      return builder.build();
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
      List<VirtualFile> jstdConfigs = JstdClientCommandLineBuilder.INSTANCE.collectJstdConfigFilesInDirectory(directory);
      if (jstdConfigs.isEmpty()) {
        return null;
      }
      JstdRunSettings.Builder builder = new JstdRunSettings.Builder();
      builder.setTestType(TestType.ALL_CONFIGS_IN_DIRECTORY)
             .setDirectory(directory.getPath())
             .setServerType(ServerType.INTERNAL);
      return builder.build();
    }
  }

  private static class TestCaseRunSettingsProvider implements JstdRunSettingsProvider {

    @Override
    public JstdRunSettings provideSettings(@NotNull PsiElement psiElement) {
      if (!(psiElement.getContainingFile() instanceof JSFile)) {
        return null;
      }
      JSFile jsFile = (JSFile) psiElement.getContainingFile();
      VirtualFile virtualFile = jsFile.getVirtualFile();
      if (virtualFile == null) {
        return null;
      }
      TestFileStructurePack pack = TestFileStructureManager.getInstance().fetchTestFileStructurePackByJsFile(jsFile);
      if (true) {
        return null;
      }
      PsiElement current = psiElement;
      while (current != jsFile) {
        Object target = null;//navigationRegistry.getTarget(current);
        if (target instanceof TestCase) {
          TestCase testCase = (TestCase) target;
          return new JstdRunSettings.Builder()
              .setTestType(TestType.TEST_CASE)
              .setJSFilePath(virtualFile.getPath())
              .setTestCaseName(testCase.getName())
              .build();
        }
        if (target instanceof Test) {
          Test test = (Test) target;
          return new JstdRunSettings.Builder()
              .setTestType(TestType.TEST_METHOD)
              .setJSFilePath(virtualFile.getPath())
              .setTestCaseName(test.getTestCase().getName())
              .setTestMethodName(test.getName())
              .build();
        }
        current = current.getParent();
      }
      return null;
    }

  }

  @Override
  public int compareTo(Object o) {
    return PREFERED;
  }
}
