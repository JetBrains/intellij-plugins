/*
 * Copyright (c) 2014, the Dart project authors.
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

import com.intellij.openapi.vfs.VirtualFile;
import org.dartlang.analysis.server.protocol.InlineLocalVariableFeedback;
import org.dartlang.analysis.server.protocol.RefactoringFeedback;
import org.dartlang.analysis.server.protocol.RefactoringKind;
import org.dartlang.analysis.server.protocol.RefactoringOptions;
import org.jetbrains.annotations.NotNull;

/**
 * LTK wrapper around Analysis Server 'Inline Local' refactoring.
 */
public class ServerInlineLocalRefactoring extends ServerRefactoring {
  private String variableName;
  private int occurrences;

  public ServerInlineLocalRefactoring(VirtualFile file, int offset, int length) {
    super("Inline Local Variable", RefactoringKind.INLINE_LOCAL_VARIABLE, file, offset, length);
  }

  public int getOccurrences() {
    return occurrences;
  }

  public String getVariableName() {
    return variableName;
  }

  @Override
  protected RefactoringOptions getOptions() {
    return null;
  }

  @Override
  protected void setFeedback(@NotNull RefactoringFeedback _feedback) {
    InlineLocalVariableFeedback feedback = (InlineLocalVariableFeedback)_feedback;
    variableName = feedback.getName();
    occurrences = feedback.getOccurrences();
  }
}
