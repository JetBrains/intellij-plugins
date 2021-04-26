/*
 * Copyright 2020 Jeremy Volkman
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.protobuf.ide.editing;

import com.intellij.codeInsight.editorActions.enter.EnterBetweenBracesDelegate;

public class PbEnterBetweenBracesDelegate extends EnterBetweenBracesDelegate {
  @Override
  protected boolean isBracePair(char c1, char c2) {
    return (c1 == '{' && c2 == '}') || (c1 == '[' && c2 == ']');
  }
}
