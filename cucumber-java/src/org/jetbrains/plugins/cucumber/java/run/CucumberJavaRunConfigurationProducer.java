// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.plugins.cucumber.java.run;

import com.intellij.execution.JavaExecutionUtil;
import com.intellij.execution.JavaRunConfigurationExtensionManager;
import com.intellij.execution.Location;
import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.junit.JavaRunConfigurationProducerBase;
import com.intellij.execution.junit2.info.LocationUtil;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.NullableComputable;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.GlobalSearchScopesCore;
import com.intellij.psi.search.searches.AnnotatedElementsSearch;
import com.intellij.util.Query;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.cucumber.java.CucumberJavaUtil;
import org.jetbrains.plugins.cucumber.psi.GherkinFileType;

import java.util.Collection;
import java.util.Set;

public abstract class CucumberJavaRunConfigurationProducer extends JavaRunConfigurationProducerBase<CucumberJavaRunConfiguration> implements Cloneable {
  public static final String FORMATTER_OPTIONS_1_0 = " --format org.jetbrains.plugins.cucumber.java.run.CucumberJvmSMFormatter --monochrome";
  public static final String FORMATTER_OPTIONS_1_2 = " --plugin org.jetbrains.plugins.cucumber.java.run.CucumberJvmSMFormatter --monochrome";
  public static final String FORMATTER_OPTIONS_2 = " --plugin org.jetbrains.plugins.cucumber.java.run.CucumberJvm2SMFormatter --monochrome";
  public static final String FORMATTER_OPTIONS_3 = " --plugin org.jetbrains.plugins.cucumber.java.run.CucumberJvm3SMFormatter";
  public static final String CUCUMBER_1_0_MAIN_CLASS = "cucumber.cli.Main";
  public static final String CUCUMBER_1_1_MAIN_CLASS = "cucumber.api.cli.Main";
  public static final String CUCUMBER_1_2_PLUGIN_CLASS = "cucumber.api.Plugin";
  public static final String CUCUMBER_2_CLASS_MARKER = "cucumber.api.formatter.Formatter";
  public static final String CUCUMBER_3_CLASS_MARKER = "cucumber.runner.TestCase";

  private static final String CUCUMBER_CORE_VERSION_3 = "3";
  private static final String CUCUMBER_CORE_VERSION_2 = "2";
  private static final String CUCUMBER_CORE_VERSION_1_2 = "1.2";
  private static final String CUCUMBER_CORE_VERSION_1_0 = "1";

  public static final Set<String> HOOK_ANNOTATION_NAMES = ContainerUtil.newHashSet("cucumber.annotation.Before",
                                                                                   "cucumber.annotation.After",
                                                                                   "cucumber.api.java.Before",
                                                                                   "cucumber.api.java.After");

  @NotNull
  @Override
  public ConfigurationFactory getConfigurationFactory() {
    return CucumberJavaRunConfigurationType.getInstance().getConfigurationFactories()[0];
  }

  @Nullable
  protected abstract NullableComputable<String> getStepsGlue(@NotNull final PsiElement element);

  protected abstract String getConfigurationName(@NotNull ConfigurationContext context);

  protected String getNameFilter(@NotNull ConfigurationContext context) {
    return "";
  }

  @Nullable
  protected abstract VirtualFile getFileToRun(ConfigurationContext context);

  @Override
  protected boolean setupConfigurationFromContext(CucumberJavaRunConfiguration configuration,
                                                  ConfigurationContext context,
                                                  Ref<PsiElement> sourceElement) {
    final VirtualFile virtualFile = getFileToRun(context);
    if (virtualFile == null) {
      return false;
    }

    final Project project = configuration.getProject();
    final PsiElement element = context.getPsiLocation();

    if (element == null) {
      return false;
    }

    final Module module = ModuleUtilCore.findModuleForFile(virtualFile, project);
    if (module == null) return false;

    if (virtualFile.isDirectory()) {
      if (!FileTypeIndex.containsFileOfType(GherkinFileType.INSTANCE, GlobalSearchScopesCore.directoryScope(project, virtualFile, true))) {
        return false;
      }
    }

    String mainClassName = null;
    String formatterOptions = null;
    String cucumberCoreVersion;
    final Location location = context.getLocation();
    if (location != null) {
      if (LocationUtil.isJarAttached(location, PsiDirectory.EMPTY_ARRAY, CUCUMBER_1_0_MAIN_CLASS)) {
        mainClassName = CUCUMBER_1_0_MAIN_CLASS;
        cucumberCoreVersion = CUCUMBER_CORE_VERSION_1_0;
      } else {
        mainClassName = CUCUMBER_1_1_MAIN_CLASS;
        cucumberCoreVersion = getCucumberCoreVersion(location);
      }

      configuration.setCucumberCoreVersion(cucumberCoreVersion);
      formatterOptions = getSMFormatterOptions(cucumberCoreVersion);
    }
    if (mainClassName == null) {
      return false;
    }

    final VirtualFile file = getFileToRun(context);
    if (file == null) {
      return false;
    }
    if (StringUtil.isEmpty(configuration.getGlue())) {
      final NullableComputable<String> glue = getStepsGlue(element);
      configuration.setGlue(glue);
    }
    configuration.setNameFilter(getNameFilter(context));
    configuration.setFilePath(file.getPath());
    String programParametersFromDefaultConfiguration = StringUtil.defaultIfEmpty(configuration.getProgramParameters(), "");
    configuration.setProgramParameters(programParametersFromDefaultConfiguration + formatterOptions);

    if (configuration.getMainClassName() == null) {
      configuration.setMainClassName(mainClassName);
    }

    if (configuration.getNameFilter() != null && configuration.getNameFilter().length() > 0) {
      final String newProgramParameters = configuration.getProgramParameters() + " --name \"" + configuration.getNameFilter() + "\"";
      configuration.setProgramParameters(newProgramParameters);
    }

    configuration.setSuggestedName(getConfigurationName(context));
    configuration.setGeneratedName();

    setupConfigurationModule(context, configuration);
    JavaRunConfigurationExtensionManager.getInstance().extendCreatedConfiguration(configuration, location);
    return true;
  }

