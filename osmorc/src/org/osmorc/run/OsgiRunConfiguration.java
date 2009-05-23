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
import com.intellij.execution.configurations.*;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ProgramRunner;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
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
    @Nullable
    private OsgiRunConfigurationChecker checker;
    private LegacyOsgiRunConfigurationLoader legacyOsgiRunConfigurationLoader;
    private boolean needsFinishRunForLegacyOsgiRunConfigurationLoader = true;

    protected OsgiRunConfiguration(final Project project, final ConfigurationFactory configurationFactory, final String name) {
        super(project, configurationFactory, name);
        bundlesToDeploy = new ArrayList<SelectedBundle>();
        additionalProperties = new HashMap<String, String>();
    }

    public SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
        return new OsgiRunConfigurationEditor(getProject());
    }


    public RunProfileState getState(@NotNull final Executor executor, @NotNull final ExecutionEnvironment env) throws
            ExecutionException {
        // prepare the state

        return new OsgiRunState(executor, env, this,
                getProject(), ProjectRootManager.getInstance(getProject()).getProjectJdk());
    }

    public void checkConfiguration() throws RuntimeConfigurationException {
        if (needsFinishRunForLegacyOsgiRunConfigurationLoader) {
            needsFinishRunForLegacyOsgiRunConfigurationLoader = false;
            if (legacyOsgiRunConfigurationLoader != null) {
                legacyOsgiRunConfigurationLoader.finishAfterModulesAreAvailable(this);
            }
        }
        if (instanceToUse == null) {
            throw new RuntimeConfigurationError(OsmorcBundle.getTranslation("runconfiguration.no.instance.selected"));
        }
        if (checker != null) {
            checker.checkConfiguration(this);
        }
    }

    public void setAdditionalChecker(@Nullable OsgiRunConfigurationChecker checker) {
        this.checker = checker;
    }

    public void setLegacyOsgiRunConfigurationLoader(LegacyOsgiRunConfigurationLoader legacyOsgiRunConfigurationLoader) {
        this.legacyOsgiRunConfigurationLoader = legacyOsgiRunConfigurationLoader;
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
    public List<SelectedBundle> getBundlesToDeploy() {
        return bundlesToDeploy;
    }

    public void setBundlesToDeploy(final List<SelectedBundle> bundlesToDeploy) {
        this.bundlesToDeploy = bundlesToDeploy;
    }

    @NotNull
    public String getVmParameters() {
        return vmParameters != null ? vmParameters : "";
    }


    public void setVmParameters(final String vmParameters) {
        this.vmParameters = vmParameters;
    }

    @Nullable
    public FrameworkInstanceDefinition getInstanceToUse() {
        return instanceToUse;
    }

    public void setInstanceToUse(@NotNull final FrameworkInstanceDefinition instanceToUse) {
        this.instanceToUse = instanceToUse;
    }

    public void putAdditionalProperties(@NotNull final Map<String, String> props) {
        additionalProperties.putAll(props);
    }

    @NotNull
    public Map<String, String> getAdditionalProperties() {
        return Collections.unmodifiableMap(additionalProperties);
    }

    public boolean isIncludeAllBundlesInClassPath() {
        return includeAllBundlesInClassPath;
    }

    public void setIncludeAllBundlesInClassPath(final boolean includeAllBundlesInClassPath) {
        this.includeAllBundlesInClassPath = includeAllBundlesInClassPath;
    }


    public void setWorkingDir(final String workingDir) {
        this.workingDir = workingDir;
    }

    public String getWorkingDir() {
        return workingDir != null ? workingDir : "";
    }

    public void setFrameworkDir(final String frameworkDir) {
        this.frameworkDir = frameworkDir;
    }

    public String getFrameworkDir() {
        return frameworkDir != null ? frameworkDir : "";
    }


    public boolean isRuntimeDirsOsmorcControlled() {
        return getFrameworkDir().length() == 0 && getWorkingDir().length() == 0;
    }

    public String getProgramParameters() {
        return programParameters != null ? programParameters : "";
    }

    public void setProgramParameters(final String programParameters) {
        this.programParameters = programParameters;
    }


    public void readExternal(final Element element) throws InvalidDataException {
        if (legacyOsgiRunConfigurationLoader == null || !legacyOsgiRunConfigurationLoader.readExternal(element, this)) {
            workingDir = element.getAttributeValue(WORKING_DIR_ATTRIBUTE);
            frameworkDir = element.getAttributeValue(FRAMEWORK_DIR_ATTRIBUTE);
            vmParameters = element.getAttributeValue(VM_PARAMETERS_ATTRIBUTE);
            programParameters = element.getAttributeValue(PROGRAM_PARAMETERS_ATTRIBUTE);
            includeAllBundlesInClassPath = Boolean.valueOf(element.getAttributeValue(
                    INCLUDE_ALL_BUNDLES_IN_CLASS_PATH_ATTRIBUTE, "false"));

            // noinspection unchecked
            List<Element> children = element.getChildren(BUNDLE_ELEMENT);
            bundlesToDeploy.clear();
            for (Element child : children) {
                String name = child.getAttributeValue(NAME_ATTRIBUTE);
                String url = child.getAttributeValue(URL_ATTRIBUTE);
                String startLevel = child.getAttributeValue(START_LEVEL_ATTRIBUTE);
                String typeName = child.getAttributeValue(TYPE_ATTRIBUTE);
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
        }

        super.readExternal(element);
    }

    public void writeExternal(final Element element) throws WriteExternalException {
        // store the vm parameters
        element.setAttribute(VM_PARAMETERS_ATTRIBUTE, vmParameters == null ? "" : vmParameters);
        element.setAttribute(PROGRAM_PARAMETERS_ATTRIBUTE, programParameters == null ? "" : programParameters);
        element.setAttribute(INCLUDE_ALL_BUNDLES_IN_CLASS_PATH_ATTRIBUTE, Boolean.toString(includeAllBundlesInClassPath));
        element.setAttribute(WORKING_DIR_ATTRIBUTE, workingDir == null ? "" : workingDir);
        element.setAttribute(FRAMEWORK_DIR_ATTRIBUTE, frameworkDir == null ? "" : frameworkDir);

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

    @SuppressWarnings({"deprecation"})
    public JDOMExternalizable createRunnerSettings(final ConfigurationInfoProvider configurationInfoProvider) {
        return null;
    }

    @SuppressWarnings({"deprecation"})
    public SettingsEditor<JDOMExternalizable> getRunnerSettingsEditor(final ProgramRunner runner) {
        return null;
    }

    @Override
    public RunConfiguration clone() {
        OsgiRunConfiguration conf = (OsgiRunConfiguration) super.clone();
        if (conf == null) {
            return conf;
        }
        conf.bundlesToDeploy = new ArrayList<SelectedBundle>(bundlesToDeploy);
        conf.additionalProperties = new HashMap<String, String>(additionalProperties);
        return conf;
    }

    private List<SelectedBundle> bundlesToDeploy;

    private String programParameters;
    private String vmParameters;
    private FrameworkInstanceDefinition instanceToUse;
    private Map<String, String> additionalProperties;
    private boolean includeAllBundlesInClassPath;
    private String workingDir;
    private String frameworkDir;

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
    private static final String FRAMEWORK_DIR_ATTRIBUTE = "FrameworkDir";
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
}
