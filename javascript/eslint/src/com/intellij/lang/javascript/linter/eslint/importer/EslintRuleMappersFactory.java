package com.intellij.lang.javascript.linter.eslint.importer;

import com.intellij.openapi.extensions.ExtensionPointName;

import java.util.List;

public interface EslintRuleMappersFactory {

  ExtensionPointName<EslintRuleMappersFactory> EP_NAME = ExtensionPointName.create("com.intellij.eslint.ruleMappersFactory");

  List<EslintRuleMapper> createMappers();
}
