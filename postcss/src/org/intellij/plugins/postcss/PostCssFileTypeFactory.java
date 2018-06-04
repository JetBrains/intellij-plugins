package org.intellij.plugins.postcss;

import com.intellij.openapi.fileTypes.FileTypeConsumer;
import com.intellij.openapi.fileTypes.FileTypeFactory;
import org.jetbrains.annotations.NotNull;

public class PostCssFileTypeFactory extends FileTypeFactory {
  @Override
  public void createFileTypes(@NotNull FileTypeConsumer consumer) {
    consumer.consume(PostCssFileType.POST_CSS, PostCssFileType.DEFAULT_EXTENSION);
  }
}
