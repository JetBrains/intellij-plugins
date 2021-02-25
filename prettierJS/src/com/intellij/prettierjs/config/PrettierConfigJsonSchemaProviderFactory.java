// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.prettierjs.config;

import com.intellij.lang.javascript.EmbeddedJsonSchemaFileProvider;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.prettierjs.PrettierUtil;
import com.jetbrains.jsonSchema.extension.JsonSchemaFileProvider;
import com.jetbrains.jsonSchema.extension.JsonSchemaProviderFactory;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public final class PrettierConfigJsonSchemaProviderFactory implements JsonSchemaProviderFactory, DumbAware {
  static final String SCHEMA_FILE_NAME = "prettierrc-schema.json";

  @NotNull
  @Override
  public List<JsonSchemaFileProvider> getProviders(@NotNull final Project project) {
    @NlsSafe String prettier = "Prettier";
    EmbeddedJsonSchemaFileProvider provider = new EmbeddedJsonSchemaFileProvider(SCHEMA_FILE_NAME, prettier,
                                                                                 "http://json.schemastore.org/prettierrc",
                                                                                 PrettierConfigJsonSchemaProviderFactory.class, "/") {
      @Override
      public boolean isAvailable(@NotNull VirtualFile file) {
        return PrettierUtil.isNonJSConfigFile(file);
      }
    };
    return Collections.singletonList(provider);
  }
}