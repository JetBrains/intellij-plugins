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

import com.intellij.execution.CantRunException;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.JavaCommandLineState;
import com.intellij.execution.configurations.JavaParameters;
import com.intellij.execution.configurations.ParametersList;
import com.intellij.execution.filters.TextConsoleBuilderImpl;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.compiler.DummyCompileContext;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.JavaSdk;
import com.intellij.openapi.projectRoots.JdkUtil;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.PathsList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osmorc.facet.OsmorcFacet;
import org.osmorc.frameworkintegration.*;
import org.osmorc.make.BundleCompiler;
import org.osmorc.run.ui.SelectedBundle;

import javax.swing.*;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 * RunState for launching the OSGI framework.
 *
 * @author <a href="mailto:janthomae@janthomae.de">Jan Thom&auml;</a>
 * @author Robert F. Beeger (robert@beeger.net)
 * @version $Id$
 */
public class OsgiRunState extends JavaCommandLineState  {
  private final OsgiRunConfiguration runConfiguration;
  private final Project project;
  private final Sdk jdkForRun;
  private SelectedBundle[] _selectedBundles;
  private final FrameworkRunner runner;
  private static final String FILE_URL_PREFIX = "file:///";

  public OsgiRunState(@NotNull Executor executor,
                      @NotNull ExecutionEnvironment env,
                      OsgiRunConfiguration configuration,
                      Project project,
                      Sdk projectJdk) {
    super(env);
    this.runConfiguration = configuration;
    this.project = project;

    if (configuration.isUseAlternativeJre()) {
      String path = configuration.getAlternativeJrePath();
      if (path == null || "".equals(path) || !JdkUtil.checkForJre(path)) {
        this.jdkForRun = null;
      }
      else {
        this.jdkForRun = JavaSdk.getInstance().createJdk("", configuration.getAlternativeJrePath());
      }
    }
    else {
      this.jdkForRun = projectJdk;
    }
    setConsoleBuilder(new TextConsoleBuilderImpl(project));
    FrameworkInstanceDefinition definition = runConfiguration.getInstanceToUse();
    FrameworkIntegratorRegistry registry = ServiceManager.getService(project, FrameworkIntegratorRegistry.class);
    FrameworkIntegrator integrator = registry.findIntegratorByInstanceDefinition(definition);
    runner = integrator.createFrameworkRunner();
    runner.init(project, runConfiguration, getRunnerSettings());
  }


  public boolean requiresRemoteDebugger() {
    return runner instanceof ExternalVMFrameworkRunner;
  }

  protected JavaParameters createJavaParameters() throws ExecutionException {
    if (jdkForRun == null) {
      throw CantRunException.noJdkConfigured();
    }
    final JavaParameters params = new JavaParameters();

    params.setWorkingDirectory(runner.getWorkingDir());

    // only add JDK classes to the classpath
    // the rest is is to be provided by bundles
    params.configureByProject(project, JavaParameters.JDK_ONLY, jdkForRun);
    PathsList classpath = params.getClassPath();
    for (VirtualFile libraryFile : runner.getFrameworkStarterLibraries()) {
      classpath.add(libraryFile);
    }

    if (runConfiguration.isIncludeAllBundlesInClassPath()) {
      SelectedBundle[] bundles = getSelectedBundles();
      for (SelectedBundle bundle : bundles) {
        String bundlePath = bundle.getBundleUrl();
        bundlePath = bundlePath.substring(FILE_URL_PREFIX.length());
        if (bundlePath.indexOf(':') < 0 && bundlePath.charAt(0) != '/') {
          bundlePath = "/" + bundlePath;
        }
        bundlePath = bundlePath.replace('/', File.separatorChar);

        classpath.add(bundlePath);
      }
    }

    // setup  the main class
    params.setMainClass(runner.getMainClass());

    // get the bundles to be run.
    SelectedBundle[] bundles = getSelectedBundles();
    if (bundles == null) {
      throw new CantRunException("One or more modules seem to be missing their OSGi facets. Please re-add the OSGi facets and try again.");
    }

    // setup the commandline parameters
    final ParametersList programParameters = params.getProgramParametersList();
    runner.fillCommandLineParameters(programParameters, bundles);

    // and the vm parameters
    final ParametersList vmParameters = params.getVMParametersList();
    runner.fillVmParameters(vmParameters, bundles);

    return params;
  }

