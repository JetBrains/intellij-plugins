// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities.metadata;

import com.intellij.openapi.fileTypes.FileNameMatcher;
import com.intellij.openapi.fileTypes.FileTypeConsumer;
import com.intellij.openapi.fileTypes.FileTypeFactory;
import org.jetbrains.annotations.NotNull;

public class Angular2MetadataFileTypeFactory extends FileTypeFactory {
  @Override
  public void createFileTypes(@NotNull FileTypeConsumer consumer) {
    consumer.consume(Angular2MetadataFileType.INSTANCE, new FileNameMatcher() {

      @Override
      public boolean accept(@NotNull String fileName) {
        return false;
      }

      @NotNull
      @Override
      public String getPresentableString() {
        return "Angular 2 Metadata JSON";
      }
    });
  }
}
