package com.intellij.lang.javascript.flex.sdk;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.FlexFacetType;
import com.intellij.lang.javascript.flex.flashbuilder.FlashBuilderSdkFinder;
import com.intellij.openapi.projectRoots.*;
import com.intellij.openapi.roots.JavadocOrderRootType;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.PathUtil;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

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
    FlexSdkUtils.setupSdkPaths(sdk.getHomeDirectory(), null, modificator);
    modificator.commitChanges();
  }

  public Icon getIcon() {
    return FlexFacetType.ourFlexIcon;
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
}
