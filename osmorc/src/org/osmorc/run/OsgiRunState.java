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
import com.intellij.execution.configurations.JavaCommandLineState;
import com.intellij.execution.configurations.JavaParameters;
import com.intellij.execution.configurations.ParametersList;
import com.intellij.execution.filters.TextConsoleBuilderImpl;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.PathsList;
import org.jetbrains.annotations.NotNull;
import org.osmorc.frameworkintegration.*;
import org.osmorc.frameworkintegration.util.PropertiesWrapper;
import org.osmorc.make.BundleCompiler;
import org.osmorc.run.ui.SelectedBundle;

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
public class OsgiRunState extends JavaCommandLineState
{
  protected AnAction[] createActions(ConsoleView consoleView, ProcessHandler processHandler)
  {
    return super.createActions(consoleView, processHandler);
  }

  private final OsgiRunConfiguration runConfiguration;
  private final Project project;
  private final Sdk projectJdk;
  private SelectedBundle[] _selectedBundles;


  public OsgiRunState(@NotNull Executor executor, @NotNull ExecutionEnvironment env,
                      OsgiRunConfiguration configuration, Project project, Sdk projectJdk)
  {
    super(env);
    this.runConfiguration = configuration;
    this.project = project;
    this.projectJdk = projectJdk;
    setConsoleBuilder(new TextConsoleBuilderImpl(project));
  }

  protected JavaParameters createJavaParameters() throws ExecutionException
  {
    final JavaParameters params = new JavaParameters();

    // make sure our temp folder exists
    String path = getRunPath();
    params.setWorkingDirectory(path);

    // only add JDK classes to the classpath
    // the rest is is to be provided by bundles
    params.configureByProject(project, JavaParameters.JDK_ONLY, projectJdk);

    // find the FrameworkInstanceManager for the selected definition
    FrameworkInstanceDefinition definition = runConfiguration.getInstanceToUse();
    FrameworkIntegratorRegistry registry = ServiceManager.getService(project, FrameworkIntegratorRegistry.class);
    FrameworkIntegrator integrator = registry.findIntegratorByInstanceDefinition(definition);
    FrameworkInstanceManager frameworkInstanceManager = integrator.getFrameworkInstanceManager();
    _runner = integrator.createFrameworkRunner();

    // add the libraries to the classpath ( probably not a good idea, but for starters...)

    List<Library> libs = frameworkInstanceManager.getLibraries(definition);
    PathsList classpath = params.getClassPath();

    for (Library lib : libs)
    {
      for (VirtualFile virtualFile : lib.getFiles(OrderRootType.CLASSES_AND_OUTPUT))
      {
        if (_runner.getFrameworkStarterClasspathPattern().matcher(virtualFile.getName()).matches())
        {
          classpath.add(virtualFile);
        }
      }
    }

    if (runConfiguration.isIncludeAllBundlesInClassPath())
    {
      SelectedBundle[] bundles = getSelectedBundles();
      for (SelectedBundle bundle : bundles)
      {
        String bundlePath = bundle.getBundleUrl();
        bundlePath = bundlePath.substring(FILE_URL_PREFIX.length());
        if (bundlePath.indexOf(':') < 0 && bundlePath.charAt(0) != '/')
        {
          bundlePath = "/" + bundlePath;
        }
        bundlePath = bundlePath.replace('/', File.separatorChar);

        classpath.add(bundlePath);
      }
    }

    // setup  the main class
    params.setMainClass(_runner.getMainClass());

    // setup any integrator specific parameters:
    final ParametersList programParameters = params.getProgramParametersList();
    SelectedBundle[] bundles = getSelectedBundles();

    programParameters
        .addAll(_runner.getCommandlineParameters(
            bundles, _runner.convertProperties(runConfiguration.getAdditionalProperties())));

    // plus the vm params that the user entered
    params.getVMParametersList().addParametersString(runConfiguration.getVmParameters());

    // and the system properties
    Map<String, String> systemProperties =
        _runner.getSystemProperties(bundles, _runner.convertProperties(runConfiguration.getAdditionalProperties()));
    for (Map.Entry<String, String> entry : systemProperties.entrySet())
    {
      params.getVMParametersList().defineProperty(entry.getKey(), entry.getValue());
    }

    return params;
  }

