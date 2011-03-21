package com.intellij.lang.javascript.flex.sdk;

import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.IFlexSdkType;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.project.ProjectBundle;
import com.intellij.openapi.projectRoots.*;
import com.intellij.openapi.roots.JavadocOrderRootType;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class AirSdkType extends SdkType implements IFlexSdkType {

  public static final Icon airIcon = IconLoader.getIcon("air.png", AirSdkType.class);

  public AirSdkType() {
    super("AIR SDK Type");
  }

  public Subtype getSubtype() {
    return Subtype.AIR;
  }

  @Nullable
  public String suggestHomePath() {
    return null;
  }

  public boolean isValidSdkHome(final String path) {
    return FlexSdkUtils.isAirSdkRoot(path != null ? VfsUtil.findRelativeFile(path, null) : null);
  }

  public String suggestSdkName(final String currentSdkName, final String sdkHome) {
    return sdkHome.substring(sdkHome.lastIndexOf('/') + 1) + " AIR";
  }

  public AdditionalDataConfigurable createAdditionalDataConfigurable(final SdkModel sdkModel, final SdkModificator sdkModificator) {
    return null;
  }

  public void saveAdditionalData(final SdkAdditionalData additionalData, final Element additional) {
  }

  public String getPresentableName() {
    return "AIR SDK";
  }

  public Icon getIconForAddAction() {
    return getIcon();
  }

  public static AirSdkType getInstance() {
    return SdkType.findInstance(AirSdkType.class);
  }

  public void setupSdkPaths(final Sdk sdk) {
    FlexSdkUtils.setupSdkPaths(sdk);
  }

  public Icon getIcon() {
    return airIcon;
  }

  @Override
  public FileChooserDescriptor getHomeChooserDescriptor() {
    final FileChooserDescriptor descriptor = new FileChooserDescriptor(false, true, false, false, false, false) {
      public void validateSelectedFiles(VirtualFile[] files) throws Exception {
        if (files.length != 0) {
          final String selectedPath = files[0].getPath();
          if (!isValidSdkHome(selectedPath)) {
            throw new Exception(FlexBundle.message("air.sdk.home.incorrect"));
          }
        }
      }
    };

    descriptor.setTitle(ProjectBundle.message("sdk.configure.home.title", getPresentableName()));
    descriptor.setDescription(FlexBundle.message("select.air.sdk.home.description"));
    return descriptor;
  }

  @NotNull
  @Override
  public String getHelpTopic() {
    return "reference.project.structure.sdk.air";
  }

  public boolean isRootTypeApplicable(final OrderRootType type) {
    return type == OrderRootType.CLASSES || type == OrderRootType.SOURCES || type == JavadocOrderRootType.getInstance();
  }
}

