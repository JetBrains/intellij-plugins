// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.flex.build;

import com.intellij.flex.FlexCommonUtils;
import com.intellij.flex.model.bc.JpsFlexBuildConfiguration;
import com.intellij.flex.model.bc.JpsFlexBuildConfigurationManager;
import com.intellij.flex.model.bc.JpsFlexCompilerOptions;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.PathUtilRt;
import com.intellij.util.containers.FileCollectionFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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
import java.io.PrintWriter;
import java.util.*;

public final class FlexResourceBuildTarget extends ModuleBasedTarget<BuildRootDescriptor> {
  FlexResourceBuildTarget(final FlexResourceBuildTargetType type, @NotNull JpsTypedModule<JpsFlexBuildConfigurationManager> module) {
    super(type, module);
  }

  @Override
  public String getId() {
    return getModule().getName();
  }

  @Override
  @NotNull
  public JpsTypedModule<JpsFlexBuildConfigurationManager> getModule() {
    //noinspection unchecked
    return (JpsTypedModule<JpsFlexBuildConfigurationManager>)super.getModule();
  }

  @Override
  public Collection<BuildTarget<?>> computeDependencies(BuildTargetRegistry targetRegistry, TargetOutputIndex outputIndex) {
    return Collections.emptyList();
  }

  @NotNull
  @Override
  public List<BuildRootDescriptor> computeRootDescriptors(final JpsModel model,
                                                          final ModuleExcludeIndex index,
                                                          final IgnoredFileIndex ignoredFileIndex,
                                                          final BuildDataPaths dataPaths) {
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

  @Nullable
  @Override
  public BuildRootDescriptor findRootDescriptor(final String rootId, final BuildRootIndex rootIndex) {
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
    return getTargetType().getTypeId() + ":" + getModule().getName();
  }

  @NotNull
  @Override
  public Collection<File> getOutputRoots(CompileContext context) {
    if (getTargetType() == FlexResourceBuildTargetType.TEST) {
      final File outputDir = ProjectPaths.getModuleOutputDir(getModule(), true);
      return outputDir == null ? Collections.emptyList() : Collections.singletonList(outputDir);
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
  public void writeConfiguration(ProjectDescriptor pd, final PrintWriter out) {
    out.println("Module: " + getModule().getName());
    for (JpsFlexBuildConfiguration bc : getModule().getProperties().getBuildConfigurations()) {
      if (!bc.isSkipCompile() &&
          FlexCommonUtils.canHaveResourceFiles(bc.getNature()) &&
          bc.getCompilerOptions().getResourceFilesMode() != JpsFlexCompilerOptions.ResourceFilesMode.None) {

        out.print("BC: " + bc.getName());
        out.print(", output folder: " + PathUtilRt.getParentPath(bc.getActualOutputFilePath()));
        out.print(", mode: " + bc.getCompilerOptions().getResourceFilesMode());

        if (bc.getCompilerOptions().getResourceFilesMode() == JpsFlexCompilerOptions.ResourceFilesMode.ResourcePatterns) {
          final JpsJavaCompilerConfiguration c = JpsJavaExtensionService.getInstance().getCompilerConfiguration(getModule().getProject());
          out.print(", patterns: " + StringUtil.join(c.getResourcePatterns(), " "));
        }

        out.println();
      }
    }
  }
}
