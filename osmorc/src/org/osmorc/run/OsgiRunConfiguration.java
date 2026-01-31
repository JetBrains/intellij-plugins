/*
 * Copyright (c) 2007-2009, Osmorc Development Team
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright notice, this list
 *       of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright notice, this
 *       list of conditions and the following disclaimer in the documentation and/or other
 *       materials provided with the distribution.
 *     * Neither the name of 'Osmorc Development Team' nor the names of its contributors may be
 *       used to endorse or promote products derived from this software without specific
 *       prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT
 * OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.osmorc.run;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ModuleRunConfiguration;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.RunConfigurationBase;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.configurations.RuntimeConfigurationError;
import com.intellij.execution.configurations.RuntimeConfigurationException;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.util.JavaParametersUtil;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.JDOMUtil;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VfsUtilCore;
import org.jdom.Attribute;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osmorc.frameworkintegration.FrameworkInstanceDefinition;
import org.osmorc.frameworkintegration.impl.GenericRunProperties;
import org.osmorc.i18n.OsmorcBundle;
import org.osmorc.run.ui.OsgiRunConfigurationEditor;
import org.osmorc.run.ui.SelectedBundle;
import org.osmorc.settings.ApplicationSettings;
import org.osmorc.settings.ProjectSettings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A run configuration for an OSGI server.
 *
 * @author <a href="mailto:janthomae@janthomae.de">Jan Thomä</a>
 * @author <a href="mailto:robert@beeger.net">Robert F. Beeger</a>
 */
public class OsgiRunConfiguration extends RunConfigurationBase<Element> implements ModuleRunConfiguration {
  private static final Logger LOG = Logger.getInstance(OsgiRunConfiguration.class);

  private static final String BUNDLE_ELEMENT = "bundle";
  private static final String NAME_ATTRIBUTE = "name";
  private static final String VM_PARAMETERS_ATTRIBUTE = "vmParameters";
  private static final String PROGRAM_PARAMETERS_ATTRIBUTE = "programParameters";
  private static final String WORKING_DIR_ATTRIBUTE = "workingDir";
  private static final String FRAMEWORK_ELEMENT = "framework";
  private static final String INSTANCE_ATTRIBUTE = "instance";
  private static final String URL_ATTRIBUTE = "url";
  private static final String ADDITIONAL_PROPERTIES_ELEMENT = "additionalProperties";
  private static final String TYPE_ATTRIBUTE = "type";
  private static final String START_AFTER_INSTALLATION_ATTRIBUTE = "startAfterInstallation";
  private static final String START_LEVEL_ATTRIBUTE = "startLevel";
  private static final String INCLUDE_ALL_BUNDLES_IN_CLASS_PATH_ATTRIBUTE = "includeAllBundlesInClassPath";
  private static final String USE_ALTERNATIVE_JRE_ATTRIBUTE = "useAlternativeJre";
  private static final String ALTERNATIVE_JRE_PATH = "alternativeJrePath";
  private static final String FRAMEWORK_START_LEVEL = "frameworkStartLevel";
  private static final String DEFAULT_START_LEVEL = "defaultStartLevel";
  private static final String GENERATE_WORKING_DIR_ATTRIBUTE = "generateWorkingDir";

  private OsgiRunConfigurationChecker checker;
  private List<SelectedBundle> bundlesToDeploy;
  private int frameworkStartLevel = 1;
  private int defaultStartLevel = 5;
  private String programParameters;
  private String vmParameters;
  private String alternativeJrePath;
  private boolean useAlternativeJre;
  private FrameworkInstanceDefinition instanceToUse;
  private Map<String, String> additionalProperties;
  private boolean includeAllBundlesInClassPath;
  private String workingDir;
  private boolean generateWorkingDir;

  public OsgiRunConfiguration(final Project project, final ConfigurationFactory configurationFactory, final String name) {
    super(project, configurationFactory, name);
    bundlesToDeploy = new ArrayList<>();
    additionalProperties = new HashMap<>();
    GenericRunProperties.setStartConsole(additionalProperties, true);
  }

