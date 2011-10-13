package com.google.jstestdriver.idea.assertFramework.codeInsight;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.template.Template;
import com.intellij.openapi.extensions.ExtensionPointName;

public interface JsCallTemplateContextProvider {
  ExtensionPointName<JsCallTemplateContextProvider> EP_NAME = ExtensionPointName.create("com.google.jstestdriver.idea.jsCallTemplate");

  String getCalledFunctionName();
  String getTailText();
  String getTypeText();

  Template getTemplate();

  boolean isInContext(CompletionParameters parameters);

}
