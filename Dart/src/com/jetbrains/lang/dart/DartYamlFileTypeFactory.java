package com.jetbrains.lang.dart;

import com.intellij.openapi.fileTypes.ExactFileNameMatcher;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeConsumer;
import com.intellij.openapi.fileTypes.FileTypeFactory;
import org.jetbrains.annotations.NotNull;

public class DartYamlFileTypeFactory extends FileTypeFactory {
  @Override
  public void createFileTypes(@NotNull final FileTypeConsumer fileTypeConsumer) {
    // Do not use YAMLFileType.YML directly to avoid class loaders conflict in IDEA Community + Dart Plugin project setup.
    // The problem is that YAMLFileType is instantiated twice in such project setup: by PluginClassLoader and by UrlClassLoader
    final FileType yamlFileType = fileTypeConsumer.getStandardFileTypeByName("YAML");
    if (yamlFileType != null) {
      fileTypeConsumer.consume(yamlFileType, new ExactFileNameMatcher(".analysis_options"));
    }
  }
}
