// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.metadata;

import com.intellij.openapi.fileTypes.FileNameMatcher;
import com.intellij.openapi.fileTypes.FileTypeConsumer;
import com.intellij.openapi.fileTypes.FileTypeFactory;
import org.jetbrains.annotations.NotNull;

public class MetadataJsonFileTypeFactory extends FileTypeFactory {
  @Override
  public void createFileTypes(@NotNull FileTypeConsumer consumer) {
    consumer.consume(MetadataJsonFileType.INSTANCE, new FileNameMatcher() {

      @Override
      public boolean accept(@NotNull String fileName) {
        return fileName.endsWith(".metadata.json");
      }

      @NotNull
      @Override
      public String getPresentableString() {
        return "Metadata JSON";
      }
    });
  }
}