  /**
   * Here we got the magic. All libs are turned into bundles sorted and returned.
   *
   * @return the sorted list of all bundles to start.
   */
  private SelectedBundle[] getSelectedBundles()
  {

    if (_selectedBundles == null)
    {
      ProgressManager.getInstance().run(new Task.Modal(project, "Fooo", false)
      {
        @Override
        public void run(@NotNull ProgressIndicator progressIndicator)
        {
          progressIndicator.setIndeterminate(false);
          HashSet<SelectedBundle> selectedBundles = new HashSet<SelectedBundle>();
          // the bundles are module names, by now we try to find jar files in the output directory which we can then install
          ModuleManager moduleManager = ModuleManager.getInstance(project);
          int bundleCount = runConfiguration.getBundlesToDeploy().size();
          for (int i = 0; i < bundleCount; i++)
          {
            SelectedBundle selectedBundle = runConfiguration.getBundlesToDeploy().get(i);
            progressIndicator.setFraction(i / bundleCount);
            if (selectedBundle.isModule())
            {
              // use the output jar name if it is a module
              try
              {
                final Module module = moduleManager.findModuleByName(selectedBundle.getName());
                selectedBundle.setBundleUrl(new URL("file", "/", BundleCompiler.getJarFileName(
                    module)).toString());
                // add all the dependencies of the bundle
                String[] depUrls = BundleCompiler.bundlifyLibraries(module, progressIndicator);
                for (String depUrl : depUrls)
                {
                  SelectedBundle dependency =
                      new SelectedBundle("Dependency", depUrl, SelectedBundle.BundleType.PlainLibrary);
                  selectedBundles.add(dependency);
                }
                selectedBundles.add(selectedBundle);
              }
              catch (MalformedURLException e)
              {
                throw new IllegalStateException(e); // should not happen...
              }
            }
            else
            {
              if (selectedBundles.contains(selectedBundle))
              {
                // if the user selected a dependency as runnable library, we need to replace the dependency with
                // the runnable library part
                selectedBundles.remove(selectedBundle);
              }
              selectedBundles.add(selectedBundle);
            }
          }
          HashMap<String, SelectedBundle> finalList = new HashMap<String, SelectedBundle>();

          // filter out bundles which have the same symbolic name
          for (SelectedBundle selectedBundle : selectedBundles)
          {
            String name = CachingBundleInfoProvider.getBundleSymbolicName(selectedBundle.getBundleUrl());
            String version = CachingBundleInfoProvider.getBundleVersions(selectedBundle.getBundleUrl());
            String key = name + version;
            if (!finalList.containsKey(key))
            {
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

  protected OSProcessHandler startProcess() throws ExecutionException
  {
    // run any final configuration steps
    SelectedBundle[] bundles = getSelectedBundles();
    _runner.runCustomInstallationSteps(bundles, _runner.convertProperties(runConfiguration.getAdditionalProperties()));

    OSProcessHandler handler = super.startProcess();
    handler.addProcessListener(new ProcessAdapter()
    {
      public void processTerminated(ProcessEvent event)
      {
        // make sure the runner is disposed when the process exits (so we get rid of the temp folders)
        Disposer.dispose(_runner);
      }
    });
    return handler;
  }

  private static String getRunPath()
  {
    String path = PathManager.getSystemPath() + File.separator + "osmorc" + File.separator + "run";
    File f = new File(path);
    if (!f.exists())
    {
      f.mkdirs();
    }
    return path;
  }

  private FrameworkRunner<PropertiesWrapper> _runner;
  private static final String FILE_URL_PREFIX = "file:///";

  /**
   * Comparator for sorting bundles by their start level.
   *
   * @author <a href="mailto:janthomae@janthomae.de">Jan Thom&auml;</a>
   * @version $Id:$
   */
  public static class StartLevelComparator implements Comparator<SelectedBundle>
  {
    public int compare(SelectedBundle selectedBundle, SelectedBundle selectedBundle2)
    {
      return selectedBundle.getStartLevel() - selectedBundle2.getStartLevel();
    }
  }
}