  @Override
  public @Nullable RunConfiguration clone() {
    OsgiRunConfiguration conf = (OsgiRunConfiguration)super.clone();
    if (conf == null) return null;

    conf.bundlesToDeploy = new ArrayList<>(bundlesToDeploy);
    conf.additionalProperties = new HashMap<>(additionalProperties);
    return conf;
  }

  @Override
  public void readExternal(final @NotNull Element element) throws InvalidDataException {
    workingDir = element.getAttributeValue(WORKING_DIR_ATTRIBUTE);
    vmParameters = element.getAttributeValue(VM_PARAMETERS_ATTRIBUTE);
    programParameters = element.getAttributeValue(PROGRAM_PARAMETERS_ATTRIBUTE);
    includeAllBundlesInClassPath = Boolean.parseBoolean(element.getAttributeValue(INCLUDE_ALL_BUNDLES_IN_CLASS_PATH_ATTRIBUTE, "false"));
    useAlternativeJre = Boolean.parseBoolean(element.getAttributeValue(USE_ALTERNATIVE_JRE_ATTRIBUTE, "false"));
    alternativeJrePath = element.getAttributeValue(ALTERNATIVE_JRE_PATH, "");
    generateWorkingDir = Boolean.parseBoolean(element.getAttributeValue(GENERATE_WORKING_DIR_ATTRIBUTE));

    try {
      frameworkStartLevel = Integer.parseInt(element.getAttributeValue(FRAMEWORK_START_LEVEL, "1"));
    }
    catch (NumberFormatException e) {
      frameworkStartLevel = 1;
    }

    try {
      defaultStartLevel = Integer.parseInt(element.getAttributeValue(DEFAULT_START_LEVEL, "5"));
    }
    catch (NumberFormatException e) {
      defaultStartLevel = 5;
    }

    List<Element> children = element.getChildren(BUNDLE_ELEMENT);
    bundlesToDeploy.clear();
    for (Element child : children) {
      String name = child.getAttributeValue(NAME_ATTRIBUTE);
      String url = child.getAttributeValue(URL_ATTRIBUTE);
      String startLevel = child.getAttributeValue(START_LEVEL_ATTRIBUTE);
      String typeName = child.getAttributeValue(TYPE_ATTRIBUTE);

      if (StringUtil.isEmptyOrSpaces(name)) {
        LOG.error("missing name attribute: " + JDOMUtil.writeElement(element));
        continue;
      }

      SelectedBundle.BundleType type;
      try {
        type = SelectedBundle.BundleType.valueOf(typeName);
      }
      catch (IllegalArgumentException e) {
        LOG.error("unexpected bundle type '" + typeName + "'");
        type = SelectedBundle.BundleType.Module;
      }

      String path = url != null ? VfsUtilCore.urlToPath(url) : null;
      SelectedBundle selectedBundle = new SelectedBundle(type, name, path);

      if (startLevel != null) {
        try {
          selectedBundle.setStartLevel(Integer.parseInt(startLevel));
        }
        catch (NumberFormatException ignored) { }
      }

      String startAfterInstallationString = child.getAttributeValue(START_AFTER_INSTALLATION_ATTRIBUTE);
      if (startAfterInstallationString != null) {
        selectedBundle.setStartAfterInstallation(Boolean.parseBoolean(startAfterInstallationString));
      }

      bundlesToDeploy.add(selectedBundle);
    }

    // try to load the framework instance
    Element framework = element.getChild(FRAMEWORK_ELEMENT);
    if (framework != null) {
      String name = framework.getAttributeValue(INSTANCE_ATTRIBUTE);
      if (name != null) {
        ApplicationSettings settings = ApplicationSettings.getInstance();
        instanceToUse = settings.getFrameworkInstance(name);
      }
    }

    Element additionalProperties = element.getChild(ADDITIONAL_PROPERTIES_ELEMENT);
    if (additionalProperties == null) {
      //noinspection SpellCheckingInspection
      additionalProperties = element.getChild("additinalProperties");
    }
    if (additionalProperties != null) {
      List<Attribute> attributes = additionalProperties.getAttributes();
      for (Attribute attribute : attributes) {
        this.additionalProperties.put(attribute.getName(), attribute.getValue());
      }
    }

    super.readExternal(element);
  }

