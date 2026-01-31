// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.osgi.jps.build;

import com.dynatrace.hash4j.hashing.HashSink;
import com.intellij.openapi.util.JDOMUtil;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.xmlb.XmlSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.builders.BuildRootDescriptor;
import org.jetbrains.jps.builders.BuildRootIndex;
import org.jetbrains.jps.builders.BuildTarget;
import org.jetbrains.jps.builders.BuildTargetHashSupplier;
import org.jetbrains.jps.builders.BuildTargetRegistry;
import org.jetbrains.jps.builders.ModuleBasedTarget;
import org.jetbrains.jps.builders.TargetOutputIndex;
import org.jetbrains.jps.builders.impl.BuildRootDescriptorImpl;
import org.jetbrains.jps.builders.storage.BuildDataPaths;
import org.jetbrains.jps.cmdline.ProjectDescriptor;
import org.jetbrains.jps.incremental.CompileContext;
import org.jetbrains.jps.indices.IgnoredFileIndex;
import org.jetbrains.jps.indices.ModuleExcludeIndex;
import org.jetbrains.jps.model.JpsModel;
import org.jetbrains.jps.model.java.JpsJavaExtensionService;
import org.jetbrains.jps.model.module.JpsModule;
import org.jetbrains.osgi.jps.model.JpsOsmorcExtensionService;
import org.jetbrains.osgi.jps.model.JpsOsmorcModuleExtension;
import org.jetbrains.osgi.jps.model.impl.JpsOsmorcModuleExtensionImpl;
import org.jetbrains.osgi.jps.model.impl.OsmorcModuleExtensionProperties;
import org.jetbrains.osgi.jps.util.OsgiBuildUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author michael.golubev
 */
public final class OsmorcBuildTarget extends ModuleBasedTarget<BuildRootDescriptor> implements BuildTargetHashSupplier {
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
  public @NotNull String getId() {
    return myModule.getName();
  }

  @Override
  public void computeConfigurationDigest(@NotNull ProjectDescriptor projectDescriptor, @NotNull HashSink hash) {
    JpsOsmorcModuleExtension extension = JpsOsmorcExtensionService.getExtension(getModule());
    if (extension == null) {
      hash.putBoolean(false);
    }
    else {
      OsmorcModuleExtensionProperties p = ((JpsOsmorcModuleExtensionImpl)myExtension).getProperties();
      hash.putBoolean(true);
      hash.putString(JDOMUtil.write(XmlSerializer.serialize(p)));
    }
  }

  @Override
  public @NotNull Collection<BuildTarget<?>> computeDependencies(@NotNull BuildTargetRegistry targetRegistry, @NotNull TargetOutputIndex outputIndex) {
    BuildTargetRegistry.ModuleTargetSelector selector = BuildTargetRegistry.ModuleTargetSelector.PRODUCTION;
    return Collections.unmodifiableCollection(targetRegistry.getModuleBasedTargets(getModule(), selector));
  }

  @Override
  public @NotNull List<BuildRootDescriptor> computeRootDescriptors(@NotNull JpsModel model,
                                                                   @NotNull ModuleExcludeIndex index,
                                                                   @NotNull IgnoredFileIndex ignoredFileIndex,
                                                                   @NotNull BuildDataPaths dataPaths) {
    List<BuildRootDescriptor> rootDescriptors = new ArrayList<>();

    JpsOsmorcModuleExtension extension = JpsOsmorcExtensionService.getExtension(getModule());
    if (extension != null) {
      File file = extension.getBundleDescriptorFile();
      if (file != null) {
        rootDescriptors.add(new BuildRootDescriptorImpl(this, file, true));
      }
      else if (extension.isUseBndMavenPlugin()) {
        // fallback to watching the default bnd file
        File mavenProjectPath = OsgiBuildUtil.findMavenProjectPath(dataPaths, myModule);
        if (mavenProjectPath != null) {
          file = new File(mavenProjectPath.getParentFile(), "bnd.bnd");
          rootDescriptors.add(new BuildRootDescriptorImpl(this, file, true));
        }
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

  @Override
  public @Nullable BuildRootDescriptor findRootDescriptor(@NotNull String rootId, @NotNull BuildRootIndex rootIndex) {
    return ContainerUtil.find(rootIndex.getTargetRoots(this, null), descriptor -> descriptor.getRootId().equals(rootId));
  }

  @Override
  public @NotNull String getPresentableName() {
    return "OSGi in module '" + getModule().getName() + "'";
  }

  @Override
  public @NotNull Collection<File> getOutputRoots(@NotNull CompileContext context) {
    if (myOutputRoots == null) {
      String location = myExtension.getJarFileLocation();
      if (!location.isEmpty()) {
        myOutputRoots = new ArrayList<>();

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