  @Override
  public boolean isConfigurationFromContext(CucumberJavaRunConfiguration runConfiguration, ConfigurationContext context) {
    Location location = context.getLocation();
    if (location == null) {
      return false;
    }
    final Location classLocation = JavaExecutionUtil.stepIntoSingleClass(location);
    if (classLocation == null) {
      return false;
    }

    final VirtualFile fileToRun = getFileToRun(context);
    if (fileToRun == null) {
      return false;
    }

    if (!fileToRun.getPath().equals(runConfiguration.getFilePath())) {
      return false;
    }

    if (!Comparing.strEqual(getNameFilter(context), runConfiguration.getNameFilter())) {
      return false;
    }

    final Module configurationModule = runConfiguration.getConfigurationModule().getModule();
    if (!Comparing.equal(classLocation.getModule(), configurationModule)) {
      return false;
    }

    return true;
  }

  protected Set<String> getHookGlue(final PsiElement element) {
    final Set<String> packages = ContainerUtil.newLinkedHashSet();

    final Module module = ModuleUtilCore.findModuleForPsiElement(element);
    if (module == null) {
      return packages;
    }

    final JavaPsiFacade javaPsiFacade = JavaPsiFacade.getInstance(element.getProject());
    final GlobalSearchScope dependenciesScope = module.getModuleWithDependenciesAndLibrariesScope(true);

    for (final String fullyQualifiedAnnotationName : HOOK_ANNOTATION_NAMES) {
      final PsiClass psiClass = javaPsiFacade.findClass(fullyQualifiedAnnotationName, dependenciesScope);

      if (psiClass != null) {
        final Query<PsiMethod> psiMethods = AnnotatedElementsSearch
          .searchPsiMethods(psiClass, GlobalSearchScope.allScope(element.getProject()));
        final Collection<PsiMethod> methods = psiMethods.findAll();
        addPackagesOfMethods(methods, packages);
      }
    }

    return packages;
  }

  private static void addPackagesOfMethods(final Collection<PsiMethod> psiMethods, final Set<String> packages) {
    for (final PsiMethod psiMethod : psiMethods) {
      final PsiClassOwner file = (PsiClassOwner)psiMethod.getContainingFile();
      final String packageName = file.getPackageName();
      if (StringUtil.isNotEmpty(packageName)) {
        CucumberJavaUtil.addGlue(packageName, packages);
      }
    }
  }

  @NotNull
  private static String getCucumberCoreVersion(@NotNull Location location) {
    if (LocationUtil.isJarAttached(location, PsiDirectory.EMPTY_ARRAY, CUCUMBER_3_CLASS_MARKER)) {
      return CUCUMBER_CORE_VERSION_3;
    } else if (LocationUtil.isJarAttached(location, PsiDirectory.EMPTY_ARRAY, CUCUMBER_2_CLASS_MARKER)) {
      return CUCUMBER_CORE_VERSION_2;
    } else if (LocationUtil.isJarAttached(location, PsiDirectory.EMPTY_ARRAY, CUCUMBER_1_2_PLUGIN_CLASS))  {
      return CUCUMBER_CORE_VERSION_1_2;
    }
    return CUCUMBER_CORE_VERSION_3;
  }

  @NotNull
  private static String getSMFormatterOptions(@NotNull String cucumberCoreVersion) {
    if (cucumberCoreVersion.equals(CUCUMBER_CORE_VERSION_1_0)) {
      return FORMATTER_OPTIONS_1_0;
    } else if (cucumberCoreVersion.equals(CUCUMBER_CORE_VERSION_1_2)) {
      return FORMATTER_OPTIONS_1_2;
    } else if (cucumberCoreVersion.equals(CUCUMBER_CORE_VERSION_2)) {
      return FORMATTER_OPTIONS_2;
    } else {
      return FORMATTER_OPTIONS_3;
    }
  }
}