  @Override
  public void writeExternal(final @NotNull Element element) throws WriteExternalException {
    // store the vm parameters
    element.setAttribute(VM_PARAMETERS_ATTRIBUTE, vmParameters == null ? "" : vmParameters);
    element.setAttribute(PROGRAM_PARAMETERS_ATTRIBUTE, programParameters == null ? "" : programParameters);
    element.setAttribute(INCLUDE_ALL_BUNDLES_IN_CLASS_PATH_ATTRIBUTE, Boolean.toString(includeAllBundlesInClassPath));
    element.setAttribute(WORKING_DIR_ATTRIBUTE, workingDir == null ? "" : workingDir);
    element.setAttribute(USE_ALTERNATIVE_JRE_ATTRIBUTE, String.valueOf(useAlternativeJre));
    element.setAttribute(ALTERNATIVE_JRE_PATH, alternativeJrePath != null ? alternativeJrePath : "");
    element.setAttribute(FRAMEWORK_START_LEVEL, String.valueOf(frameworkStartLevel));
    element.setAttribute(DEFAULT_START_LEVEL, String.valueOf(defaultStartLevel));
    element.setAttribute(GENERATE_WORKING_DIR_ATTRIBUTE, String.valueOf(generateWorkingDir));

    // all module's names
    for (SelectedBundle selectedBundle : bundlesToDeploy) {
      Element bundle = new Element(BUNDLE_ELEMENT);
      bundle.setAttribute(NAME_ATTRIBUTE, selectedBundle.getName());
      if (!selectedBundle.isModule()) {
        String path = selectedBundle.getBundlePath();
        if (path != null) bundle.setAttribute(URL_ATTRIBUTE, VfsUtilCore.pathToUrl(path));
      }
      bundle.setAttribute(START_LEVEL_ATTRIBUTE, String.valueOf(selectedBundle.getStartLevel()));
      bundle.setAttribute(TYPE_ATTRIBUTE, selectedBundle.getBundleType().name());
      bundle.setAttribute(START_AFTER_INSTALLATION_ATTRIBUTE, Boolean.toString(selectedBundle.isStartAfterInstallation()));
      element.addContent(bundle);
    }

    // and the instance to use
    Element framework = new Element(FRAMEWORK_ELEMENT);
    framework.setAttribute(INSTANCE_ATTRIBUTE, instanceToUse != null ? instanceToUse.getName() : "");
    element.addContent(framework);

    Element additionalProperties = new Element(ADDITIONAL_PROPERTIES_ELEMENT);
    for (String additionalPropertyName : this.additionalProperties.keySet()) {
      additionalProperties.setAttribute(additionalPropertyName, this.additionalProperties.get(additionalPropertyName));
    }
    element.addContent(additionalProperties);

    super.writeExternal(element);
  }

