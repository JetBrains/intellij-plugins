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
package org.jetbrains.osgi.jps.build;

import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.builders.*;
import org.jetbrains.jps.builders.impl.BuildRootDescriptorImpl;
import org.jetbrains.jps.builders.storage.BuildDataPaths;
import org.jetbrains.jps.incremental.CompileContext;
import org.jetbrains.jps.indices.IgnoredFileIndex;
import org.jetbrains.jps.indices.ModuleExcludeIndex;
import org.jetbrains.jps.model.JpsModel;
import org.jetbrains.jps.model.java.JpsJavaExtensionService;
import org.jetbrains.jps.model.module.JpsModule;
import org.jetbrains.osgi.jps.model.JpsOsmorcExtensionService;
import org.jetbrains.osgi.jps.model.JpsOsmorcModuleExtension;
import org.jetbrains.osgi.jps.util.OsgiBuildUtil;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author michael.golubev
 */
public class OsmorcBuildTarget extends ModuleBasedTarget<BuildRootDescriptor> {
  private final JpsOsmorcModuleExtension myExtension;
  private List<File> myOutputRoots = null;

  public OsmorcBuildTarget(@NotNull JpsOsmorcModuleExtension extension, @NotNull JpsModule module) {
    super(OsmorcBuildTargetType.INSTANCE, module);
    myExtension = extension;
  }

  public JpsOsmorcModuleExtension getExtension() {
    return myExtension;
  }

  @Override
  public String getId() {
    return myModule.getName();
  }

  @Override
  public Collection<BuildTarget<?>> computeDependencies(BuildTargetRegistry targetRegistry, TargetOutputIndex outputIndex) {
    BuildTargetRegistry.ModuleTargetSelector selector = BuildTargetRegistry.ModuleTargetSelector.PRODUCTION;
    return Collections.unmodifiableCollection(targetRegistry.getModuleBasedTargets(getModule(), selector));
  }

  @NotNull
  @Override
  public List<BuildRootDescriptor> computeRootDescriptors(JpsModel model,
                                                          ModuleExcludeIndex index,
                                                          IgnoredFileIndex ignoredFileIndex,
                                                          BuildDataPaths dataPaths) {
    List<BuildRootDescriptor> rootDescriptors = ContainerUtil.newArrayList();

    JpsOsmorcModuleExtension extension = JpsOsmorcExtensionService.getExtension(getModule());
    if (extension != null) {
      File file = extension.getBundleDescriptorFile();
      if (file != null) {
        rootDescriptors.add(new BuildRootDescriptorImpl(this, file, true));
      }
    }

    JpsJavaExtensionService.dependencies(getModule()).recursively().productionOnly().processModules(module -> {
      if (module == getModule() || JpsOsmorcExtensionService.getExtension(module) == null) {
        File root = JpsJavaExtensionService.getInstance().getOutputDirectory(myModule, false);
        if (root != null) {
          rootDescriptors.add(new BuildRootDescriptorImpl(this, root, true));
        }
      }
    });

    return rootDescriptors;
  }

  @Nullable
  @Override
  public BuildRootDescriptor findRootDescriptor(String rootId, BuildRootIndex rootIndex) {
    return ContainerUtil.find(rootIndex.getTargetRoots(this, null), descriptor -> descriptor.getRootId().equals(rootId));
  }

  @NotNull
  @Override
  public String getPresentableName() {
    return "OSGi in module '" + getModule().getName() + "'";
  }

  @NotNull
  @Override
  public Collection<File> getOutputRoots(CompileContext context) {
    if (myOutputRoots == null) {
      String location = myExtension.getJarFileLocation();
      if (!location.isEmpty()) {
        myOutputRoots = ContainerUtil.newArrayList();

        if (myExtension.isUseBndFile()) {
          File bndFile = OsgiBuildUtil.findFileInModuleContentRoots(myModule, myExtension.getBndFileLocation());
          if (bndFile != null && bndFile.isFile()) {
            List<String> bundleNames = BndWrapper.getBundleNames(bndFile);
            if (!bundleNames.isEmpty()) {
              String bundleDir = new File(location).getParent();
              bundleNames.stream().map(name -> new File(bundleDir, name)).forEach(myOutputRoots::add);
            }
          }
        }

        if (myOutputRoots.isEmpty()) {
          myOutputRoots.add(new File(location));
        }
      }
      else {
        myOutputRoots = Collections.emptyList();
      }
    }

    return myOutputRoots;
  }

  @Override
  public boolean isTests() {
    return false;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    OsmorcBuildTarget target = (OsmorcBuildTarget)o;
    return myExtension.equals(target.myExtension);
  }

  @Override
  public int hashCode() {
    return myExtension.hashCode();
  }
}