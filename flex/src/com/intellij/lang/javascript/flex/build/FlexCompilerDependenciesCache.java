// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.lang.javascript.flex.build;

import com.intellij.ProjectTopics;
import com.intellij.flex.model.bc.BuildConfigurationNature;
import com.intellij.lang.javascript.flex.FlexUtils;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexBuildConfiguration;
import com.intellij.lang.javascript.flex.projectStructure.model.impl.Factory;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.ModuleListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class FlexCompilerDependenciesCache {
  private final Project myProject;
  private final Map<Module, Collection<BCInfo>> myCache = new HashMap<>();

  private static final String[] TAGS_FOR_FILE_PATHS_IN_CONFIG_FILE =
    {"<flex-config><compiler><external-library-path><path-element>", "<flex-config><compiler><local-font-paths><path-element>",
      "<flex-config><compiler><library-path><path-element>", "<flex-config><compiler><namespaces><namespace><manifest>",
      /*"<flex-config><compiler><source-path><path-element>", "<flex-config><include-sources><path-element>",*/
      "<flex-config><compiler><theme><filename>", "<flex-config><include-file><path>",
      "<flex-config><include-stylesheet><path>", "<flex-config><file-specs><path-element>",
      "<flex-config><compiler><include-libraries><library>", "<flex-config><compiler><local-fonts-snapshot>",
      "<flex-config><compiler><defaults-css-url>", "<flex-config><compiler><defaults-css-files><filename>",
      "<flex-config><load-config>", "<flex-config><load-externs>", "<flex-config><link-report>",
      "<flex-config><services>", "<flex-config><metadata><raw-metadata>",
      // "<flex-config><output>"   intentionally excluded, because already handled
    };

  public FlexCompilerDependenciesCache(final Project project) {
    myProject = project;

    project.getMessageBus().connect().subscribe(ProjectTopics.MODULES, new ModuleListener() {
      @Override
      public void moduleRemoved(@NotNull final Project project, @NotNull final Module module) {
        myCache.remove(module);
      }
    });
  }

  public void clear() {
    myCache.clear();
  }

  public void markModuleDirty(final Module module) {
    myCache.remove(module);
  }

  public void markBCDirty(final Module module, final FlexBuildConfiguration bc) {
    final Collection<BCInfo> infosForModule = myCache.get(module);
    final BCInfo existingInfo = infosForModule == null ? null : findCacheForBC(infosForModule, bc);
    if (existingInfo != null) {
      infosForModule.remove(existingInfo);
      if (infosForModule.isEmpty()) {
        myCache.remove(module);
      }
    }
  }

  public void markModuleDirtyIfInSourceRoot(final VirtualFile file) {
    if (myCache.isEmpty()) return;

    final ProjectFileIndex fileIndex = ProjectRootManager.getInstance(myProject).getFileIndex();
    final Module module = fileIndex.getModuleForFile(file);
    if (module != null && fileIndex.getSourceRootForFile(file) != null && !fileIndex.isInTestSourceContent(file)) {
      markModuleDirty(module);
    }
  }

  public boolean isNothingChangedSincePreviousCompilation(final Module module, final FlexBuildConfiguration bc) {
    final Collection<BCInfo> infosForModule = myCache.get(module);
    final BCInfo existingInfo = infosForModule == null ? null : findCacheForBC(infosForModule, bc);
    if (existingInfo == null) {
      return false;
    }

    final String[] currentSourceRoots = ModuleRootManager.getInstance(module).getSourceRootUrls();
    if (!Arrays.equals(existingInfo.mySourceRootUrls, currentSourceRoots) || existingInfo.timestampsChanged()) {
      infosForModule.remove(existingInfo);
      if (infosForModule.isEmpty()) {
        myCache.remove(module);
      }
      return false;
    }

    return true;
  }

  public void cacheBC(final Module module, final FlexBuildConfiguration bc, final List<VirtualFile> configFiles) {
    Collection<BCInfo> infosForModule = myCache.get(module);
    if (infosForModule == null) {
      infosForModule = new ArrayList<>();
      myCache.put(module, infosForModule);
    }
    else {
      final BCInfo existingInfo = findCacheForBC(infosForModule, bc);
      if (existingInfo != null) {
        infosForModule.remove(existingInfo);
      }
    }

    final VirtualFile outputFile = FlexCompilationUtils.refreshAndFindFileInWriteAction(bc.getActualOutputFilePath());
    if (outputFile == null) return;

    final BCInfo bcInfo = new BCInfo(Factory.getCopy(bc), ModuleRootManager.getInstance(module).getSourceRootUrls());
    infosForModule.add(bcInfo);

    bcInfo.addFileDependency(outputFile.getPath());

    final String workDirPath = FlexUtils.getFlexCompilerWorkDirPath(module.getProject(), null);
    for (VirtualFile configFile : configFiles) {
      addFileDependencies(bcInfo, configFile, workDirPath);
    }

    if (bc.isTempBCForCompilation() && !bc.getCompilerOptions().getAdditionalConfigFilePath().isEmpty()) {
      bcInfo.addFileDependency(bc.getCompilerOptions().getAdditionalConfigFilePath());
    }

    final BuildConfigurationNature nature = bc.getNature();
    if (nature.isApp() && !nature.isWebPlatform()) {
      if (nature.isDesktopPlatform()) {
        if (!bc.getAirDesktopPackagingOptions().isUseGeneratedDescriptor()) {
          bcInfo.addFileDependency(bc.getAirDesktopPackagingOptions().getCustomDescriptorPath());
        }
      }
      else {
        if (bc.getAndroidPackagingOptions().isEnabled() && !bc.getAndroidPackagingOptions().isUseGeneratedDescriptor()) {
          bcInfo.addFileDependency(bc.getAndroidPackagingOptions().getCustomDescriptorPath());
        }
        if (bc.getIosPackagingOptions().isEnabled() && !bc.getIosPackagingOptions().isUseGeneratedDescriptor()) {
          bcInfo.addFileDependency(bc.getIosPackagingOptions().getCustomDescriptorPath());
        }
      }
    }
  }

  @Nullable
  private static BCInfo findCacheForBC(final @NotNull Collection<BCInfo> bcInfos, @NotNull final FlexBuildConfiguration bc) {
    return ContainerUtil.find(bcInfos, info -> info.myBC.isEqual(bc));
  }

  private static void addFileDependencies(final BCInfo bcInfo, final VirtualFile configFile, final String workDirPath) {
    bcInfo.addFileDependency(configFile.getPath());

    try {
      final Map<String, List<String>> elementsMap =
        FlexUtils.findXMLElements(configFile.getInputStream(), Arrays.asList(TAGS_FOR_FILE_PATHS_IN_CONFIG_FILE));
      for (List<String> filePathList : elementsMap.values()) {
        for (String filePath : filePathList) {
          bcInfo.addFileDependency(filePath, configFile.getParent().getPath(), workDirPath);
        }
      }
    }
    catch (IOException e) {/*ignore*/}
  }

  private static final class BCInfo {
    private final FlexBuildConfiguration myBC;
    private final String[] mySourceRootUrls;
    private final Collection<Pair<File, Long>> myFileToTimestamp = new ArrayList<>();

    private BCInfo(final FlexBuildConfiguration bc, final String[] sourceRootUrls) {
      myBC = bc;
      mySourceRootUrls = sourceRootUrls;
    }

    private void addFileDependency(final String filePath, final String... potentialBaseDirs) {
      final File file = new File(FileUtil.toSystemDependentName(filePath));
      if (file.exists()) {
        myFileToTimestamp.add(Pair.create(file, file.lastModified()));
      }
      else if (potentialBaseDirs != null) {
        for (String baseDir : potentialBaseDirs) {
          final File file1 = new File(FileUtil.toSystemDependentName(baseDir + '/' + filePath));
          if (file1.exists()) {
            myFileToTimestamp.add(Pair.create(file1, file1.lastModified()));
            break;
          }
        }
      }
    }

    public boolean timestampsChanged() {
      for (Pair<File, Long> fileAndTimestamp : myFileToTimestamp) {
        if (fileAndTimestamp.first.lastModified() != fileAndTimestamp.second) {
          return true;
        }
      }
      return false;
    }
  }
}
