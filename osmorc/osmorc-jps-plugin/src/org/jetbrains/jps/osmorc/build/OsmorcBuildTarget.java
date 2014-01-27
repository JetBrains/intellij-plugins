package org.jetbrains.jps.osmorc.build;

import com.intellij.util.Consumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.builders.*;
import org.jetbrains.jps.builders.impl.BuildRootDescriptorImpl;
import org.jetbrains.jps.builders.storage.BuildDataPaths;
import org.jetbrains.jps.incremental.CompileContext;
import org.jetbrains.jps.indices.IgnoredFileIndex;
import org.jetbrains.jps.indices.ModuleExcludeIndex;
import org.jetbrains.jps.model.JpsModel;
import org.jetbrains.jps.model.java.JavaSourceRootType;
import org.jetbrains.jps.model.module.JpsModule;
import org.jetbrains.jps.model.module.JpsModuleSourceRoot;
import org.jetbrains.jps.osmorc.model.JpsOsmorcExtensionService;
import org.jetbrains.jps.osmorc.model.JpsOsmorcModuleExtension;
import org.jetbrains.jps.util.JpsPathUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author michael.golubev
 */
public class OsmorcBuildTarget extends ModuleBasedTarget<BuildRootDescriptor> {

  private final JpsOsmorcModuleExtension myExtension;

  public OsmorcBuildTarget(JpsOsmorcModuleExtension extension) {
    super(OsmorcBuildTargetType.INSTANCE, extension.getModule());
    myExtension = extension;
  }

  @Override
  public String getId() {
    return myModule.getName();
  }

  @Override
  public Collection<BuildTarget<?>> computeDependencies(BuildTargetRegistry targetRegistry, TargetOutputIndex outputIndex) {
    return Collections.<BuildTarget<?>>unmodifiableCollection(
      targetRegistry.getModuleBasedTargets(myExtension.getModule(), BuildTargetRegistry.ModuleTargetSelector.PRODUCTION));
  }

  @NotNull
  @Override
  public List<BuildRootDescriptor> computeRootDescriptors(JpsModel model,
                                                          ModuleExcludeIndex index,
                                                          IgnoredFileIndex ignoredFileIndex,
                                                          BuildDataPaths dataPaths) {
    final List<BuildRootDescriptor> roots = new ArrayList<BuildRootDescriptor>();

    myExtension.processAffectedModules(new Consumer<JpsModule>() {

      @Override
      public void consume(JpsModule module) {
        JpsOsmorcModuleExtension extension = JpsOsmorcExtensionService.getInstance().getExtension(module);
        if (extension == null) {
          return;
        }

        for (JpsModuleSourceRoot sourceRoot : module.getSourceRoots(JavaSourceRootType.SOURCE)) {
          final File root = JpsPathUtil.urlToFile(sourceRoot.getUrl());
          roots.add(new BuildRootDescriptorImpl(OsmorcBuildTarget.this, root, true));
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
    return "OSGi in module '" + myExtension.getModule().getName() + "'";
  }

  @NotNull
  @Override
  public Collection<File> getOutputRoots(CompileContext context) {
    String jarFileLocation = myExtension.getJarFileLocation();
    return jarFileLocation.isEmpty() ? Collections.<File>emptyList() : Collections.singleton(new File(jarFileLocation));
  }

  public JpsOsmorcModuleExtension getExtension() {
    return myExtension;
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
