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

package com.thoughtworks.gauge;

import com.intellij.codeInsight.editorActions.enter.EnterHandlerDelegate;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actionSystem.EditorActionHandler;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.thoughtworks.gauge.util.GaugeUtil;
import org.jetbrains.annotations.NotNull;

final class GaugeEnterHandlerDelegate implements EnterHandlerDelegate {
  @Override
  public Result preprocessEnter(@NotNull PsiFile psiFile, @NotNull Editor editor,
                                @NotNull Ref<Integer> ref,
                                @NotNull Ref<Integer> ref1, @NotNull DataContext dataContext,
                                EditorActionHandler editorActionHandler) {
    return Result.Continue;
  }

  @Override
  public Result postProcessEnter(@NotNull PsiFile psiFile, Editor editor, @NotNull DataContext dataContext) {
    Document document = editor.getDocument();
    VirtualFile file = FileDocumentManager.getInstance().getFile(document);
    if (file != null && GaugeUtil.isGaugeFile(file)) {
      FileDocumentManager.getInstance().saveDocumentAsIs(document);
    }
    return null;
  }
}
