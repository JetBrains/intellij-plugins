package com.intellij.lang.javascript.flex.sdk;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.flashbuilder.FlashBuilderSdkFinder;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.projectRoots.*;
import com.intellij.openapi.roots.JavadocOrderRootType;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.util.NullableComputable;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.*;
import com.intellij.util.PathUtil;
import com.intellij.util.Processor;
import icons.FlexIcons;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class FlexSdkType2 extends SdkType {

  public static final String NAME = "Flex SDK Type (new)";
  public static final String LAST_SELECTED_FLEX_SDK_HOME_KEY = "last.selected.flex.sdk.home";

  public FlexSdkType2() {
    super(NAME);
  }

  @Nullable
  public String suggestHomePath() {
    final String path = PropertiesComponent.getInstance().getValue(LAST_SELECTED_FLEX_SDK_HOME_KEY);
    if (path != null) return PathUtil.getParentPath(path);

    final String fbInstallation = FlashBuilderSdkFinder.findFBInstallationPath();
    return fbInstallation == null ? null : fbInstallation + "/" + FlashBuilderSdkFinder.SDKS_FOLDER;
  }

  public boolean isValidSdkHome(final String path) {
    if (path == null) {
      return false;
    }

    final VirtualFile sdkHome = LocalFileSystem.getInstance().findFileByPath(path);
    if (sdkHome == null || !sdkHome.isDirectory()) {
      return false;
    }

    return FlexSdkUtils.doReadFlexSdkVersion(sdkHome) != null;
  }

  public String suggestSdkName(final String currentSdkName, final String sdkHome) {
    return PathUtil.getFileName(sdkHome);
  }

  public AdditionalDataConfigurable createAdditionalDataConfigurable(final SdkModel sdkModel, final SdkModificator sdkModificator) {
    return null;
  }

  public void saveAdditionalData(final SdkAdditionalData additionalData, final Element additional) {

  }

  public String getPresentableName() {
    return FlexBundle.message("flex.sdk.presentable.name");
  }

  public Icon getIconForAddAction() {
    return getIcon();
  }

  @NotNull
  public static FlexSdkType2 getInstance() {
    return SdkType.findInstance(FlexSdkType2.class);
  }

  public void setupSdkPaths(final Sdk sdk) {
    SdkModificator modificator = sdk.getSdkModificator();
    setupSdkPaths(sdk.getHomeDirectory(), modificator);
    modificator.commitChanges();
  }

  public Icon getIcon() {
    return FlexIcons.Flex.Sdk.Flex_sdk;
  }

  @NotNull
  @Override
  public String getHelpTopic() {
    return "reference.project.structure.sdk.flex";
  }

  public boolean isRootTypeApplicable(final OrderRootType type) {
    return type == OrderRootType.CLASSES || type == OrderRootType.SOURCES || type == JavadocOrderRootType.getInstance();
  }

  public String getDefaultDocumentationUrl(final @NotNull Sdk sdk) {
    return "http://help.adobe.com/en_US/FlashPlatform/reference/actionscript/3/";
  }

  public String getVersionString(final String sdkHome) {
    return StringUtil.notNullize(FlexSdkUtils.doReadFlexSdkVersion(LocalFileSystem.getInstance().findFileByPath(sdkHome)),
                                 FlexBundle.message("flex.sdk.version.unknown"));
  }

  private void setupSdkPaths(final VirtualFile sdkRoot, final SdkModificator sdkModificator) {
    if (sdkRoot == null || !sdkRoot.isValid()) {
      return;
    }
    PropertiesComponent.getInstance().setValue(LAST_SELECTED_FLEX_SDK_HOME_KEY, sdkRoot.getPath());
    sdkRoot.refresh(false, true);

    sdkModificator.setVersionString(getVersionString(sdkRoot.getPath()));

    final VirtualFile playerDir = ApplicationManager.getApplication().runWriteAction(new NullableComputable<VirtualFile>() {
      public VirtualFile compute() {
        final VirtualFile libsDir = LocalFileSystem.getInstance().refreshAndFindFileByPath(sdkRoot.getPath() + "/frameworks/libs");
        if (libsDir != null && libsDir.isDirectory()) {
          libsDir.refresh(false, true);
          return libsDir.findChild("player");
        }
        return null;
      }
    });

    if (playerDir != null) {
      FlexSdkUtils.processPlayerglobalSwcFiles(playerDir, new Processor<VirtualFile>() {
        public boolean process(final VirtualFile playerglobalSwcFile) {
          addSwcRoot(sdkModificator, playerglobalSwcFile);
          return true;
        }
      });
    }

    final VirtualFile baseDir = sdkRoot.findChild("frameworks"); // I'm not sure if we need to refresh here
    if (baseDir != null && baseDir.isDirectory()) {
      // let global lib be in the beginning of the list
      addSwcRoots(sdkModificator, baseDir, Collections.singletonList("libs/air/airglobal.swc"), false);
      addFlexSdkSwcRoots(sdkModificator, baseDir);
    }

    final VirtualFile projectsDir = VfsUtil.findRelativeFile("frameworks/projects", sdkRoot);
    if (projectsDir != null && projectsDir.isDirectory()) {
      findSourceRoots(projectsDir, sdkModificator);
    }
  }

  public static void addFlexSdkSwcRoots(SdkModificator sdkModificator, VirtualFile frameworksDir) {
    addSwcRoots(sdkModificator, frameworksDir, Arrays.asList("libs", "libs/mx", "libs/air", "libs/mobile", "themes/Mobile"), true);
  }

  private static void findSourceRoots(final VirtualFile dir, final SdkModificator sdkModificator) {
    VfsUtilCore.visitChildrenRecursively(dir, new VirtualFileVisitor(VirtualFileVisitor.SKIP_ROOT) {
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
