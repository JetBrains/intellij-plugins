// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2;

import com.intellij.lang.typescript.tsconfig.TypeScriptConfigFileType;
import com.intellij.openapi.fileTypes.ExactFileNameMatcher;
import com.intellij.openapi.fileTypes.FileTypeConsumer;
import com.intellij.openapi.fileTypes.FileTypeFactory;
import org.jetbrains.annotations.NotNull;

public class AngularTypeScriptConfigFileTypeFactory extends FileTypeFactory {
  @Override
  public void createFileTypes(@NotNull FileTypeConsumer consumer) {
    consumer.consume(TypeScriptConfigFileType.INSTANCE,
                     new ExactFileNameMatcher("tsconfig.app.json"),
                     new ExactFileNameMatcher("tsconfig.lib.json"),
                     new ExactFileNameMatcher("tsconfig.spec.json"),
                     new ExactFileNameMatcher("tsconfig.e2e.json"));
  }
}
