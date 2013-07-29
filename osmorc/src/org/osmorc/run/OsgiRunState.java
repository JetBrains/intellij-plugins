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
import com.intellij.execution.configurations.JavaCommandLineState;
import com.intellij.execution.configurations.JavaParameters;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.compiler.DummyCompileContext;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.util.Ref;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.osmorc.facet.OsmorcFacet;
import org.osmorc.frameworkintegration.*;
import org.osmorc.make.BundleCompiler;
import org.osmorc.run.ui.SelectedBundle;

import java.net.URL;
import java.util.*;

/**
 * RunState for launching the OSGI framework.
 *
 * @author <a href="mailto:janthomae@janthomae.de">Jan Thom&auml;</a>
 * @author Robert F. Beeger (robert@beeger.net)
 */
public class OsgiRunState extends JavaCommandLineState {
  private final OsgiRunConfiguration myRunConfiguration;
  private final FrameworkRunner myRunner;

  public OsgiRunState(@NotNull ExecutionEnvironment environment, @NotNull OsgiRunConfiguration configuration) throws ExecutionException {
    super(environment);
    myRunConfiguration = configuration;

    FrameworkInstanceDefinition instance = myRunConfiguration.getInstanceToUse();
    if (instance == null) {
      throw new CantRunException("Incorrect OSGi run configuration: framework not set");
    }
    FrameworkIntegrator integrator = FrameworkIntegratorRegistry.getInstance().findIntegratorByInstanceDefinition(instance);
    if (integrator == null) {
      throw new CantRunException("Internal error: missing integrator for " + instance);
    }
    myRunner = integrator.createFrameworkRunner();
  }

  public boolean requiresRemoteDebugger() {
    return myRunner instanceof ExternalVMFrameworkRunner;
  }

  @Override
  protected JavaParameters createJavaParameters() throws ExecutionException {
    return myRunner.createJavaParameters(myRunConfiguration, getSelectedBundles());
  }

  /**
   * Here we got the magic. All libs are turned into bundles sorted and returned.
   */
  private List<SelectedBundle> getSelectedBundles() throws ExecutionException {
    final Ref<List<SelectedBundle>> result = Ref.create();
    final Ref<ExecutionException> error = Ref.create();

    ProgressManager.getInstance().run(new Task.Modal(myRunConfiguration.getProject(), "Preparing bundles...", false) {
      @Override
      public void run(@NotNull ProgressIndicator progressIndicator) {
        progressIndicator.setIndeterminate(false);

        try {
          Set<SelectedBundle> selectedBundles = new HashSet<SelectedBundle>();
          // the bundles are module names, by now we try to find jar files in the output directory which we can then install
          ModuleManager moduleManager = ModuleManager.getInstance(myRunConfiguration.getProject());
          int bundleCount = myRunConfiguration.getBundlesToDeploy().size();
          for (int i = 0; i < bundleCount; i++) {
            progressIndicator.setFraction((double)i / bundleCount);

            SelectedBundle selectedBundle = myRunConfiguration.getBundlesToDeploy().get(i);
            if (selectedBundle.isModule()) {
              // use the output jar name if it is a module
              Module module = moduleManager.findModuleByName(selectedBundle.getName());
              if (module == null) {
                throw new CantRunException("Module '" + selectedBundle.getName() + "' does no longer exist." +
                                           " Please check your run configuration.");
              }
              if (!OsmorcFacet.hasOsmorcFacet(module)) {
                // actually this should not happen, but it seemed to happen once, so we check this here.
                throw new CantRunException("Module '" + selectedBundle.getName() + "' has no OSGi facet, but should have." +
                                           " Please re-add the OSGi facet to this module.");
              }
              selectedBundle.setBundleUrl(new URL("file", "/", BundleCompiler.getJarFileName(module)).toString());
              selectedBundles.add(selectedBundle);
              // add all the library dependencies of the bundle
              String[] depUrls = BundleCompiler.bundlifyLibraries(module, progressIndicator, DummyCompileContext.getInstance());
              for (String depUrl : depUrls) {
                SelectedBundle dependency = new SelectedBundle("Dependency", depUrl, SelectedBundle.BundleType.PlainLibrary);
                selectedBundles.add(dependency);
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

          // filter out bundles which have the same symbolic name
          Map<String, SelectedBundle> filteredBundles = new HashMap<String, SelectedBundle>();
          for (SelectedBundle selectedBundle : selectedBundles) {
            String name = CachingBundleInfoProvider.getBundleSymbolicName(selectedBundle.getBundleUrl());
            String version = CachingBundleInfoProvider.getBundleVersions(selectedBundle.getBundleUrl());
            String key = name + version;
            if (!filteredBundles.containsKey(key)) {
              filteredBundles.put(key, selectedBundle);
            }
          }

          List<SelectedBundle> sortedBundles = ContainerUtil.newArrayList(filteredBundles.values());
          Collections.sort(sortedBundles, new StartLevelComparator());
          result.set(sortedBundles);
        }
        catch (CantRunException e) {
          error.set(e);
        }
        catch (Throwable t) {
          error.set(new CantRunException("Internal error: " + t.getMessage()));
        }
      }
    });

    if (!result.isNull()) {
      return result.get();
    }
    else {
      throw error.get();
    }
  }

  public static class StartLevelComparator implements Comparator<SelectedBundle> {
    public int compare(SelectedBundle selectedBundle, SelectedBundle selectedBundle2) {
      return selectedBundle.getStartLevel() - selectedBundle2.getStartLevel();
    }
  }
}
