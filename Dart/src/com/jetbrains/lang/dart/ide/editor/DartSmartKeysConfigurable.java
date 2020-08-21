// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.ide.editor;


import com.intellij.application.options.CodeCompletionOptionsCustomSection;
import com.intellij.openapi.options.ConfigurableBuilder;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.ide.codeInsight.DartCodeInsightSettings;

public class DartSmartKeysConfigurable extends ConfigurableBuilder implements CodeCompletionOptionsCustomSection {
  public DartSmartKeysConfigurable() {
    super(DartBundle.message("dart.title"));
    DartCodeInsightSettings settings = DartCodeInsightSettings.getInstance();

    checkBox(DartBundle.message("dart.smartKeys.insertDefaultArgValues.text"), () -> settings.INSERT_DEFAULT_ARG_VALUES,
             v -> settings.INSERT_DEFAULT_ARG_VALUES = v);
  }
}