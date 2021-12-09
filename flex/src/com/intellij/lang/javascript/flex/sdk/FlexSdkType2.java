// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.lang.javascript.flex.sdk;

import com.intellij.flex.FlexCommonUtils;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.flashbuilder.FlashBuilderSdkFinder;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.projectRoots.*;
import com.intellij.openapi.roots.JavadocOrderRootType;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.util.NullableComputable;
import com.intellij.openapi.vfs.*;
import com.intellij.util.PathUtil;
import icons.FlexIcons;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class FlexSdkType2 extends SdkType {

  public static final String NAME = "Flex SDK Type (new)";
  public static final String LAST_SELECTED_FLEX_SDK_HOME_KEY = "last.selected.flex.sdk.home";

  public FlexSdkType2() {
    super(NAME);
  }

  @Override
  @Nullable
  public String suggestHomePath() {
    final String path = PropertiesComponent.getInstance().getValue(LAST_SELECTED_FLEX_SDK_HOME_KEY);
    if (path != null) return PathUtil.getParentPath(path);
    return null;
  }

  @NotNull
  @Override
  public Collection<String> suggestHomePaths() {
    final String fbInstallation = FlashBuilderSdkFinder.findFBInstallationPath();
    return fbInstallation == null
           ? Collections.emptySet()
           : Collections.singleton(fbInstallation + "/" + FlashBuilderSdkFinder.SDKS_FOLDER);
  }

  @Override
  public boolean isValidSdkHome(final @NotNull String path) {
    if (path == null) {
      return false;
    }

    final VirtualFile sdkHome = LocalFileSystem.getInstance().findFileByPath(path);
    if (sdkHome == null || !sdkHome.isDirectory()) {
      return false;
    }

    return FlexSdkUtils.doReadFlexSdkVersion(sdkHome) != null || FlexSdkUtils.doReadAirSdkVersion(sdkHome) != null;
  }

  @NotNull
  @Override
  public String suggestSdkName(@Nullable final String currentSdkName, final @NotNull String sdkHome) {
    return PathUtil.getFileName(sdkHome);
  }

  @Override
  public AdditionalDataConfigurable createAdditionalDataConfigurable(@NotNull final SdkModel sdkModel, @NotNull final SdkModificator sdkModificator) {
    return null;
  }

  @Override
  public void saveAdditionalData(@NotNull final SdkAdditionalData additionalData, @NotNull final Element additional) {

  }

  @Override
  @NotNull
  public String getPresentableName() {
    return FlexBundle.message("flex.sdk.presentable.name");
  }

  @NotNull
  public static FlexSdkType2 getInstance() {
    return SdkType.findInstance(FlexSdkType2.class);
  }

  @Override
  public void setupSdkPaths(@NotNull final Sdk sdk) {
    SdkModificator modificator = sdk.getSdkModificator();
    setupSdkPaths(sdk.getHomeDirectory(), modificator);
    modificator.commitChanges();
  }

  @Override
  @NotNull
  public Icon getIcon() {
    return FlexIcons.Flex.Sdk.Flex_sdk;
  }

  @NotNull
  @Override
  public String getHelpTopic() {
    return "reference.project.structure.sdk.flex";
  }

  @Override
  public boolean isRootTypeApplicable(@NotNull final OrderRootType type) {
    return type == OrderRootType.CLASSES || type == OrderRootType.SOURCES || type == JavadocOrderRootType.getInstance();
  }

  @Override
  public String getDefaultDocumentationUrl(@NotNull final Sdk sdk) {
    return "http://help.adobe.com/en_US/FlashPlatform/reference/actionscript/3/";
  }

  @Override
  public String getVersionString(final String sdkHome) {
    final VirtualFile sdkRoot = LocalFileSystem.getInstance().findFileByPath(sdkHome);
    final String flexVersion = FlexSdkUtils.doReadFlexSdkVersion(sdkRoot);
    if (flexVersion != null) return flexVersion;

    final String airVersion = FlexSdkUtils.doReadAirSdkVersion(sdkRoot);
    return airVersion != null ? FlexCommonUtils.AIR_SDK_VERSION_PREFIX + airVersion : FlexBundle.message("flex.sdk.version.unknown");
  }

  private void setupSdkPaths(final VirtualFile sdkRoot, final SdkModificator sdkModificator) {
    if (sdkRoot == null || !sdkRoot.isValid()) {
      return;
    }
    PropertiesComponent.getInstance().setValue(LAST_SELECTED_FLEX_SDK_HOME_KEY, sdkRoot.getPath());
    sdkRoot.refresh(false, true);

    sdkModificator.setVersionString(getVersionString(sdkRoot.getPath()));

    final VirtualFile playerDir = ApplicationManager.getApplication().runWriteAction((NullableComputable<VirtualFile>)() -> {
      final VirtualFile libsDir = LocalFileSystem.getInstance().refreshAndFindFileByPath(sdkRoot.getPath() + "/frameworks/libs");
      if (libsDir != null && libsDir.isDirectory()) {
        libsDir.refresh(false, true);
        return libsDir.findChild("player");
      }
      return null;
    });

    if (playerDir != null) {
      FlexSdkUtils.processPlayerglobalSwcFiles(playerDir, playerglobalSwcFile -> {
        addSwcRoot(sdkModificator, playerglobalSwcFile);
        return true;
      });
    }

    final VirtualFile baseDir = sdkRoot.findChild("frameworks"); // I'm not sure if we need to refresh here
    if (baseDir != null && baseDir.isDirectory()) {
      // let global lib be in the beginning of the list
      addSwcRoots(sdkModificator, baseDir, Collections.singletonList("libs/air/airglobal.swc"), false);
      addFlexSdkSwcRoots(sdkModificator, baseDir);
    }

    final VirtualFile projectsDir = VfsUtilCore.findRelativeFile("frameworks/projects", sdkRoot);
    if (projectsDir != null && projectsDir.isDirectory()) {
      findSourceRoots(projectsDir, sdkModificator);
    }
  }

  public static void addFlexSdkSwcRoots(SdkModificator sdkModificator, VirtualFile frameworksDir) {
    addSwcRoots(sdkModificator, frameworksDir, Arrays.asList("libs", "libs/mx", "libs/air", "libs/mobile", "themes/Mobile"), true);
  }

  private static void findSourceRoots(final VirtualFile dir, final SdkModificator sdkModificator) {
    VfsUtilCore.visitChildrenRecursively(dir, new VirtualFileVisitor<Void>(VirtualFileVisitor.SKIP_ROOT) {
      @Override
      public boolean visitFile(@NotNull VirtualFile child) {
        if (child.isDirectory() && child.getName().equals("src")) {
          sdkModificator.addRoot(child, OrderRootType.SOURCES);
          return false;
        }
        return true;
      }
    });
  }

  private static void addSwcRoots(final SdkModificator sdkModificator,
                                  final VirtualFile baseDir,
                                  final List<String> libRelativePaths,
                                  final boolean skipAirglobalSwc) {
    for (String libRelativePath : libRelativePaths) {
      final VirtualFile libFileOrDir = baseDir.findFileByRelativePath(libRelativePath);
      if (libFileOrDir != null) {
        if (libFileOrDir.isDirectory()) {
          for (final VirtualFile libCandidate : libFileOrDir.getChildren()) {
            if (!libCandidate.isDirectory() && "swc".equalsIgnoreCase(libCandidate.getExtension())) {
              if (!skipAirglobalSwc || !libCandidate.getPath().endsWith("frameworks/libs/air/airglobal.swc")) {
                addSwcRoot(sdkModificator, libCandidate);
              }
            }
          }
        }
        else if ("swc".equalsIgnoreCase(libFileOrDir.getExtension())) {
          if (!skipAirglobalSwc || !libFileOrDir.getPath().endsWith("frameworks/libs/air/airglobal.swc")) {
            addSwcRoot(sdkModificator, libFileOrDir);
          }
        }
      }
    }
  }

  private static void addSwcRoot(@NotNull final SdkModificator sdkModificator, @NotNull final VirtualFile swcFile) {
    final VirtualFile jarRoot = JarFileSystem.getInstance().getJarRootForLocalFile(swcFile);
    if (jarRoot != null) {
      sdkModificator.addRoot(jarRoot, OrderRootType.CLASSES);
    }
  }
}
