/*
 * Copyright (c) 2015, the Dart project authors.
 * 
 * Licensed under the Eclipse Public License v1.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.jetbrains.lang.dart.ide.refactoring;

import org.dartlang.analysis.server.protocol.*;
import org.jetbrains.annotations.NotNull;

import static com.intellij.util.ArrayUtil.toStringArray;

/**
 * LTK wrapper around Analysis Server 'Extract Local Variable' refactoring.
 */
public class ServerExtractLocalVariableRefactoring extends ServerRefactoring {
  private final ExtractLocalVariableOptions options = new ExtractLocalVariableOptions("name", true);
  private ExtractLocalVariableFeedback feedback;

  public ServerExtractLocalVariableRefactoring(String file, int offset, int length) {
    super("Extract Local Variable", RefactoringKind.EXTRACT_LOCAL_VARIABLE, file, offset, length);
  }

  @NotNull
  public int[] getCoveringExpressionLengths() {
    return feedback.getCoveringExpressionLengths();
  }

  @NotNull
  public int[] getCoveringExpressionOffsets() {
    return feedback.getCoveringExpressionOffsets();
  }

  @NotNull
  public String[] getNames() {
    return toStringArray(feedback.getNames());
  }

  public int getOccurrences() {
    return feedback.getOffsets().length;
  }

  @NotNull
  public int[] getOccurrencesLengths() {
    return feedback.getLengths();
  }

  @NotNull
  public int[] getOccurrencesOffsets() {
    return feedback.getOffsets();
  }

  @Override
  protected RefactoringOptions getOptions() {
    return options;
  }

  public void setExtractAll(boolean extractAll) {
    options.setExtractAll(extractAll);
  }

  @Override
  protected void setFeedback(@NotNull RefactoringFeedback _feedback) {
    feedback = (ExtractLocalVariableFeedback)_feedback;
  }

  public void setName(@NotNull String name) {
    options.setName(name);
    setOptions(true, null);
  }
}
