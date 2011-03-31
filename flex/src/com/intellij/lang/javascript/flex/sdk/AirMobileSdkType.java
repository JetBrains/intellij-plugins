package com.intellij.lang.javascript.flex.sdk;

import com.intellij.lang.javascript.flex.IFlexSdkType;
import com.intellij.openapi.projectRoots.*;
import com.intellij.openapi.roots.JavadocOrderRootType;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.vfs.VfsUtil;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class AirMobileSdkType extends SdkType implements IFlexSdkType {

  public static final Icon airMobileIcon = IconLoader.getIcon("airmobile.png", AirMobileSdkType.class);

  public AirMobileSdkType() {
    super("AIR Mobile SDK Type");
  }

  public Subtype getSubtype() {
    return Subtype.AIRMobile;
  }

  @Nullable
  public String suggestHomePath() {
    return null;
  }

  public boolean isValidSdkHome(final String path) {
    return FlexSdkUtils.isValidSdkRoot(this, path != null ? VfsUtil.findRelativeFile(path, null) : null);
  }

  public String suggestSdkName(final String currentSdkName, final String sdkHome) {
    return sdkHome.substring(sdkHome.lastIndexOf('/') + 1) + " AIR Mobile";
  }

  public AdditionalDataConfigurable createAdditionalDataConfigurable(final SdkModel sdkModel, final SdkModificator sdkModificator) {
    return null;
  }

  public void saveAdditionalData(final SdkAdditionalData additionalData, final Element additional) {
  }

  public String getPresentableName() {
    return "AIR Mobile SDK";
  }

  public Icon getIconForAddAction() {
    return getIcon();
  }

  public static AirMobileSdkType getInstance() {
    return SdkType.findInstance(AirMobileSdkType.class);
  }

  public void setupSdkPaths(final Sdk sdk) {
    FlexSdkUtils.setupSdkPaths(sdk);
  }

  public Icon getIcon() {
    return airMobileIcon;
  }

  @NotNull
  @Override
  public String getHelpTopic() {
    return "reference.project.structure.sdk.airmobile";
  }

  public boolean isRootTypeApplicable(final OrderRootType type) {
    return type == OrderRootType.CLASSES || type == OrderRootType.SOURCES || type == JavadocOrderRootType.getInstance();
  }
}

