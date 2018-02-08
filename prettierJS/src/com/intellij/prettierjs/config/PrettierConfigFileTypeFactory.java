package com.intellij.prettierjs.config;

import com.intellij.json.JsonFileType;
import com.intellij.openapi.fileTypes.ExactFileNameMatcher;
import com.intellij.openapi.fileTypes.FileTypeConsumer;
import com.intellij.openapi.fileTypes.FileTypeFactory;
import com.intellij.prettierjs.PrettierUtil;
import org.jetbrains.annotations.NotNull;

public class PrettierConfigFileTypeFactory extends FileTypeFactory {
  @Override
  public void createFileTypes(@NotNull FileTypeConsumer consumer) {
    consumer.consume(JsonFileType.INSTANCE, new ExactFileNameMatcher(PrettierUtil.RC_FILE_NAME));
  }
}
