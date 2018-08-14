// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.coldFusion.model.files;

import com.intellij.openapi.fileTypes.FileTypeConsumer;
import com.intellij.openapi.fileTypes.FileTypeFactory;
import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.NotNull;

public class CfmlFileTypeFactory extends FileTypeFactory {
  @Override
  public void createFileTypes(final @NotNull FileTypeConsumer consumer) {
    consumer.consume(CfmlFileType.INSTANCE, StringUtil.join(CfmlFileType.INSTANCE.getExtensions(), ";"));
  }
}
