package org.osmorc.manifest;

import com.intellij.openapi.fileTypes.FileTypeConsumer;
import com.intellij.openapi.fileTypes.FileTypeFactory;
import com.intellij.openapi.fileTypes.LanguageFileType;
import org.jetbrains.annotations.NotNull;
import org.osmorc.manifest.lang.ManifestFileType;

/**
 * @author yole
 */
public class ManifestFileTypeFactory extends FileTypeFactory {
  public final static LanguageFileType MANIFEST = new ManifestFileType();

  public void createFileTypes(@NotNull FileTypeConsumer consumer) {
    consumer.consume(MANIFEST, "MF");
  }
}