  @Override
  public @NotNull SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
    return new OsgiRunConfigurationEditor(getProject());
  }

  @Override
  public RunProfileState getState(@NotNull Executor executor, @NotNull ExecutionEnvironment env) throws ExecutionException {
    return new OsgiRunState(env, this);
  }

  @Override
  public void checkConfiguration() throws RuntimeConfigurationException {
    if (getInstanceToUse() == null) {
      throw new RuntimeConfigurationError(OsmorcBundle.message("run.configuration.no.instance"));
    }
    if (isUseAlternativeJre()) {
      JavaParametersUtil.checkAlternativeJRE(getAlternativeJrePath());
    }
    if (checker != null) {
      checker.checkConfiguration(this);
    }
  }

  @Override
  public Module @NotNull [] getModules() {
    List<Module> modules = new ArrayList<>();

    ModuleManager moduleManager = ModuleManager.getInstance(getProject());
    for (SelectedBundle selectedBundle : getBundlesToDeploy()) {
      if (selectedBundle.isModule()) {
        Module module = moduleManager.findModuleByName(selectedBundle.getName());
        if (module != null) {
          modules.add(module);
        }
        else {
          LOG.debug("module not found: " + selectedBundle.getName());
        }
      }
    }

    return modules.toArray(Module.EMPTY_ARRAY);
  }

  public @NotNull Map<String, String> getAdditionalProperties() {
    return Collections.unmodifiableMap(additionalProperties);
  }

  public String getAlternativeJrePath() {
    return alternativeJrePath;
  }

  public @NotNull List<SelectedBundle> getBundlesToDeploy() {
    return bundlesToDeploy;
  }

  public int getFrameworkStartLevel() {
    return frameworkStartLevel;
  }

  public @Nullable FrameworkInstanceDefinition getInstanceToUse() {
    if (instanceToUse != null) return instanceToUse;

    String projectInstanceName = ProjectSettings.getInstance(getProject()).getFrameworkInstanceName();
    FrameworkInstanceDefinition projectInstance = ApplicationSettings.getInstance().getFrameworkInstance(projectInstanceName);
    if (projectInstance != null) return projectInstance;

    return null;
  }

  public String getProgramParameters() {
    return programParameters != null ? programParameters : "";
  }

  public @NotNull String getVmParameters() {
    return vmParameters != null ? vmParameters : "";
  }

  public String getWorkingDir() {
    return workingDir != null ? workingDir : "";
  }

  public boolean isAutoStartLevel() {
    return frameworkStartLevel == 0;
  }

  public boolean isIncludeAllBundlesInClassPath() {
    return includeAllBundlesInClassPath;
  }

  public boolean isUseAlternativeJre() {
    return useAlternativeJre;
  }

  public void putAdditionalProperties(@NotNull Map<String, String> props) {
    additionalProperties.putAll(props);
  }

  public void setAdditionalChecker(@Nullable OsgiRunConfigurationChecker checker) {
    this.checker = checker;
  }

  public void setAlternativeJrePath(String alternativeJrePath) {
    this.alternativeJrePath = alternativeJrePath;
  }

  public void setBundlesToDeploy(final List<SelectedBundle> bundlesToDeploy) {
    this.bundlesToDeploy = bundlesToDeploy;
  }

  public void setFrameworkStartLevel(int frameworkStartLevel) {
    this.frameworkStartLevel = frameworkStartLevel;
  }

  public void setIncludeAllBundlesInClassPath(final boolean includeAllBundlesInClassPath) {
    this.includeAllBundlesInClassPath = includeAllBundlesInClassPath;
  }

  public void setInstanceToUse(@Nullable FrameworkInstanceDefinition instanceToUse) {
    this.instanceToUse = instanceToUse;
  }

  public void setProgramParameters(final String programParameters) {
    this.programParameters = programParameters;
  }

  public void setUseAlternativeJre(boolean useAlternativeJre) {
    this.useAlternativeJre = useAlternativeJre;
  }

  public void setVmParameters(final String vmParameters) {
    this.vmParameters = vmParameters;
  }

  public void setWorkingDir(final String workingDir) {
    this.workingDir = workingDir;
  }

  public int getDefaultStartLevel() {
    return defaultStartLevel;
  }

  public void setDefaultStartLevel(int defaultStartLevel) {
    this.defaultStartLevel = defaultStartLevel;
  }

  /**
   * Should the working directory be regenerated on run or not?
   */
  public boolean isGenerateWorkingDir() {
    return generateWorkingDir;
  }

  public void setGenerateWorkingDir(boolean generateWorkingDir) {
    this.generateWorkingDir = generateWorkingDir;
  }
}