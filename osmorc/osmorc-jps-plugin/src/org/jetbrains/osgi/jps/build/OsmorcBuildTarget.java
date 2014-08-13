package org.jetbrains.osgi.jps.build;

import com.intellij.util.Consumer;
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
import org.jetbrains.jps.model.java.JavaSourceRootType;
import org.jetbrains.jps.model.java.JpsJavaExtensionService;
import org.jetbrains.jps.model.module.JpsModule;
import org.jetbrains.jps.model.module.JpsModuleSourceRoot;
import org.jetbrains.osgi.jps.model.JpsOsmorcExtensionService;
import org.jetbrains.osgi.jps.model.JpsOsmorcModuleExtension;
import org.jetbrains.jps.util.JpsPathUtil;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author michael.golubev
 */
public class OsmorcBuildTarget extends ModuleBasedTarget<BuildRootDescriptor> {
  private final JpsOsmorcModuleExtension myExtension;

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
    Collection<ModuleBasedTarget<?>> targets = targetRegistry.getModuleBasedTargets(getModule(),
                                                                                    BuildTargetRegistry.ModuleTargetSelector.PRODUCTION);
    return Collections.<BuildTarget<?>>unmodifiableCollection(targets);
  }

  @NotNull
  @Override
  public List<BuildRootDescriptor> computeRootDescriptors(JpsModel model,
                                                          ModuleExcludeIndex index,
                                                          IgnoredFileIndex ignoredFileIndex,
                                                          BuildDataPaths dataPaths) {
    final List<BuildRootDescriptor> roots = ContainerUtil.newArrayList();
    JpsJavaExtensionService.dependencies(getModule()).recursively().productionOnly().processModules(new Consumer<JpsModule>() {
      @Override
      public void consume(JpsModule module) {
        JpsOsmorcModuleExtension extension = JpsOsmorcExtensionService.getExtension(module);
        if (extension != null) {
          for (JpsModuleSourceRoot sourceRoot : module.getSourceRoots(JavaSourceRootType.SOURCE)) {
            File root = JpsPathUtil.urlToFile(sourceRoot.getUrl());
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
    String jarFileLocation = myExtension.getJarFileLocation();
    return jarFileLocation.isEmpty() ? Collections.<File>emptyList() : Collections.singleton(new File(jarFileLocation));
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
