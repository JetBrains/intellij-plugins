/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jetbrains.lang.dart.ide.editor;


import com.intellij.application.options.CodeCompletionOptionsCustomSection;
import com.intellij.openapi.options.ConfigurableBuilder;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.ide.codeInsight.DartCodeInsightSettings;

public class DartSmartKeysConfigurable extends ConfigurableBuilder implements CodeCompletionOptionsCustomSection {
  public DartSmartKeysConfigurable() {
    super("Dart");
    DartCodeInsightSettings settings = DartCodeInsightSettings.getInstance();

    checkBox(DartBundle.message("dart.smartKeys.insertDefaultArgValues.text"), () -> settings.INSERT_DEFAULT_ARG_VALUES,
             v -> settings.INSERT_DEFAULT_ARG_VALUES = v);
  }
}