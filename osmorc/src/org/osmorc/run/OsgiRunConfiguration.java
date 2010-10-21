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

import com.intellij.execution.ExecutionBundle;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.*;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ProgramRunner;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.impl.JavaSdkImpl;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.JDOMExternalizable;
import com.intellij.openapi.util.WriteExternalException;
import org.jdom.Attribute;
import org.jdom.Element;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osmorc.frameworkintegration.FrameworkInstanceDefinition;
import org.osmorc.i18n.OsmorcBundle;
import org.osmorc.run.ui.OsgiRunConfigurationEditor;
import org.osmorc.run.ui.SelectedBundle;
import org.osmorc.settings.ApplicationSettings;

import java.util.*;

/**
 * A run configuration for an OSGI server.
 *
 * @author <a href="mailto:janthomae@janthomae.de">Jan Thom&auml;</a>
 * @author Robert F. Beeger (robert@beeger.net)
 * @version $Id$
 */
public class OsgiRunConfiguration extends RunConfigurationBase implements ModuleRunConfiguration {
  @NonNls
  private static final String BUNDLE_ELEMENT = "bundle";
  @NonNls
  private static final String NAME_ATTRIBUTE = "name";
  @NonNls
  private static final String VM_PARAMETERS_ATTRIBUTE = "vmParameters";
  @NonNls
  private static final String PROGRAM_PARAMETERS_ATTRIBUTE = "programParameters";
  @NonNls
  private static final String WORKING_DIR_ATTRIBUTE = "workingDir";
  @NonNls
  private static final String FRAMEWORK_ELEMENT = "framework";
  @NonNls
  private static final String INSTANCE_ATTRIBUTE = "instance";
  @NonNls
  private static final String URL_ATTRIBUTE = "url";
  @NonNls
  private static final String ADDITIONAL_PROPERTIES_ELEMENT = "additinalProperties";
  @NonNls
  private static final String TYPE_ATTRIBUTE = "type";
  @NonNls
  private static final String START_AFTER_INSTALLATION_ATTRIBUTE = "startAfterInstallation";
  @NonNls
  private static final String START_LEVEL_ATTRIBUTE = "startLevel";
  @NonNls
  private static final String INCLUDE_ALL_BUNDLES_IN_CLASS_PATH_ATTRIBUTE = "includeAllBundlesInClassPath";
  @NonNls
  private static final String USE_ALTERNATIVE_JRE_ATTRIBUTE = "useAlternativeJre";
  @NonNls
  private static final String ALTERNATIVE_JRE_PATH = "alternativeJrePath";
  @NonNls
  private static final String FRAMEWORK_START_LEVEL = "frameworkStartLevel";
  @NonNls
  private static final String DEFAULT_START_LEVEL = "defaultStartLevel";
  @NonNls
  private static final String AUTO_START_LEVEL = "autoStartLevel";
  @NonNls
  public static final String GENERATE_WORKING_DIR_ATTRIBUTE = "generateWorkingDir";
  @Nullable
  private OsgiRunConfigurationChecker checker;
  private LegacyOsgiRunConfigurationLoader legacyOsgiRunConfigurationLoader;

