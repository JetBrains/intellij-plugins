package com.intellij.lang.javascript.linter.tslint.schema;

import com.intellij.lang.javascript.EmbeddedJsonSchemaFileProvider;
import com.intellij.util.containers.ContainerUtil;
import com.jetbrains.jsonSchema.extension.JsonSchemaFileProvider;
import com.jetbrains.jsonSchema.extension.JsonSchemaProviderFactory;
import org.jetbrains.annotations.NotNull;

import java.util.List;


public final class TsLintJsonSchemaProviderFactory implements JsonSchemaProviderFactory {

  private static final String TSLINT_SCHEMA_JSON = "tslint-schema.json";
  private static final String TSLINT_JSON_SCHEMA_DIR = "/tslintJsonSchema";

  @NotNull
  private final List<JsonSchemaFileProvider> myProviders;

  public TsLintJsonSchemaProviderFactory() {
    EmbeddedJsonSchemaFileProvider provider = new EmbeddedJsonSchemaFileProvider(TSLINT_SCHEMA_JSON,
                                                                                 TsLintJsonSchemaProviderFactory.class,
                                                                                 TSLINT_JSON_SCHEMA_DIR + '/',
                                                                                 "tslint.json");
    myProviders = ContainerUtil.createMaybeSingletonList(provider);
  }

  @NotNull
  @Override
  public List<JsonSchemaFileProvider> getProviders() {
    return myProviders;
  }
}
