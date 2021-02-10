// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.osgi.jps.build;

import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.xmlb.XmlSerializer;
import org.jdom.output.XMLOutputter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.builders.*;
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
import java.io.PrintWriter;
import java.util.ArrayList;
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
  public void writeConfiguration(ProjectDescriptor pd, PrintWriter out) {
    int configHash = 0;
    JpsOsmorcModuleExtension extension = JpsOsmorcExtensionService.getExtension(getModule());
    if (extension != null) {
      OsmorcModuleExtensionProperties p = ((JpsOsmorcModuleExtensionImpl)myExtension).getProperties();
      configHash = new XMLOutputter().outputString(XmlSerializer.serialize(p)).hashCode();
    }
    out.write(Integer.toHexString(configHash));
  }

  @Override
  public Collection<BuildTarget<?>> computeDependencies(BuildTargetRegistry targetRegistry, TargetOutputIndex outputIndex) {
    BuildTargetRegistry.ModuleTargetSelector selector = BuildTargetRegistry.ModuleTargetSelector.PRODUCTION;
    return Collections.unmodifiableCollection(targetRegistry.getModuleBasedTargets(getModule(), selector));
  }

  @Override
  public @NotNull List<BuildRootDescriptor> computeRootDescriptors(JpsModel model,
                                                                   ModuleExcludeIndex index,
                                                                   IgnoredFileIndex ignoredFileIndex,
                                                                   BuildDataPaths dataPaths) {
    List<BuildRootDescriptor> rootDescriptors = new ArrayList<>();

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

  @Override
  public @Nullable BuildRootDescriptor findRootDescriptor(String rootId, BuildRootIndex rootIndex) {
    return ContainerUtil.find(rootIndex.getTargetRoots(this, null), descriptor -> descriptor.getRootId().equals(rootId));
  }

  @Override
  public @NotNull String getPresentableName() {
    return "OSGi in module '" + getModule().getName() + "'";
  }

  @Override
  public @NotNull Collection<File> getOutputRoots(CompileContext context) {
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
