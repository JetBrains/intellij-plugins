// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.javascript.linter.tslint.schema;

import com.intellij.lang.javascript.EmbeddedJsonSchemaFileProvider;
import com.intellij.lang.javascript.linter.tslint.TslintUtil;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsSafe;
import com.jetbrains.jsonSchema.extension.JsonSchemaFileProvider;
import com.jetbrains.jsonSchema.extension.JsonSchemaProviderFactory;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;


public final class TsLintJsonSchemaProviderFactory implements JsonSchemaProviderFactory, DumbAware {
  private static final String TSLINT_SCHEMA_JSON = "tslint-schema.json";
  private static final String TSLINT_JSON_SCHEMA_DIR = "/tslintJsonSchema";
  private static final @NlsSafe String NAME = "TSLint";

  private static @NotNull EmbeddedJsonSchemaFileProvider createProvider() {
    return new EmbeddedJsonSchemaFileProvider(TSLINT_SCHEMA_JSON, NAME, "http://json.schemastore.org/tslint",
                                              TsLintJsonSchemaProviderFactory.class, TSLINT_JSON_SCHEMA_DIR + '/', TslintUtil.CONFIG_FILE_NAMES);
  }

  @Override
  public @NotNull List<JsonSchemaFileProvider> getProviders(final @NotNull Project project) {
    return Collections.singletonList(createProvider());
  }
}
