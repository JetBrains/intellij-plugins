package org.jetbrains.plugins.cucumber.psi;

import com.intellij.openapi.fileTypes.FileTypeFactory;
import com.intellij.openapi.fileTypes.FileTypeConsumer;
import org.jetbrains.annotations.NotNull;

/**
 * @author yole
 */
public class CucumberFileTypeFactory extends FileTypeFactory {
  public void createFileTypes(@NotNull FileTypeConsumer consumer) {
    consumer.consume(GherkinFileType.INSTANCE, "feature");
  }
}
