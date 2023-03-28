// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.flex.build;

import com.intellij.flex.FlexCommonBundle;
import com.intellij.flex.FlexCommonUtils;
import com.intellij.flex.model.JpsFlexProjectLevelCompilerOptionsExtension;
import com.intellij.flex.model.bc.*;
import com.intellij.flex.model.bc.impl.JpsFlexBCState;
import com.intellij.flex.model.bc.impl.JpsFlexCompilerOptionsImpl;
import com.intellij.flex.model.run.JpsBCBasedRunnerParameters;
import com.intellij.flex.model.run.JpsFlashRunConfigurationType;
import com.intellij.flex.model.run.JpsFlexUnitRunConfigurationType;
import com.intellij.openapi.util.JDOMUtil;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.util.PathUtilRt;
import com.intellij.util.xmlb.XmlSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.builders.*;
import org.jetbrains.jps.builders.storage.BuildDataPaths;
import org.jetbrains.jps.cmdline.ProjectDescriptor;
import org.jetbrains.jps.incremental.CompileContext;
import org.jetbrains.jps.indices.IgnoredFileIndex;
import org.jetbrains.jps.indices.ModuleExcludeIndex;
import org.jetbrains.jps.model.JpsModel;
import org.jetbrains.jps.model.JpsProject;
import org.jetbrains.jps.model.java.JavaSourceRootType;
import org.jetbrains.jps.model.library.JpsLibrary;
import org.jetbrains.jps.model.library.JpsOrderRootType;
import org.jetbrains.jps.model.module.JpsModuleSourceRoot;
import org.jetbrains.jps.model.runConfiguration.JpsRunConfigurationType;
import org.jetbrains.jps.model.runConfiguration.JpsTypedRunConfiguration;
import org.jetbrains.jps.util.JpsPathUtil;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public final class FlexBuildTarget extends BuildTarget<BuildRootDescriptor> {

  private final @NotNull JpsFlexBuildConfiguration myBC;
  private final @NotNull String myId;

  private FlexBuildTarget(final @NotNull JpsFlexBuildConfiguration bc, final @NotNull String id) {
    super(FlexBuildTargetType.INSTANCE);
    myBC = bc;
    myId = id;
  }

  /**
   * @param forcedDebugStatus {@code true} or {@code false} means that this bc is compiled for further packaging and we need swf to have corresponding debug status;
   *                          {@code null} means that bc is compiled as is (i.e. as configured) without any modifications
   */
  @NotNull
  public static FlexBuildTarget create(final @NotNull JpsFlexBuildConfiguration bc, final @Nullable Boolean forcedDebugStatus) {
    final String id = FlexCommonUtils.getBuildTargetId(bc.getModule().getName(), bc.getName(), forcedDebugStatus);

    if (forcedDebugStatus == null) {
      return new FlexBuildTarget(bc, id);
    }
    else {
      // must not use getTemporaryCopyForCompilation() here because additional config file must not be merged with the generated one when compiling swf for release or AIR package
      final JpsFlexBuildConfiguration bcCopy = bc.getModule().getProperties().createCopy(bc);
      final String additionalOptions = FlexCommonUtils
        .removeOptions(bc.getCompilerOptions().getAdditionalOptions(), "debug", "compiler.debug");
      bcCopy.getCompilerOptions().setAdditionalOptions(additionalOptions + " -debug=" + forcedDebugStatus.toString());
      return new FlexBuildTarget(bcCopy, id);
    }
  }

  @Nullable
  public static FlexBuildTarget create(final JpsProject project,
                                       final @NotNull JpsRunConfigurationType<? extends JpsBCBasedRunnerParameters<?>> runConfigType,
                                       final @NotNull String runConfigName) {
    assert runConfigType instanceof JpsFlashRunConfigurationType ||
           runConfigType instanceof JpsFlexUnitRunConfigurationType : runConfigType;

    final String runConfigTypeId = runConfigType instanceof JpsFlashRunConfigurationType ? JpsFlashRunConfigurationType.ID
                                                                                         : JpsFlexUnitRunConfigurationType.ID;
    final JpsTypedRunConfiguration<? extends JpsBCBasedRunnerParameters<?>> runConfig =
      FlexCommonUtils.findRunConfiguration(project, runConfigType, runConfigName);
    final JpsFlexBuildConfiguration bc = runConfig == null ? null : runConfig.getProperties().getBC(project);

    final String id = FlexCommonUtils.getBuildTargetIdForRunConfig(runConfigTypeId, runConfigName);
    return bc == null ? null : new FlexBuildTarget(bc, id);
  }

  @Override
  public @NotNull String getId() {
    return myId;
  }

  @NotNull
  public JpsFlexBuildConfiguration getBC() {
    return myBC;
  }

  @Override
  public @NotNull Collection<BuildTarget<?>> computeDependencies(@NotNull BuildTargetRegistry targetRegistry, @NotNull TargetOutputIndex outputIndex) {
    final ArrayList<BuildTarget<?>> result = new ArrayList<>();

    final FlexResourceBuildTargetType type = FlexCommonUtils.isFlexUnitBC(myBC) ? FlexResourceBuildTargetType.TEST
                                                                                : FlexResourceBuildTargetType.PRODUCTION;
    result.add(new FlexResourceBuildTarget(type, myBC.getModule()));

    for (JpsFlexDependencyEntry entry : myBC.getDependencies().getEntries()) {
      if (entry instanceof JpsFlexBCDependencyEntry) {
        final JpsFlexBuildConfiguration dependencyBC = ((JpsFlexBCDependencyEntry)entry).getBC();
        if (dependencyBC != null) {
          result.add(create(dependencyBC, null));
        }
      }
    }
    result.trimToSize();
    return result;
  }

  @Override
  @NotNull
  public List<BuildRootDescriptor> computeRootDescriptors(final @NotNull JpsModel model,
                                                          final @NotNull ModuleExcludeIndex index,
                                                          final @NotNull IgnoredFileIndex ignoredFileIndex,
                                                          final @NotNull BuildDataPaths dataPaths) {
    final List<BuildRootDescriptor> result = new ArrayList<>();

    final Collection<File> srcRoots = new ArrayList<>();

    for (JpsModuleSourceRoot sourceRoot : myBC.getModule().getSourceRoots(JavaSourceRootType.SOURCE)) {
      final File root = JpsPathUtil.urlToFile(sourceRoot.getUrl());
      result.add(new FlexSourceRootDescriptor(this, root));
      srcRoots.add(root);
    }

    if (FlexCommonUtils.isFlexUnitBC(myBC)) {
      for (JpsModuleSourceRoot sourceRoot : myBC.getModule().getSourceRoots(JavaSourceRootType.TEST_SOURCE)) {
        final File root = JpsPathUtil.urlToFile(sourceRoot.getUrl());
        result.add(new FlexSourceRootDescriptor(this, root));
        srcRoots.add(root);
      }
    }

    for (final JpsFlexDependencyEntry entry : myBC.getDependencies().getEntries()) {
      if (entry instanceof JpsFlexBCDependencyEntry) {
        final JpsFlexBuildConfiguration dependencyBC = ((JpsFlexBCDependencyEntry)entry).getBC();
        if (dependencyBC != null) {
          result.add(new FlexSourceRootDescriptor(this, new File(dependencyBC.getActualOutputFilePath())));
        }
      }
      else if (entry instanceof JpsLibraryDependencyEntry) {
        final JpsLibrary library = ((JpsLibraryDependencyEntry)entry).getLibrary();
        if (library != null) {
          for (String rootUrl : library.getRootUrls(JpsOrderRootType.COMPILED)) {
            result.add(new FlexSourceRootDescriptor(this, JpsPathUtil.urlToFile(rootUrl)));
          }
        }
      }
    }

    final BuildConfigurationNature nature = myBC.getNature();

    if (nature.isWebPlatform() && nature.isApp() && myBC.isUseHtmlWrapper() && !myBC.getWrapperTemplatePath().isEmpty()) {
      addIfNotUnderRoot(result, new File(myBC.getWrapperTemplatePath()), srcRoots);
    }

    if (FlexCommonUtils.canHaveRLMsAndRuntimeStylesheets(myBC)) {
      for (String cssPath : myBC.getCssFilesToCompile()) {
        if (!cssPath.isEmpty()) {
          addIfNotUnderRoot(result, new File(cssPath), srcRoots);
        }
      }
    }

    if (!myBC.getCompilerOptions().getAdditionalConfigFilePath().isEmpty()) {
      addIfNotUnderRoot(result, new File(myBC.getCompilerOptions().getAdditionalConfigFilePath()), srcRoots);
    }

    if (nature.isApp()) {
      if (nature.isDesktopPlatform()) {
        addAirDescriptorPathIfCustom(result, myBC.getAirDesktopPackagingOptions(), srcRoots);
      }
      else if (nature.isMobilePlatform()) {
        if (myBC.getAndroidPackagingOptions().isEnabled()) {
          addAirDescriptorPathIfCustom(result, myBC.getAndroidPackagingOptions(), srcRoots);
        }
        if (myBC.getIosPackagingOptions().isEnabled()) {
          addAirDescriptorPathIfCustom(result, myBC.getIosPackagingOptions(), srcRoots);
        }
      }
    }

    return result;
  }

  private void addIfNotUnderRoot(final List<BuildRootDescriptor> descriptors, final File file, final Collection<File> roots) {
    for (File root : roots) {
      if (FileUtil.isAncestor(root, file, false)) {
        return;
      }
    }

    descriptors.add(new FlexSourceRootDescriptor(this, file));
  }

  private void addAirDescriptorPathIfCustom(final List<BuildRootDescriptor> descriptors,
                                            final JpsAirPackagingOptions packagingOptions,
                                            final Collection<File> srcRoots) {
    if (!packagingOptions.isUseGeneratedDescriptor() && !packagingOptions.getCustomDescriptorPath().isEmpty()) {
      addIfNotUnderRoot(descriptors, new File(packagingOptions.getCustomDescriptorPath()), srcRoots);
    }
  }

  @Override
  @Nullable
  public BuildRootDescriptor findRootDescriptor(final @NotNull String rootId, final @NotNull BuildRootIndex rootIndex) {
    for (BuildRootDescriptor descriptor : rootIndex.getTargetRoots(this, null)) {
      if (descriptor.getRootId().equals(rootId)) {
        return descriptor;
      }
    }

    return null;
  }

  @Override
  @NotNull
  public String getPresentableName() {
    return FlexCommonBundle.message("bc.0.module.1", myBC.getName(), myBC.getModule().getName());
  }

  @Override
  @NotNull
  public Collection<File> getOutputRoots(@NotNull CompileContext context) {
    return Collections.singleton(new File(PathUtilRt.getParentPath(myBC.getActualOutputFilePath())));
  }

  @Override
  public void writeConfiguration(@NotNull ProjectDescriptor pd, final @NotNull PrintWriter out) {
    out.println("id: " + myId);

    out.println(JDOMUtil.writeElement(XmlSerializer.serialize(JpsFlexBCState.getState(myBC))));

    final JpsFlexModuleOrProjectCompilerOptions moduleOptions = myBC.getModule().getProperties().getModuleLevelCompilerOptions();
    out.println(JDOMUtil.writeElement(XmlSerializer.serialize(((JpsFlexCompilerOptionsImpl)moduleOptions).getState())));

    final JpsFlexModuleOrProjectCompilerOptions projectOptions =
      JpsFlexProjectLevelCompilerOptionsExtension.getProjectLevelCompilerOptions(myBC.getModule().getProject());
    out.println(JDOMUtil.writeElement(XmlSerializer.serialize(((JpsFlexCompilerOptionsImpl)projectOptions).getState())));
  }

  public String toString() {
    return myId;
  }

  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    final FlexBuildTarget target = (FlexBuildTarget)o;

    if (!myId.equals(target.myId)) return false;

    return true;
  }

  public int hashCode() {
    return myId.hashCode();
  }
}
