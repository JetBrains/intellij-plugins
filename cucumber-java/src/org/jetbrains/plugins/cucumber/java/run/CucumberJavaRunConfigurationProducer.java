// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.cucumber.java.run;

import com.intellij.execution.JavaExecutionUtil;
import com.intellij.execution.JavaRunConfigurationExtensionManager;
import com.intellij.execution.Location;
import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.junit.JavaRunConfigurationProducerBase;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.registry.Registry;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.GlobalSearchScopesCore;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.text.VersionComparatorUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;
import org.jetbrains.plugins.cucumber.psi.GherkinFileType;

import java.util.Set;

import static org.jetbrains.plugins.cucumber.java.CucumberJavaUtil.getCucumberMainClass;
import static org.jetbrains.plugins.cucumber.java.CucumberJavaVersionUtil.*;

public abstract class CucumberJavaRunConfigurationProducer extends JavaRunConfigurationProducerBase<CucumberJavaRunConfiguration> {
  private static final Logger LOG = Logger.getInstance(CucumberJavaRunConfigurationProducer.class);

  public static final String FORMATTER_OPTIONS_1_0 =
    " --format org.jetbrains.plugins.cucumber.java.run.CucumberJvmSMFormatter --monochrome";
  public static final String FORMATTER_OPTIONS_1_2 =
    " --plugin org.jetbrains.plugins.cucumber.java.run.CucumberJvmSMFormatter --monochrome";
  public static final String FORMATTER_OPTIONS_2 = " --plugin org.jetbrains.plugins.cucumber.java.run.CucumberJvm2SMFormatter --monochrome";
  public static final String FORMATTER_OPTIONS_3 = " --plugin org.jetbrains.plugins.cucumber.java.run.CucumberJvm3SMFormatter";
  public static final String FORMATTER_OPTIONS_4 = " --plugin org.jetbrains.plugins.cucumber.java.run.CucumberJvm4SMFormatter";
  public static final String FORMATTER_OPTIONS_5 = " --plugin org.jetbrains.plugins.cucumber.java.run.CucumberJvm5SMFormatter";

  /**
   * Cucumber v6 and newer come with a built-in `teamcity` formatter (which works for both TeamCity and JetBrains IDEs).
   *
   * @see <a href="https://youtrack.jetbrains.com/issue/IDEA-276468/Use-plugin-teamcity-with-Cucumber-JVM-v6">IDEA-276468</a>
   * @see <a href="https://github.com/cucumber/cucumber-jvm/blob/v7.22.2/cucumber-core/src/main/java/io/cucumber/core/plugin/TeamCityPlugin.java">TeamCityPlugin.java</a>
   */
  public static final String FORMATTER_OPTIONS_6 = " --plugin teamcity";

  public static final Set<String> HOOK_AND_TYPE_ANNOTATION_NAMES = ContainerUtil.newHashSet("cucumber.annotation.Before",
                                                                                            "cucumber.annotation.After",
                                                                                            "cucumber.api.java.Before",
                                                                                            "cucumber.api.java.After",
                                                                                            "io.cucumber.java.Before",
                                                                                            "io.cucumber.java.After",
                                                                                            "io.cucumber.java.BeforeStep",
                                                                                            "io.cucumber.java.AfterStep",
                                                                                            "io.cucumber.java.ParameterType",
                                                                                            "io.cucumber.java.DataTableType",
                                                                                            "io.cucumber.java.DocStringType",
                                                                                            "io.cucumber.java.DefaultParameterTransformer",
                                                                                            "io.cucumber.java.DefaultDataTableEntryTransformer",
                                                                                            "io.cucumber.java.DefaultDataTableCellTransformer");

  public static final Set<String> CONFIGURATION_ANNOTATION_NAMES =
    ContainerUtil.newHashSet("io.cucumber.spring.CucumberContextConfiguration",
                             "io.cucumber.spring.ScenarioScope");

  @Override
  public @NotNull ConfigurationFactory getConfigurationFactory() {
    return CucumberJavaRunConfigurationType.getInstance().getConfigurationFactories()[0];
  }

  protected abstract @Nullable CucumberGlueProvider getGlueProvider(@NotNull PsiElement element);

  protected abstract String getConfigurationName(@NotNull ConfigurationContext context);

  protected String getNameFilter(@NotNull ConfigurationContext context) {
    return "";
  }

  protected abstract @Nullable VirtualFile getFileToRun(ConfigurationContext context);

