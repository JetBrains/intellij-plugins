package com.intellij.aws.cloudformation;

import com.intellij.lang.javascript.json.JSONFileType;
import com.intellij.openapi.fileTypes.FileTypeConsumer;
import com.intellij.openapi.fileTypes.FileTypeFactory;
import org.jetbrains.annotations.NotNull;

public class CloudFormationFileType extends FileTypeFactory {
  public static final String CLOUD_FORMATION_TEMPLATE_EXTENSION = "template";

  @Override
  public void createFileTypes(@NotNull FileTypeConsumer consumer) {
    consumer.consume(JSONFileType.JSON, CLOUD_FORMATION_TEMPLATE_EXTENSION);
  }
}