/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jetbrains.plugins.ruby.motion;

import com.intellij.facet.FacetManager;
import com.intellij.facet.ModifiableFacetModel;
import com.intellij.facet.ProjectFacetManager;
import com.intellij.openapi.application.Result;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.platform.DirectoryProjectConfigurator;
import com.intellij.util.ActionRunner;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.ruby.motion.facet.RubyMotionFacet;
import org.jetbrains.plugins.ruby.motion.facet.RubyMotionFacetConfiguration;
import org.jetbrains.plugins.ruby.motion.facet.RubyMotionFacetType;
import org.jetbrains.plugins.ruby.remote.RubyRemoteInterpreterManager;
import org.jetbrains.plugins.ruby.ruby.sdk.RubySdkType;
import org.jetbrains.plugins.ruby.ruby.sdk.RubySdkUtil;
import org.jetbrains.plugins.ruby.utils.IdeaInternalUtil;

/**
 * @author Dennis.Ushakov
 */
public class RubyMotionFacetConfigurator implements DirectoryProjectConfigurator {
  private static final Logger LOG = Logger.getInstance(RubyMotionFacetConfigurator.class);

  @Override
  public void configureProject(Project project, @NotNull VirtualFile baseDir, Ref<Module> moduleRef) {
    final Module[] modules = ModuleManager.getInstance(project).getModules();
    if (modules.length == 0) {
      LOG.error("RubyMotionFacetConfigurator must run after PlatformProjectConfigurator");
      return;
    }
    final Module module = modules[0];
    if (RubyMotionUtil.getInstance().isRubyMotionModule(module)) {
      configure(baseDir, module);
    }
    configureSdk(project);
  }

  private static void configureSdk(final Project project) {
    final ProjectRootManager manager = ProjectRootManager.getInstance(project);
    final Sdk sdk = manager.getProjectSdk();
    if (!RubySdkUtil.isNativeRuby19(sdk)) {
      for (final Sdk newSdk : ProjectJdkTable.getInstance().getSdksOfType(RubySdkType.getInstance())) {
        if (RubySdkUtil.isNativeRuby19(newSdk) && !RubySdkUtil.isNativeRuby20(newSdk) &&
            !RubyRemoteInterpreterManager.getInstance().isRemoteSdk(newSdk)) {
          new WriteAction() {
            @Override
            protected void run(@NotNull Result result) throws Throwable {
              ProjectRootManager.getInstance(project).setProjectSdk(newSdk);
            }
          }.execute();
        }
      }
    }
  }

  public static void configure(VirtualFile baseDir, Module module) {
    final RubyMotionFacet existingFacet = RubyMotionFacet.getInstance(module);
    if (existingFacet != null) {
      return;
    }
    FacetManager facetManager = FacetManager.getInstance(module);
    final ModifiableFacetModel model = facetManager.createModifiableModel();
    RubyMotionFacetType facetType = RubyMotionFacetType.getInstance();
    RubyMotionFacetConfiguration configuration = ProjectFacetManager.getInstance(module.getProject()).createDefaultConfiguration(facetType);
    //configuration.setProjectRootPath(baseDir.getPath());
    VirtualFile testFolder = baseDir.findChild("spec");
    final ModifiableRootModel rootModel = ModuleRootManager.getInstance(module).getModifiableModel();
    if (testFolder != null) {
      //configuration.setTestPath(testFolder.getPath());
      addTestSources(testFolder, rootModel);
    }
    VirtualFile libFolder = baseDir.findChild("app");
    if (libFolder != null) {
      //configuration.setLibPath(libFolder.getPath());
    }
    RubyMotionFacet.updateMotionLibrary(rootModel);
    WriteAction.run(() -> rootModel.commit());
    RubyMotionFacet facet = facetManager.createFacet(facetType, facetType.getDefaultFacetName(), configuration, null);
    model.addFacet(facet);
    new WriteAction() {
      protected void run(@NotNull final Result result) throws Throwable {
        model.commit();
      }
    }.execute();
    RubyMotionUtilExt.createMotionRunConfiguration(module);
  }

  private static void addTestSources(final VirtualFile testFolder, final ModifiableRootModel model) {
    final ContentEntry[] contentEntries = model.getContentEntries();
    if (contentEntries.length > 0) {
      final ContentEntry contentEntry = contentEntries[0];
      if (testFolder != null) {
        contentEntry.addSourceFolder(testFolder.getUrl(), true);
      }
    }
  }
}