  @Override
  @VisibleForTesting
  public boolean setupConfigurationFromContext(@NotNull CucumberJavaRunConfiguration configuration,
                                                  @NotNull ConfigurationContext context,
                                                  @NotNull Ref<PsiElement> sourceElement) {
    final VirtualFile virtualFile = getFileToRun(context);
    if (virtualFile == null) {
      LOG.debug("No file to run.");
      return false;
    }

    final Project project = configuration.getProject();
    final PsiElement element = context.getPsiLocation();

    if (element == null) {
      LOG.debug("PSI locations is null.");
      return false;
    }

    final Module module = ModuleUtilCore.findModuleForFile(virtualFile, project);
    if (module == null) {
      LOG.debug("Module is null.");
      return false;
    }

    if (virtualFile.isDirectory()) {
      if (!FileTypeIndex.containsFileOfType(GherkinFileType.INSTANCE, GlobalSearchScopesCore.directoryScope(project, virtualFile, true))) {
        LOG.debug("No '*.feature' files to run inside directory: ", virtualFile.getCanonicalPath());
        return false;
      }
    }

    final Location location = context.getLocation();
    if (location == null) {
      LOG.debug("Location is null.");
      return false;
    }

    configuration.setCucumberCoreVersion(getCucumberCoreVersion(module, module.getProject()));

    String cucumberCoreVersion = configuration.getCucumberCoreVersion();
    if (configuration.getMainClassName() == null) {
      configuration.setMainClassName(getCucumberMainClass(cucumberCoreVersion));
    }
    if (JavaPsiFacade.getInstance(project).findClass(configuration.getMainClassName(), GlobalSearchScope.allScope(project)) == null) {
      LOG.debug("Failed to find main cucumber class: ", configuration.getMainClassName());
      return false;
    }
    if (StringUtil.isEmpty(configuration.getGlue())) {
      if (isCucumber60orMore(module)) {
        // Cucumber can automatically find glue packages since v6. See IDEA-243074
        // Do not reset glue in case it's set manually.

        // Some users reported in IDEA-377245 that this breaks their workflows.
        // Until the root cause and fix are known, let's expose a registry key.
        if (Registry.is("cucumber.java.run.configuration.glue.use.idea")) {
          configuration.setGlueProvider(getGlueProvider(element));
        }
      }
      else {
        configuration.setGlueProvider(getGlueProvider(element));
      }
    }
    configuration.setNameFilter(getNameFilter(context));
    configuration.setFilePath(virtualFile.getPath());
    String programParametersFromDefaultConfiguration = StringUtil.defaultIfEmpty(configuration.getProgramParameters(), "");
    if (StringUtil.isEmpty(programParametersFromDefaultConfiguration)) {
      configuration.setProgramParameters(getSMFormatterOptions(cucumberCoreVersion));
    }

    if (configuration.getNameFilter() != null && !configuration.getNameFilter().isEmpty()) {
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
  public boolean isConfigurationFromContext(@NotNull CucumberJavaRunConfiguration runConfiguration, @NotNull ConfigurationContext context) {
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

  private static @NotNull String getSMFormatterOptions(@NotNull String cucumberCoreVersion) {
    if (VersionComparatorUtil.compare(cucumberCoreVersion, CUCUMBER_CORE_VERSION_6) >= 0) {
      return FORMATTER_OPTIONS_6;
    }
    if (VersionComparatorUtil.compare(cucumberCoreVersion, CUCUMBER_CORE_VERSION_5) >= 0) {
      return FORMATTER_OPTIONS_5;
    }
    if (VersionComparatorUtil.compare(cucumberCoreVersion, CUCUMBER_CORE_VERSION_4) >= 0) {
      return FORMATTER_OPTIONS_4;
    }
    if (VersionComparatorUtil.compare(cucumberCoreVersion, CUCUMBER_CORE_VERSION_3) >= 0) {
      return FORMATTER_OPTIONS_3;
    }
    if (VersionComparatorUtil.compare(cucumberCoreVersion, CUCUMBER_CORE_VERSION_2) >= 0) {
      return FORMATTER_OPTIONS_2;
    }
    if (VersionComparatorUtil.compare(cucumberCoreVersion, CUCUMBER_CORE_VERSION_1_2) >= 0) {
      return FORMATTER_OPTIONS_1_2;
    }
    return FORMATTER_OPTIONS_1_0;
  }
}
