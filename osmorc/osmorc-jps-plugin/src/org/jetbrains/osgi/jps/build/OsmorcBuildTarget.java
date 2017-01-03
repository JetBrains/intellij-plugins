/*
 * Copyright 2000-2015 JetBrains s.r.o.
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

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.builders.BuildRootDescriptor;
import org.jetbrains.jps.builders.BuildRootIndex;
import org.jetbrains.jps.builders.BuildTarget;
import org.jetbrains.jps.builders.BuildTargetRegistry;
import org.jetbrains.jps.builders.ModuleBasedTarget;
import org.jetbrains.jps.builders.TargetOutputIndex;
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

import com.intellij.util.Consumer;
import com.intellij.util.containers.ContainerUtil;

import aQute.bnd.build.Project;
import aQute.bnd.osgi.Builder;

/**
 * @author michael.golubev
 */
public class OsmorcBuildTarget extends ModuleBasedTarget<BuildRootDescriptor> {
  private JpsOsmorcModuleExtension myExtension;
  private Project myProject;
  private Collection<File> myOutputFiles;

  public OsmorcBuildTarget(@NotNull Project project, @NotNull JpsModule module) throws Exception {
    super(OsmorcBuildTargetType.INSTANCE, module);
    myProject = project;

    Collection<File> outputFiles = new ArrayList<File>();
    Collection<? extends Builder> subBuilders = project.getSubBuilders();
    for (Builder subBuilder : subBuilders) {
      outputFiles.add(project.getOutputFile(subBuilder.getBsn()));
    }

    myOutputFiles = outputFiles;
  }

  public OsmorcBuildTarget(@NotNull JpsOsmorcModuleExtension extension, @NotNull JpsModule module) {
    super(OsmorcBuildTargetType.INSTANCE, module);
    myExtension = extension;

    String jarFileLocation = extension.getJarFileLocation();
    myOutputFiles = jarFileLocation.isEmpty() ? Collections.<File>emptyList() : Collections.singleton(new File(jarFileLocation));
  }

  public JpsOsmorcModuleExtension getExtension() {
    return myExtension;
  }

  public Project getProject() {
    return myProject;
  }

  @Override
  public String getId() {
    return myModule.getName();
  }

  @Override
  public Collection<BuildTarget<?>> computeDependencies(BuildTargetRegistry targetRegistry, TargetOutputIndex outputIndex) {
    BuildTargetRegistry.ModuleTargetSelector selector = BuildTargetRegistry.ModuleTargetSelector.PRODUCTION;
    Collection<ModuleBasedTarget<?>> targets = targetRegistry.getModuleBasedTargets(getModule(), selector);
    return Collections.<BuildTarget<?>>unmodifiableCollection(targets);
  }

  @NotNull
  @Override
  public List<BuildRootDescriptor> computeRootDescriptors(JpsModel model,
                                                          ModuleExcludeIndex index,
                                                          IgnoredFileIndex ignoredFileIndex,
                                                          BuildDataPaths dataPaths) {
    final List<BuildRootDescriptor> roots = ContainerUtil.newArrayList();

    JpsOsmorcModuleExtension extension = JpsOsmorcExtensionService.getExtension(getModule());
    if (extension != null) {
      File file = extension.getBundleDescriptorFile();
      if (file != null) {
        roots.add(new BuildRootDescriptorImpl(this, file, true));
      }
    }

    JpsJavaExtensionService.dependencies(getModule()).recursively().productionOnly().processModules(new Consumer<JpsModule>() {
      @Override
      public void consume(JpsModule module) {
        if (module == getModule() || JpsOsmorcExtensionService.getExtension(module) == null) {
          File root = JpsJavaExtensionService.getInstance().getOutputDirectory(myModule, false);
          if (root != null) {
            roots.add(new BuildRootDescriptorImpl(OsmorcBuildTarget.this, root, true));
          }
        }
      }
    });

    return roots;
  }

  @Nullable
  @Override
  public BuildRootDescriptor findRootDescriptor(String rootId, BuildRootIndex rootIndex) {
    for (BuildRootDescriptor descriptor : rootIndex.getTargetRoots(this, null)) {
      if (descriptor.getRootId().equals(rootId)) {
        return descriptor;
      }
    }
    return null;
  }

  @NotNull
  @Override
  public String getPresentableName() {
    return "OSGi in module '" + getModule().getName() + "'";
  }

  @NotNull
  @Override
  public Collection<File> getOutputRoots(CompileContext context) {
      return myOutputFiles;
  }

  @Override
  public boolean isTests() {
    return false;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;

    OsmorcBuildTarget that = (OsmorcBuildTarget) o;

    return myOutputFiles != null ? myOutputFiles.equals(that.myOutputFiles) : that.myOutputFiles == null;
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (myOutputFiles != null ? myOutputFiles.hashCode() : 0);
    return result;
  }
}
