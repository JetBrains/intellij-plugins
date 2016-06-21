package org.intellij.plugins.postcss;

import com.intellij.openapi.fileTypes.FileTypeConsumer;
import com.intellij.openapi.fileTypes.FileTypeFactory;
import org.jetbrains.annotations.NotNull;

/**
 * Creates PostCss file types
 * Created by Ilya Bochkarev on 6/21/16.
 */
public class PostCssFileTypeFactory extends FileTypeFactory {
  @Override
  public void createFileTypes(@NotNull FileTypeConsumer consumer) {
    consumer.consume(PostCssFileType.POST_CSS, PostCssFileType.DEFAULT_EXTENSION);
  }
}
