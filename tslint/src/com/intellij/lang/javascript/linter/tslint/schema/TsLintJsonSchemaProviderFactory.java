package com.intellij.lang.javascript.linter.tslint.schema;

import com.intellij.lang.javascript.EmbeddedJsonSchemaFileProvider;
import com.intellij.openapi.project.Project;
import com.jetbrains.jsonSchema.extension.JsonSchemaFileProvider;
import com.jetbrains.jsonSchema.extension.JsonSchemaProviderFactory;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;


public final class TsLintJsonSchemaProviderFactory implements JsonSchemaProviderFactory {
  private static final String TSLINT_SCHEMA_JSON = "tslint-schema.json";
  private static final String TSLINT_JSON_SCHEMA_DIR = "/tslintJsonSchema";

  @NotNull
  private static EmbeddedJsonSchemaFileProvider createProvider() {
    return new EmbeddedJsonSchemaFileProvider(TSLINT_SCHEMA_JSON, "TSLint", "http://json.schemastore.org/tslint",
                                              TsLintJsonSchemaProviderFactory.class, TSLINT_JSON_SCHEMA_DIR + '/', "tslint.json");
  }

  @NotNull
  @Override
  public List<JsonSchemaFileProvider> getProviders(@NotNull final Project project) {
    return Collections.singletonList(createProvider());
  }
}
