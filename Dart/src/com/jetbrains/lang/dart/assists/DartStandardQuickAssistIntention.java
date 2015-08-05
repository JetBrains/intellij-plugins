/*
 * Copyright 2000-2015 JetBrains s.r.o.
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
package com.jetbrains.lang.dart.assists;

import com.intellij.codeInsight.intention.HighPriorityAction;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import com.jetbrains.lang.dart.DartBundle;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

public class DartStandardQuickAssistIntention implements IntentionAction, HighPriorityAction {
  @NotNull
  @Override
  public String getFamilyName() {
    //noinspection DialogTitleCapitalization
    return DartBundle.message("dart.quick.assist.family.name");
  }

  @Nls
  @NotNull
  @Override
  public String getText() {
    return "IntentionAction stub for Dart";
  }

  @Override
  public void invoke(@NotNull Project project, Editor editor, PsiFile file) throws IncorrectOperationException {

  }

  @Override
  public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
    return false;
  }

  @Override
  public boolean startInWriteAction() {
    return false;
  }
}
