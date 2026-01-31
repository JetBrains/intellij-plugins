// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.javascript.flex.sdk;

import com.intellij.flex.FlexCommonUtils;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.flashbuilder.FlashBuilderSdkFinder;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.projectRoots.AdditionalDataConfigurable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkAdditionalData;
import com.intellij.openapi.projectRoots.SdkModel;
import com.intellij.openapi.projectRoots.SdkModificator;
import com.intellij.openapi.projectRoots.SdkType;
import com.intellij.openapi.roots.JavadocOrderRootType;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.vfs.JarFileSystem;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileVisitor;
import com.intellij.util.PathUtil;
import icons.FlexIcons;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public final class FlexSdkType2 extends SdkType {

  public static final String NAME = "Flex SDK Type (new)";
  public static final String LAST_SELECTED_FLEX_SDK_HOME_KEY = "last.selected.flex.sdk.home";

  public FlexSdkType2() {
    super(NAME);
  }

  @Override
  public @Nullable String suggestHomePath() {
    final String path = PropertiesComponent.getInstance().getValue(LAST_SELECTED_FLEX_SDK_HOME_KEY);
    if (path != null) return PathUtil.getParentPath(path);
    return null;
  }

  @Override
  public @NotNull Collection<String> suggestHomePaths() {
    final String fbInstallation = FlashBuilderSdkFinder.findFBInstallationPath();
    return fbInstallation == null
           ? Collections.emptySet()
           : Collections.singleton(fbInstallation + "/" + FlashBuilderSdkFinder.SDKS_FOLDER);
  }

  @Override
  public boolean isValidSdkHome(final @NotNull String path) {
    final VirtualFile sdkHome = LocalFileSystem.getInstance().findFileByPath(path);
    if (sdkHome == null || !sdkHome.isDirectory()) {
      return false;
    }

    return FlexSdkUtils.doReadFlexSdkVersion(sdkHome) != null || FlexSdkUtils.doReadAirSdkVersion(sdkHome) != null;
  }

  @Override
  public @NotNull String suggestSdkName(final @Nullable String currentSdkName, final @NotNull String sdkHome) {
    return PathUtil.getFileName(sdkHome);
  }

  @Override
  public AdditionalDataConfigurable createAdditionalDataConfigurable(final @NotNull SdkModel sdkModel, final @NotNull SdkModificator sdkModificator) {
    return null;
  }

  @Override
  public void saveAdditionalData(final @NotNull SdkAdditionalData additionalData, final @NotNull Element additional) {

  }

  @Override
  public @NotNull String getPresentableName() {
    return FlexBundle.message("flex.sdk.presentable.name");
  }

  @Override
  public void setupSdkPaths(final @NotNull Sdk sdk) {
    SdkModificator modificator = sdk.getSdkModificator();
    setupSdkPaths(sdk.getHomeDirectory(), modificator);
    ApplicationManager.getApplication().invokeAndWait(() -> WriteAction.run(() -> modificator.commitChanges()));
  }

  @Override
  public @NotNull Icon getIcon() {
    return FlexIcons.Flex.Sdk.Flex_sdk;
  }

  @Override
  public @NotNull String getHelpTopic() {
    return "reference.project.structure.sdk.flex";
  }

  @Override
  public boolean isRootTypeApplicable(final @NotNull OrderRootType type) {
    return type == OrderRootType.CLASSES || type == OrderRootType.SOURCES || type == JavadocOrderRootType.getInstance();
  }

  @Override
  public String getDefaultDocumentationUrl(final @NotNull Sdk sdk) {
    return "http://help.adobe.com/en_US/FlashPlatform/reference/actionscript/3/";
  }

  public static @NotNull FlexSdkType2 getInstance() {
    return SdkType.findInstance(FlexSdkType2.class);
  }

  @Override
  public String getVersionString(final @NotNull String sdkHome) {
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

    VirtualFile libsDir = LocalFileSystem.getInstance().findFileByPath(sdkRoot.getPath() + "/frameworks/libs");
    VirtualFile playerDir = libsDir != null && libsDir.isDirectory() ? libsDir.findChild("player") : null;
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

  private static void addSwcRoot(final @NotNull SdkModificator sdkModificator, final @NotNull VirtualFile swcFile) {
    final VirtualFile jarRoot = JarFileSystem.getInstance().getJarRootForLocalFile(swcFile);
    if (jarRoot != null) {
      sdkModificator.addRoot(jarRoot, OrderRootType.CLASSES);
    }
  }
}
