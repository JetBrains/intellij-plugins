package com.intellij.prettierjs.config;

import com.intellij.lang.javascript.EmbeddedJsonSchemaFileProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.prettierjs.PrettierUtil;
import com.jetbrains.jsonSchema.extension.JsonSchemaFileProvider;
import com.jetbrains.jsonSchema.extension.JsonSchemaProviderFactory;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public final class PrettierConfigJsonSchemaProviderFactory implements JsonSchemaProviderFactory {
  private static final String SCHEMA_FILE_NAME = "prettierrc-schema.json";

  @NotNull
  @Override
  public List<JsonSchemaFileProvider> getProviders(@NotNull final Project project) {
    EmbeddedJsonSchemaFileProvider provider = new EmbeddedJsonSchemaFileProvider(SCHEMA_FILE_NAME, "Prettier",
                                                                                 "http://json.schemastore.org/prettierrc-1.8.2",
                                                                                 PrettierConfigJsonSchemaProviderFactory.class, "/") {
      @Override
      public boolean isAvailable(@NotNull VirtualFile file) {
        return PrettierUtil.isConfigFile(file);
      }
    };
    return Collections.singletonList(provider);
  }
}