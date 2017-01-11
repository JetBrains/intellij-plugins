package com.intellij.lang.javascript.linter.tslint.ide;

import com.intellij.lang.javascript.linter.tslint.config.TsLintConfiguration;
import com.intellij.openapi.fileTypes.ExactFileNameMatcher;
import com.intellij.openapi.fileTypes.FileTypeConsumer;
import com.intellij.openapi.fileTypes.FileTypeFactory;
import org.jetbrains.annotations.NotNull;

/**
 * @author Irina.Chernushina on 8/31/2015.
 */
public final class TsLintConfigFileTypeFactory extends FileTypeFactory {
  @Override
  public void createFileTypes(@NotNull FileTypeConsumer consumer) {
    consumer.consume(TsLintConfigFileType.INSTANCE, new ExactFileNameMatcher(TsLintConfiguration.TSLINT_JSON));
  }
}
