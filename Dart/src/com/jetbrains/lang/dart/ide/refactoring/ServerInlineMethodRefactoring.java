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

import org.dartlang.analysis.server.protocol.InlineMethodFeedback;
import org.dartlang.analysis.server.protocol.InlineMethodOptions;
import org.dartlang.analysis.server.protocol.RefactoringFeedback;
import org.dartlang.analysis.server.protocol.RefactoringKind;
import org.dartlang.analysis.server.protocol.RefactoringOptions;
import org.jetbrains.annotations.NotNull;

/**
 * LTK wrapper around Analysis Server 'Inline Method' refactoring.
 */
public class ServerInlineMethodRefactoring extends ServerRefactoring {
  private final InlineMethodOptions options = new InlineMethodOptions(false, false);
  private String fullName;
  private boolean isDeclaration;

  public ServerInlineMethodRefactoring(String file, int offset, int length) {
    super("Inline Method", RefactoringKind.INLINE_METHOD, file, offset, length);
  }

  public String getFullName() {
    return fullName;
  }

  public boolean isDeclaration() {
    return isDeclaration;
  }

  public void setDeleteSource(boolean value) {
    options.setDeleteSource(value);
    setOptions(true, null);
  }

  public void setInlineAll(boolean value) {
    options.setInlineAll(value);
    setOptions(true, null);
  }

  @Override
  protected RefactoringOptions getOptions() {
    return options;
  }

  @Override
  protected void setFeedback(@NotNull RefactoringFeedback _feedback) {
    InlineMethodFeedback feedback = (InlineMethodFeedback) _feedback;
    String className = feedback.getClassName();
    String methodName = feedback.getMethodName();
    if (className != null) {
      fullName = className + "." + methodName;
    } else {
      fullName = methodName;
    }
    isDeclaration = feedback.isDeclaration();
  }
}
