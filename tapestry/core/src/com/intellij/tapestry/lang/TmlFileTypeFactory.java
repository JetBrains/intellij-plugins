package com.intellij.tapestry.lang;

import com.intellij.openapi.fileTypes.FileTypeConsumer;
import com.intellij.openapi.fileTypes.FileTypeFactory;
import org.jetbrains.annotations.NotNull;

/**
 * @author Alexey Chmutov
 */
public class TmlFileTypeFactory extends FileTypeFactory {
  public void createFileTypes(final @NotNull FileTypeConsumer consumer) {
    final TmlFileType fileType = TmlFileType.INSTANCE;
    consumer.consume(fileType, fileType.getDefaultExtension());
  }
}

