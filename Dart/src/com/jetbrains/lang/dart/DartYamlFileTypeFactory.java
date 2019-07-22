// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart;

import com.intellij.openapi.fileTypes.ExactFileNameMatcher;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeConsumer;
import com.intellij.openapi.fileTypes.FileTypeFactory;
import org.jetbrains.annotations.NotNull;

// TODO: switch to <fileType .../> registration when YAMLFileTypeLoader is removed and changed to <fileType .../> registration (at the moment of writing it would break 3rd party plugin)
public class DartYamlFileTypeFactory extends FileTypeFactory {

  public static final String DOT_ANALYSIS_OPTIONS = ".analysis_options";

  @Override
  public void createFileTypes(@NotNull final FileTypeConsumer fileTypeConsumer) {
    // Do not use YAMLFileType.YML directly to avoid class loaders conflict in IDEA Community + Dart Plugin project setup.
    // The problem is that YAMLFileType is instantiated twice in such project setup: by PluginClassLoader and by UrlClassLoader
    final FileType yamlFileType = fileTypeConsumer.getStandardFileTypeByName("YAML");
    if (yamlFileType != null) {
      fileTypeConsumer.consume(yamlFileType, new ExactFileNameMatcher(DOT_ANALYSIS_OPTIONS));
    }
  }
}
