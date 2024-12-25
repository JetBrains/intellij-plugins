// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.flex.build;

import com.dynatrace.hash4j.hashing.HashFunnel;
import com.dynatrace.hash4j.hashing.HashSink;
import com.intellij.flex.FlexCommonUtils;
import com.intellij.flex.model.bc.JpsFlexBuildConfiguration;
import com.intellij.flex.model.bc.JpsFlexBuildConfigurationManager;
import com.intellij.flex.model.bc.JpsFlexCompilerOptions;
import com.intellij.util.PathUtilRt;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.containers.FileCollectionFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.jetbrains.jps.ProjectPaths;
import org.jetbrains.jps.builders.*;
import org.jetbrains.jps.builders.storage.BuildDataPaths;
import org.jetbrains.jps.cmdline.ProjectDescriptor;
import org.jetbrains.jps.incremental.CompileContext;
import org.jetbrains.jps.indices.IgnoredFileIndex;
import org.jetbrains.jps.indices.ModuleExcludeIndex;
import org.jetbrains.jps.model.JpsModel;
import org.jetbrains.jps.model.java.JavaSourceRootType;
import org.jetbrains.jps.model.java.JpsJavaExtensionService;
import org.jetbrains.jps.model.java.compiler.JpsJavaCompilerConfiguration;
import org.jetbrains.jps.model.module.JpsModuleSourceRoot;
import org.jetbrains.jps.model.module.JpsTypedModule;
import org.jetbrains.jps.util.JpsPathUtil;

import java.io.File;
import java.util.*;

public final class FlexResourceBuildTarget extends ModuleBasedTarget<BuildRootDescriptor> implements BuildTargetHashSupplier {
  FlexResourceBuildTarget(@NotNull FlexResourceBuildTargetType type, @NotNull JpsTypedModule<JpsFlexBuildConfigurationManager> module) {
    super(type, module);
  }

  @Override
  public @NotNull String getId() {
    return getModule().getName();
  }

  @Override
  public @NotNull JpsTypedModule<JpsFlexBuildConfigurationManager> getModule() {
    //noinspection unchecked
    return (JpsTypedModule<JpsFlexBuildConfigurationManager>)super.getModule();
  }

  @Override
  public @NotNull Collection<BuildTarget<?>> computeDependencies(@NotNull BuildTargetRegistry targetRegistry, @NotNull TargetOutputIndex outputIndex) {
    return Collections.emptyList();
  }

  @Override
  public @NotNull List<BuildRootDescriptor> computeRootDescriptors(final @NotNull JpsModel model,
                                                                   final @NotNull ModuleExcludeIndex index,
                                                                   final @NotNull IgnoredFileIndex ignoredFileIndex,
                                                                   final @NotNull BuildDataPaths dataPaths) {
    final List<BuildRootDescriptor> result = new ArrayList<>();

    final JavaSourceRootType rootType = getTargetType() == FlexResourceBuildTargetType.PRODUCTION ? JavaSourceRootType.SOURCE
                                                                                                  : JavaSourceRootType.TEST_SOURCE;

    for (JpsModuleSourceRoot sourceRoot : getModule().getSourceRoots(rootType)) {
      final File root = JpsPathUtil.urlToFile(sourceRoot.getUrl());
      result.add(new FlexSourceRootDescriptor(this, root));
    }

    return result;
  }

  @Override
  public boolean isTests() {
    return ((FlexResourceBuildTargetType)getTargetType()).isTests();
  }

  @Override
  public @Nullable BuildRootDescriptor findRootDescriptor(final @NotNull String rootId, final @NotNull BuildRootIndex rootIndex) {
    for (BuildRootDescriptor descriptor : rootIndex.getTargetRoots(this, null)) {
      if (descriptor.getRootId().equals(rootId)) {
        return descriptor;
      }
    }
    return null;
  }

  @Override
  public @NotNull String getPresentableName() {
    return getTargetType().getTypeId() + ":" + getModule().getName();
  }

  @Override
  public @Unmodifiable @NotNull Collection<File> getOutputRoots(@NotNull CompileContext context) {
    if (getTargetType() == FlexResourceBuildTargetType.TEST) {
      final File outputDir = ProjectPaths.getModuleOutputDir(getModule(), true);
      return ContainerUtil.createMaybeSingletonList(outputDir);
    }

    Set<File> result = FileCollectionFactory.createCanonicalFileSet();
    for (JpsFlexBuildConfiguration bc : getModule().getProperties().getBuildConfigurations()) {
      if (FlexCommonUtils.canHaveResourceFiles(bc.getNature())) {
        result.add(new File(PathUtilRt.getParentPath(bc.getActualOutputFilePath())));
      }
    }

    return result;
  }

  @Override
  public void computeConfigurationDigest(@NotNull ProjectDescriptor projectDescriptor, @NotNull HashSink hash) {
    hash.putString(getModule().getName());
    List<JpsFlexBuildConfiguration> configurations = getModule().getProperties().getBuildConfigurations();
    for (JpsFlexBuildConfiguration bc : configurations) {
      if (!bc.isSkipCompile() &&
          FlexCommonUtils.canHaveResourceFiles(bc.getNature()) &&
          bc.getCompilerOptions().getResourceFilesMode() != JpsFlexCompilerOptions.ResourceFilesMode.None) {
        hash.putString(bc.getName());
        hash.putString(PathUtilRt.getParentPath(bc.getActualOutputFilePath()));
        hash.putString(bc.getCompilerOptions().getResourceFilesMode().toString());

        if (bc.getCompilerOptions().getResourceFilesMode() == JpsFlexCompilerOptions.ResourceFilesMode.ResourcePatterns) {
          JpsJavaCompilerConfiguration c = JpsJavaExtensionService.getInstance().getCompilerConfiguration(getModule().getProject());
          hash.putBoolean(true);
          hash.putOrderedIterable(c.getResourcePatterns(), HashFunnel.forString());
        }
        else {
          hash.putBoolean(false);
        }
      }
    }
    hash.putInt(configurations.size());
  }
}