  /**
   * Here we got the magic. All libs are turned into bundles sorted and returned.
   *
   * @return the sorted list of all bundles to start.
   */
  @Nullable
  private SelectedBundle[] getSelectedBundles() {

    if (_selectedBundles == null) {
      ProgressManager.getInstance().run(new Task.Modal(project, "Preparing bundles...", false) {
        
        public void run(@NotNull ProgressIndicator progressIndicator) {
          progressIndicator.setIndeterminate(false);
          HashSet<SelectedBundle> selectedBundles = new HashSet<SelectedBundle>();
          // the bundles are module names, by now we try to find jar files in the output directory which we can then install
          ModuleManager moduleManager = ModuleManager.getInstance(project);
          int bundleCount = runConfiguration.getBundlesToDeploy().size();
          for (int i = 0; i < bundleCount; i++) {
            final SelectedBundle selectedBundle = runConfiguration.getBundlesToDeploy().get(i);
            progressIndicator.setFraction(i / bundleCount);
            if (selectedBundle.isModule()) {
              // use the output jar name if it is a module
              try {
                final Module module = moduleManager.findModuleByName(selectedBundle.getName());
                if (!OsmorcFacet.hasOsmorcFacet(module)) {
                  // actually this should not happen, but it seemed to happen once, so we check this here.
                  try {
                    SwingUtilities.invokeAndWait(new Runnable() {
                      public void run() {
                        Messages.showErrorDialog("Module '" +
                                                 selectedBundle.getName() +
                                                 "' has no OSGi facet, but should have. Please re-add the OSGi facet to this module.",
                                                 "Error");
                      }
                    });
                  }
                  catch (Exception e) {
                    // it's ok.
                  }
                  _selectedBundles = null;
                  return;
                }
                selectedBundle.setBundleUrl(new URL("file", "/", BundleCompiler.getJarFileName(module)).toString());
                // add all the dependencies of the bundle
                String[] depUrls = BundleCompiler.bundlifyLibraries(module, progressIndicator, DummyCompileContext.getInstance());
                for (String depUrl : depUrls) {
                  SelectedBundle dependency = new SelectedBundle("Dependency", depUrl, SelectedBundle.BundleType.PlainLibrary);
                  selectedBundles.add(dependency);
                }
                selectedBundles.add(selectedBundle);
              }
              catch (MalformedURLException e) {
                throw new IllegalStateException(e); // should not happen...
              }
            }
            else {
              if (selectedBundles.contains(selectedBundle)) {
                // if the user selected a dependency as runnable library, we need to replace the dependency with
                // the runnable library part
                selectedBundles.remove(selectedBundle);
              }
              selectedBundles.add(selectedBundle);
            }
          }
          HashMap<String, SelectedBundle> finalList = new HashMap<String, SelectedBundle>();

          // filter out bundles which have the same symbolic name
          for (SelectedBundle selectedBundle : selectedBundles) {
            String name = CachingBundleInfoProvider.getBundleSymbolicName(selectedBundle.getBundleUrl());
            String version = CachingBundleInfoProvider.getBundleVersions(selectedBundle.getBundleUrl());
            String key = name + version;
            if (!finalList.containsKey(key)) {
              finalList.put(key, selectedBundle);
            }
          }

          Collection<SelectedBundle> selectedBundleCollection = finalList.values();
          _selectedBundles = selectedBundleCollection.toArray(new SelectedBundle[selectedBundleCollection.size()]);
          Arrays.sort(_selectedBundles, new StartLevelComparator());
        }
      });
    }
    return _selectedBundles;
  }

  protected OSProcessHandler startProcess() throws ExecutionException {
    // run any final configuration steps
    SelectedBundle[] bundles = getSelectedBundles();
    runner.runCustomInstallationSteps(bundles);

    OSProcessHandler handler = super.startProcess();
    handler.addProcessListener(new ProcessAdapter() {
      public void processTerminated(ProcessEvent event) {
        // make sure the runner is disposed when the process exits (so we get rid of the temp folders)
        Disposer.dispose(runner);
      }
    });
    return handler;
  }

  /**
   * Comparator for sorting bundles by their start level.
   *
   * @author <a href="mailto:janthomae@janthomae.de">Jan Thom&auml;</a>
   * @version $Id:$
   */
  public static class StartLevelComparator implements Comparator<SelectedBundle> {
    public int compare(SelectedBundle selectedBundle, SelectedBundle selectedBundle2) {
      return selectedBundle.getStartLevel() - selectedBundle2.getStartLevel();
    }
  }
}