  private List<SelectedBundle> bundlesToDeploy;
  private int frameworkStartLevel = 1;
  private int defaultStartLevel = 5;
  private boolean autoStartLevel;
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
    bundlesToDeploy = new ArrayList<SelectedBundle>();
    additionalProperties = new HashMap<String, String>();
  }

  @Override
  @Nullable
  public RunConfiguration clone() {
    OsgiRunConfiguration conf = (OsgiRunConfiguration)super.clone();
    if (conf == null) {
      return conf;
    }
    conf.bundlesToDeploy = new ArrayList<SelectedBundle>(bundlesToDeploy);
    conf.additionalProperties = new HashMap<String, String>(additionalProperties);
    return conf;
  }

  public void readExternal(final Element element) throws InvalidDataException {
    workingDir = element.getAttributeValue(WORKING_DIR_ATTRIBUTE);
    vmParameters = element.getAttributeValue(VM_PARAMETERS_ATTRIBUTE);
    programParameters = element.getAttributeValue(PROGRAM_PARAMETERS_ATTRIBUTE);
    includeAllBundlesInClassPath = Boolean.valueOf(element.getAttributeValue(INCLUDE_ALL_BUNDLES_IN_CLASS_PATH_ATTRIBUTE, "false"));
    useAlternativeJre = Boolean.valueOf(element.getAttributeValue(USE_ALTERNATIVE_JRE_ATTRIBUTE, "false"));
    alternativeJrePath = element.getAttributeValue(ALTERNATIVE_JRE_PATH, "");
    autoStartLevel = Boolean.valueOf(element.getAttributeValue(AUTO_START_LEVEL));
    generateWorkingDir = Boolean.valueOf(element.getAttributeValue(GENERATE_WORKING_DIR_ATTRIBUTE));

    String fwsl = element.getAttributeValue(FRAMEWORK_START_LEVEL);
    if (fwsl != null) {
      try {
        frameworkStartLevel = Integer.parseInt(fwsl);
      }
      catch (NumberFormatException e) {
        frameworkStartLevel = 1;
      }
    }

    String dfsl = element.getAttributeValue(DEFAULT_START_LEVEL);
    if ( dfsl != null ) {
      try {
        defaultStartLevel = Integer.parseInt(dfsl);
      }
      catch( NumberFormatException e) {
        defaultStartLevel = 5;
      }
    }

    // noinspection unchecked
    List<Element> children = element.getChildren(BUNDLE_ELEMENT);
    bundlesToDeploy.clear();
    for (Element child : children) {
      String name = child.getAttributeValue(NAME_ATTRIBUTE);
      String url = child.getAttributeValue(URL_ATTRIBUTE);
      String startLevel = child.getAttributeValue(START_LEVEL_ATTRIBUTE);
      String typeName = child.getAttributeValue(TYPE_ATTRIBUTE);

      if ("legacyLoader".equals(name)) {
        try {
          legacyOsgiRunConfigurationLoader = (LegacyOsgiRunConfigurationLoader)Class.forName(url).newInstance();
        }
        catch (InstantiationException e) {
          throw new InvalidDataException(e);
        }
        catch (IllegalAccessException e) {
          throw new InvalidDataException(e);
        }
        catch (ClassNotFoundException e) {
          throw new InvalidDataException(e);
        }
        break;
      }

      SelectedBundle.BundleType type;
      try {
        type = SelectedBundle.BundleType.valueOf(typeName);
      }
      catch (Exception e) {
        // legacy settings should have modules, only so this is a safe guess.
        type = SelectedBundle.BundleType.Module;
      }
      SelectedBundle selectedBundle = new SelectedBundle(name, url, type);
      if (startLevel != null) { // avoid crashing on legacy settings.
        try {
          selectedBundle.setStartLevel(Integer.parseInt(startLevel));
        }
        catch (NumberFormatException e) {
          // ok.
        }
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
        ApplicationSettings settings = ServiceManager.getService(ApplicationSettings.class);
        instanceToUse = settings.getFrameworkInstance(name);
      }
    }

    Element additionalProperties = element.getChild(ADDITIONAL_PROPERTIES_ELEMENT);
    if (additionalProperties != null) {
      //noinspection unchecked
      List<Attribute> attributes = additionalProperties.getAttributes();
      for (Attribute attribute : attributes) {
        this.additionalProperties.put(attribute.getName(), attribute.getValue());
      }
    }

    super.readExternal(element);
  }


  public void writeExternal(final Element element) throws WriteExternalException {
    // store the vm parameters
    element.setAttribute(VM_PARAMETERS_ATTRIBUTE, vmParameters == null ? "" : vmParameters);
    element.setAttribute(PROGRAM_PARAMETERS_ATTRIBUTE, programParameters == null ? "" : programParameters);
    element.setAttribute(INCLUDE_ALL_BUNDLES_IN_CLASS_PATH_ATTRIBUTE, Boolean.toString(includeAllBundlesInClassPath));
    element.setAttribute(WORKING_DIR_ATTRIBUTE, workingDir == null ? "" : workingDir);
    element.setAttribute(USE_ALTERNATIVE_JRE_ATTRIBUTE, String.valueOf(useAlternativeJre));
    element.setAttribute(ALTERNATIVE_JRE_PATH, alternativeJrePath != null ? alternativeJrePath : "");
    element.setAttribute(FRAMEWORK_START_LEVEL, String.valueOf(frameworkStartLevel));
    element.setAttribute(DEFAULT_START_LEVEL, String.valueOf(defaultStartLevel));
    element.setAttribute(AUTO_START_LEVEL, String.valueOf(autoStartLevel));
    element.setAttribute(GENERATE_WORKING_DIR_ATTRIBUTE, String.valueOf(generateWorkingDir));


    // all module's names
    for (SelectedBundle selectedBundle : bundlesToDeploy) {
      Element bundle = new Element(BUNDLE_ELEMENT);
      bundle.setAttribute(NAME_ATTRIBUTE, selectedBundle.getName());
      if (!selectedBundle.isModule()) {
        bundle.setAttribute(URL_ATTRIBUTE, selectedBundle.getBundleUrl());
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

  public SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
    return new OsgiRunConfigurationEditor(getProject());
  }

  @SuppressWarnings({"deprecation"})
  public JDOMExternalizable createRunnerSettings(final ConfigurationInfoProvider configurationInfoProvider) {
    return null;
  }

  @SuppressWarnings({"deprecation"})
  public SettingsEditor<JDOMExternalizable> getRunnerSettingsEditor(final ProgramRunner runner) {
    return null;
  }

  public RunProfileState getState(@NotNull final Executor executor, @NotNull final ExecutionEnvironment env) throws ExecutionException {
    // prepare the state

    return new OsgiRunState(executor, env, this, getProject(), ProjectRootManager.getInstance(getProject()).getProjectSdk());
  }

  public void checkConfiguration() throws RuntimeConfigurationException {
    if (legacyOsgiRunConfigurationLoader != null) {
      legacyOsgiRunConfigurationLoader.finishAfterModulesAreAvailable(this);
      legacyOsgiRunConfigurationLoader = null;
    }
    if (instanceToUse == null) {
      throw new RuntimeConfigurationError(OsmorcBundle.getTranslation("runconfiguration.no.instance.selected"));
    }
    if (isUseAlternativeJre()) {
      final String jrePath = this.getAlternativeJrePath();
      if (jrePath == null || jrePath.length() == 0 || !JavaSdkImpl.checkForJre(jrePath)) {
        throw new RuntimeConfigurationWarning(ExecutionBundle.message("jre.not.valid.error.message", jrePath));
      }
    }
    if (checker != null) {
      checker.checkConfiguration(this);
    }
  }

  @NotNull
  public Module[] getModules() {
    List<Module> modules = new ArrayList<Module>();
    for (SelectedBundle selectedBundle : getBundlesToDeploy()) {
      if (selectedBundle.isModule()) {
        modules.add(ModuleManager.getInstance(getProject()).findModuleByName(selectedBundle.getName()));
      }
    }
    return modules.toArray(new Module[modules.size()]);
  }

  @NotNull
  public Map<String, String> getAdditionalProperties() {
    return Collections.unmodifiableMap(additionalProperties);
  }

  public String getAlternativeJrePath() {
    return alternativeJrePath;
  }

  @NotNull
  public List<SelectedBundle> getBundlesToDeploy() {
    return bundlesToDeploy;
  }

  public int getFrameworkStartLevel() {
    return frameworkStartLevel;
  }

  @Nullable
  public FrameworkInstanceDefinition getInstanceToUse() {
    return instanceToUse;
  }

  public String getProgramParameters() {
    return programParameters != null ? programParameters : "";
  }

  @NotNull
  public String getVmParameters() {
    return vmParameters != null ? vmParameters : "";
  }

  public String getWorkingDir() {
    return workingDir != null ? workingDir : "";
  }

  public boolean isAutoStartLevel() {
    return autoStartLevel;
  }

  public boolean isIncludeAllBundlesInClassPath() {
    return includeAllBundlesInClassPath;
  }

  public boolean isUseAlternativeJre() {
    return useAlternativeJre;
  }

  public void putAdditionalProperties(@NotNull final Map<String, String> props) {
    additionalProperties.putAll(props);
  }

  public void setAdditionalChecker(@Nullable OsgiRunConfigurationChecker checker) {
    this.checker = checker;
  }

  public void setAlternativeJrePath(String alternativeJrePath) {
    this.alternativeJrePath = alternativeJrePath;
  }

  public void setAutoStartLevel(boolean autoStartLevel) {
    this.autoStartLevel = autoStartLevel;
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

  public void setInstanceToUse(@NotNull final FrameworkInstanceDefinition instanceToUse) {
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
