package com.intellij.lang.javascript.flex.sdk;

import com.intellij.lang.javascript.flex.FlexFacetType;
import com.intellij.lang.javascript.flex.IFlexSdkType;
import com.intellij.lang.javascript.flex.TargetPlayerUtils;
import com.intellij.openapi.projectRoots.*;
import com.intellij.openapi.roots.JavadocOrderRootType;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * Created by IntelliJ IDEA.
 * User: Maxim.Mossienko
 * Date: Dec 1, 2007
 * Time: 11:49:06 PM
 * To change this template use File | Settings | File Templates.
 */
public class FlexSdkType extends SdkType implements IFlexSdkType {

  public static final String NAME = "Flex SDK Type";

  public FlexSdkType() {
    super(NAME);
  }

  public Subtype getSubtype() {
    return Subtype.Flex;
  }

  @Nullable
  public String suggestHomePath() {
    return null;
  }

  public boolean isValidSdkHome(final String path) {
    return FlexSdkUtils.isValidSdkRoot(this, path != null ? VfsUtil.findRelativeFile(path, null) : null);
  }

  public String suggestSdkName(final String currentSdkName, final String sdkHome) {
    final VirtualFile sdkRoot = LocalFileSystem.getInstance().findFileByPath(sdkHome);
    String name = sdkHome.substring(sdkHome.lastIndexOf('/') + 1);
    if (sdkRoot != null) {
      final String targetPlayerVersion = TargetPlayerUtils.getTargetPlayerFromConfigXmlFile(sdkRoot, this);
      if (targetPlayerVersion != null && targetPlayerVersion.length() > 0) {
        final Pair<String, String> majorMinor = TargetPlayerUtils.getPlayerMajorMinorVersion(targetPlayerVersion);
        name += " (player " + majorMinor.first + "." + majorMinor.second + ")";
      }
    }
    return name;
  }

  public AdditionalDataConfigurable createAdditionalDataConfigurable(final SdkModel sdkModel, final SdkModificator sdkModificator) {
    return null;
  }

  public void saveAdditionalData(final SdkAdditionalData additionalData, final Element additional) {

  }

  public String getPresentableName() {
    return "Flex SDK";
  }

  public Icon getIconForAddAction() {
    return getIcon();
  }

  public static FlexSdkType getInstance() {
    return SdkType.findInstance(FlexSdkType.class);
  }

  public void setupSdkPaths(final Sdk sdk) {
    FlexSdkUtils.setupSdkPaths(sdk);
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
