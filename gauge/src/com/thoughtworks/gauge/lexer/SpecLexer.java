/*
 * Copyright (C) 2020 ThoughtWorks, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.thoughtworks.gauge.lexer;

import com.intellij.lexer.FlexAdapter;

/**
 * An adapter for integrating with the auto-generated _SpecLexer created by _SpecLexer.flex.
 * It is used to break .spec file text into semantic tokens.
 * <p>
 * Use of this code, and how to generate _SpecLexer from the .flex file, can be found at
 * <a href="https://plugins.jetbrains.com/docs/intellij/implementing-lexer.html">IntelliJ Platform SDK Docs</a>
 */
public final class SpecLexer extends FlexAdapter {
  public SpecLexer() {
    super(new _SpecLexer());
  }
}
