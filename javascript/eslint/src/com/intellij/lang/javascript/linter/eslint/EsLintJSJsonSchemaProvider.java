package com.intellij.lang.javascript.linter.eslint;

import com.intellij.lang.javascript.linter.LinterJsJsonSchemaProviderBase;

import java.util.Collections;
import java.util.Set;

public class EsLintJSJsonSchemaProvider extends LinterJsJsonSchemaProviderBase {
  static final Set<String> ESLINT_JS_CONFIG = Collections.singleton(".eslintrc.js");
  private static final String SCHEMA_FILE = ".eslintrc-schema.json";

  @Override
  protected Set<String> getConfigFileNames() {
    return ESLINT_JS_CONFIG;
  }

  @Override
  protected String getSchemaFileName() {
    return SCHEMA_FILE;
  }
}
