package com.jetbrains.lang.dart;

import com.intellij.openapi.fileTypes.FileTypeConsumer;
import com.intellij.openapi.fileTypes.FileTypeFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.YAMLFileType;

public class DartYamlFileTypeFactory extends FileTypeFactory {
  @Override
  public void createFileTypes(@NotNull final FileTypeConsumer fileTypeConsumer) {
    fileTypeConsumer.consume(YAMLFileType.YML, "analysis_options");
  }
}
