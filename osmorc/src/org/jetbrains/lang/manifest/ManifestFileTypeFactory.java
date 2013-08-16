package org.jetbrains.lang.manifest;

import com.intellij.openapi.fileTypes.FileTypeConsumer;
import com.intellij.openapi.fileTypes.FileTypeFactory;
import com.intellij.openapi.fileTypes.LanguageFileType;
import org.jetbrains.annotations.NotNull;

/**
 * @author yole
 */
public class ManifestFileTypeFactory extends FileTypeFactory {
  public final static LanguageFileType MANIFEST = new ManifestFileType();

  @Override
  public void createFileTypes(@NotNull FileTypeConsumer consumer) {
    consumer.consume(MANIFEST, "MF");
  }
}
