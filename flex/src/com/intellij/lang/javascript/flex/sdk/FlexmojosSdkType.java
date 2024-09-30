// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.javascript.flex.sdk;

import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.projectRoots.*;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.util.Conditions;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import icons.FlexIcons;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public final class FlexmojosSdkType extends SdkType {

  static final String COMPILER_POM_PATTERN_1 = ".+/com/adobe/flex/compiler/.+/compiler-.+\\.pom";
  static final String COMPILER_POM_PATTERN_2 = ".+/org/apache/flex/compiler/.+/compiler-.+\\.pom";

  public FlexmojosSdkType() {
    super("Flexmojos SDK Type");
  }

  public static FlexmojosSdkType getInstance() {
    return SdkType.findInstance(FlexmojosSdkType.class);
  }

  @Override
  public String suggestHomePath() {
    return null;
  }

  @Override
  public boolean isValidSdkHome(final @NotNull String _path) {
    final String path = FileUtil.toSystemIndependentName(_path);
    return path.matches(COMPILER_POM_PATTERN_1) || path.matches(COMPILER_POM_PATTERN_2);
  }

  @Override
  public @NotNull FileChooserDescriptor getHomeChooserDescriptor() {
    return new FileChooserDescriptor(false, false, false, false, false, false)
      .withFileFilter(Conditions.alwaysFalse())
      .withTitle("SDK of this type can only be created automatically during Maven project import.");
  }

  @Override
  public @NotNull String suggestSdkName(final @Nullable String currentSdkName, final @NotNull String sdkHome) {
    return "Flexmojos SDK " + getVersionString(sdkHome);
  }

  @Override
  public AdditionalDataConfigurable createAdditionalDataConfigurable(final @NotNull SdkModel sdkModel, final @NotNull SdkModificator sdkModificator) {
    return new FlexmojosSdkDataConfigurable();
  }

  @Override
  public SdkAdditionalData loadAdditionalData(final @NotNull Element element) {
    final FlexmojosSdkAdditionalData additionalData = new FlexmojosSdkAdditionalData();
    additionalData.load(element);
    return additionalData;
  }

  @Override
  public void saveAdditionalData(final @NotNull SdkAdditionalData additionalData, final @NotNull Element element) {
    ((FlexmojosSdkAdditionalData)additionalData).save(element);
  }

  @Override
  public @NotNull String getPresentableName() {
    return "Flexmojos SDK";
  }

  @Override
  public @NotNull @NlsContexts.Label String getHomeFieldLabel() {
    return "Flex Compiler POM:";
  }

  @Override
  public boolean isRootTypeApplicable(final @NotNull OrderRootType type) {
    return false;
  }

  @Override
  public Icon getIcon() {
    return FlexIcons.Flex.Sdk.MavenFlex;
  }

  @Override
  public @NotNull String getHelpTopic() {
    return "reference.project.structure.sdk.flexmojos";
  }

  @Override
  public void setupSdkPaths(final @NotNull Sdk sdk) {
    final VirtualFile sdkRoot = sdk.getHomeDirectory();
    if (sdkRoot == null || !sdkRoot.isValid() || sdkRoot.isDirectory()) {
      return;
    }

    final SdkModificator modificator = sdk.getSdkModificator();
    modificator.setVersionString(getVersionString(sdkRoot.getPath()));

    FlexmojosSdkAdditionalData data = (FlexmojosSdkAdditionalData)sdk.getSdkAdditionalData();
    if (data == null) {
      data = new FlexmojosSdkAdditionalData();
      modificator.setSdkAdditionalData(data);
    }
    data.setup(sdkRoot);

    modificator.commitChanges();
  }

  @Override
  public String getVersionString(final @NotNull String sdkHome) {
    // C:/Users/xxx/.m2/repository/com/adobe/flex/compiler/4.10.0-SNAPSHOT/compiler-4.10.0-SNAPSHOT.pom
    final int index1 = sdkHome.lastIndexOf("compiler-");
    return index1 > 0 && sdkHome.endsWith(".pom") ? sdkHome.substring(index1 + "compiler-".length(), sdkHome.length() - ".pom".length())
                                                  : FlexBundle.message("flex.sdk.version.unknown");
  }
}
