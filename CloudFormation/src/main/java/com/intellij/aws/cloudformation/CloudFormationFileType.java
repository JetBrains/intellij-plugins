package com.intellij.aws.cloudformation;

import com.intellij.icons.AllIcons;
import com.intellij.json.JsonLanguage;
import com.intellij.openapi.fileTypes.LanguageFileType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class CloudFormationFileType extends LanguageFileType {
  public static final CloudFormationFileType INSTANCE = new CloudFormationFileType();
  public static final String DEFAULT_EXTENSION = "template";

  public CloudFormationFileType() {
    super(JsonLanguage.INSTANCE);
  }

  @NotNull
  @Override
  public String getName() {
    return "AWSCloudFormation";
  }

  @NotNull
  @Override
  public String getDescription() {
    return "AWS CloudFormation templates";
  }

  @NotNull
  @Override
  public String getDefaultExtension() {
    return DEFAULT_EXTENSION;
  }

  @Nullable
  @Override
  public Icon getIcon() {
    return AllIcons.FileTypes.Json;
  }
}
