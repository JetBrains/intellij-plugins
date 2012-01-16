package com.intellij.lang.javascript.flex.sdk;

import com.intellij.lang.javascript.flex.FlexFacetType;
import com.intellij.openapi.projectRoots.*;
import com.intellij.openapi.roots.JavadocOrderRootType;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.vfs.VfsUtil;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class FlexSdkType2 extends SdkType {

  public static final String NAME = "Flex SDK Type (new)";

  public FlexSdkType2() {
    super(NAME);
  }

  @Nullable
  public String suggestHomePath() {
    return null; // TODO detect Flash Builder installation? check env variables?
  }

  public boolean isValidSdkHome(final String path) {
    return path != null && FlexSdkUtils.isValidSdkRoot(FlexSdkType.getInstance(), VfsUtil.findRelativeFile(path, null));
  }

  public String suggestSdkName(final String currentSdkName, final String sdkHome) {
    return FlexSdkType.suggestSdkName(sdkHome, FlexSdkType.getInstance());
  }

  public AdditionalDataConfigurable createAdditionalDataConfigurable(final SdkModel sdkModel, final SdkModificator sdkModificator) {
    return null;
  }

  public void saveAdditionalData(final SdkAdditionalData additionalData, final Element additional) {

  }

  public String getPresentableName() {
    return "Flex SDK (new)";
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
}
