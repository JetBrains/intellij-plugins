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
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.configurations.JavaCommandLineState;
import com.intellij.execution.configurations.JavaParameters;
import com.intellij.execution.filters.ArgumentFileFilter;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.util.Ref;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.osgi.jps.build.CachingBundleInfoProvider;
import org.jetbrains.osgi.jps.build.OsgiBuildException;
import org.osmorc.facet.OsmorcFacet;
import org.osmorc.frameworkintegration.FrameworkInstanceDefinition;
import org.osmorc.frameworkintegration.FrameworkIntegrator;
import org.osmorc.frameworkintegration.FrameworkIntegratorRegistry;
import org.osmorc.frameworkintegration.FrameworkRunner;
import org.osmorc.i18n.OsmorcBundle;
import org.osmorc.make.BundleCompiler;
import org.osmorc.run.ui.SelectedBundle;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * RunState for launching the OSGi framework.
 *
 * @author <a href="mailto:janthomae@janthomae.de">Jan Thomä</a>
 * @author <a href="mailto:robert@beeger.net">Robert F. Beeger</a>
 */
public class OsgiRunState extends JavaCommandLineState {
  private static final Logger LOG = Logger.getInstance(OsgiRunState.class);

  private final OsgiRunConfiguration myRunConfiguration;
  private final FrameworkRunner myRunner;
  private final ArgumentFileFilter myArgFileFilter = new ArgumentFileFilter();

  public OsgiRunState(@NotNull ExecutionEnvironment environment, @NotNull OsgiRunConfiguration configuration) throws ExecutionException {
    super(environment);
    myRunConfiguration = configuration;

    FrameworkInstanceDefinition instance = myRunConfiguration.getInstanceToUse();
    if (instance == null) {
      throw new CantRunException(OsmorcBundle.message("run.configuration.no.framework"));
    }
    FrameworkIntegrator integrator = FrameworkIntegratorRegistry.getInstance().findIntegratorByInstanceDefinition(instance);
    if (integrator == null) {
      throw new CantRunException(OsmorcBundle.message("run.configuration.missing.integrator", instance));
    }
    myRunner = integrator.createFrameworkRunner();

    addConsoleFilters(myArgFileFilter);
  }

  @Override
  protected JavaParameters createJavaParameters() throws ExecutionException {
    return myRunner.createJavaParameters(myRunConfiguration, getSelectedBundles());
  }

  @Override
  protected GeneralCommandLine createCommandLine() throws ExecutionException {
    GeneralCommandLine commandLine = super.createCommandLine();

    File argFile = myRunner.getArgumentFile();
    if (argFile != null) {
      myArgFileFilter.setPath(argFile.getPath());
      OSProcessHandler.deleteFileOnTermination(commandLine, argFile);
    }

    return commandLine;
  }

  /**
   * Here we got the magic. All libs are turned into bundles sorted and returned.
   */
  private List<SelectedBundle> getSelectedBundles() throws ExecutionException {
    final Ref<List<SelectedBundle>> result = Ref.create();
    final Ref<ExecutionException> error = Ref.create();

    ProgressIndicator progressIndicator = Objects.requireNonNull(ProgressManager.getInstance().getProgressIndicator());
    progressIndicator.setText(OsmorcBundle.message("run.configuration.progress.preparing.bundles"));
    progressIndicator.setIndeterminate(false);

    ApplicationManager.getApplication().runReadAction(() -> {
      try {
        Set<SelectedBundle> selectedBundles = new HashSet<>();
        // the bundles are module names, by now we try to find jar files in the output directory which we can then install
        ModuleManager moduleManager = ModuleManager.getInstance(myRunConfiguration.getProject());
        BundleCompiler bundleCompiler = new BundleCompiler(progressIndicator);

        List<SelectedBundle> bundlesToDeploy = myRunConfiguration.getBundlesToDeploy();
        for (int i = 0; i < bundlesToDeploy.size(); i++) {
          progressIndicator.setFraction((double)i / bundlesToDeploy.size());

          SelectedBundle selectedBundle = bundlesToDeploy.get(i);
          if (selectedBundle.isModule()) {
            // use the output jar name if it is a module
            String name = selectedBundle.getName();
            Module module = moduleManager.findModuleByName(name);
            if (module == null) throw new CantRunException(OsmorcBundle.message("run.configuration.missing.module", name));
            OsmorcFacet facet = OsmorcFacet.getInstance(module);
            if (facet == null) throw new CantRunException(OsmorcBundle.message("run.configuration.missing.facet", name));
            String jar = facet.getConfiguration().getJarFileLocation();
            if (!new File(jar).exists()) throw new CantRunException(OsmorcBundle.message("run.configuration.missing.bundle", jar));
            selectedBundle.setBundlePath(jar);
            selectedBundles.add(selectedBundle);
            // add all the library dependencies of the bundle
            List<String> paths = bundleCompiler.bundlifyLibraries(module);
            for (String path : paths) {
              selectedBundles.add(new SelectedBundle(SelectedBundle.BundleType.PlainLibrary, "Dependency", path));
            }
          }
          else {
            // if a user selected a dependency as runnable library, we need to replace the dependency with the runnable library part
            selectedBundles.remove(selectedBundle);
            selectedBundles.add(selectedBundle);
          }
        }

        // detects bundles which have the same symbolic name
        Map<String, SelectedBundle> filter = new HashMap<>();
        for (SelectedBundle selectedBundle : selectedBundles) {
          String path = selectedBundle.getBundlePath();
          if (path != null) {
            String key = CachingBundleInfoProvider.getBundleSymbolicName(path) + ':' + CachingBundleInfoProvider.getBundleVersion(path);
            SelectedBundle previous = filter.put(key, selectedBundle);
            if (previous != null) {
              throw new CantRunException(OsmorcBundle.message("run.configuration.bundles.clash", key, previous, selectedBundle));
            }
          }
        }

        List<SelectedBundle> sortedBundles = new ArrayList<>(selectedBundles);
        sortedBundles.sort(START_LEVEL_COMPARATOR);
        result.set(sortedBundles);
      }
      catch (CantRunException e) {
        error.set(e);
      }
      catch (OsgiBuildException e) {
        LOG.warn(e);
        error.set(new CantRunException(e.getMessage()));
      }
      catch (Throwable t) {
        LOG.error(t);
        error.set(new CantRunException(OsmorcBundle.message("run.configuration.internal.error", t.getMessage())));
      }
    });

    if (!result.isNull()) {
      return result.get();
    }
    else {
      throw error.get();
    }
  }

  public static final Comparator<SelectedBundle> START_LEVEL_COMPARATOR = Comparator.comparingInt(SelectedBundle::getStartLevel);
}
