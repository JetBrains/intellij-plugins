package com.intellij.tapestry.lang;

import com.intellij.openapi.fileTypes.FileTypeConsumer;
import com.intellij.openapi.fileTypes.FileTypeFactory;
import org.jetbrains.annotations.NotNull;

/**
 * @author Alexey Chmutov
 *         Date: Jun 18, 2009
 *         Time: 8:59:33 PM
 */
public class TmlFileTypeFactory extends FileTypeFactory {
  public void createFileTypes(final @NotNull FileTypeConsumer consumer) {
    final TmlFileType fileType = new TmlFileType();
    consumer.consume(fileType, fileType.getDefaultExtension());
  }
}

