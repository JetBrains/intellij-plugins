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

  @NotNull
  private static EmbeddedJsonSchemaFileProvider createProvider() {
    return new EmbeddedJsonSchemaFileProvider(TSLINT_SCHEMA_JSON, NAME, "http://json.schemastore.org/tslint",
                                              TsLintJsonSchemaProviderFactory.class, TSLINT_JSON_SCHEMA_DIR + '/', TslintUtil.CONFIG_FILE_NAMES);
  }

  @NotNull
  @Override
  public List<JsonSchemaFileProvider> getProviders(@NotNull final Project project) {
    return Collections.singletonList(createProvider());
  }
}
