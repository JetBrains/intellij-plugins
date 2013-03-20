/*
 * Copyright 2000-2009 JetBrains s.r.o.
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
package com.jetbrains.maya;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.jetbrains.python.psi.PyFile;
import org.jetbrains.annotations.Nullable;

/**
 * @author traff
 */
public class SendToMayaAction extends AnAction {
  public SendToMayaAction() {
    super("Send to Maya");
  }

  public void actionPerformed(AnActionEvent e) {
    final String selectionText = getSelectionText(e);

    Project p = PlatformDataKeys.PROJECT.getData(e.getDataContext());

    if (p != null) {

      final int port = getMayaCommandPort(p);

      if (selectionText != null && port > 0) {
        SendToMayaCommand c = new SendToMayaCommand(p, selectionText, port);
        c.run();
      }
    }
  }

  @Nullable
  private static String getSelectionText(AnActionEvent e) {
    Editor editor = PlatformDataKeys.EDITOR.getData(e.getDataContext());
    if (editor != null) {
      SelectionModel model = editor.getSelectionModel();
      return model.getSelectedText();
    }
    return null;
  }

  public void update(AnActionEvent e) {
    boolean enabled = !StringUtil.isEmpty(getSelectionText(e)) && isPython(e);

    Presentation presentation = e.getPresentation();
    presentation.setEnabled(enabled);
    presentation.setVisible(enabled);
  }

  private static boolean isPython(AnActionEvent e) {
    Editor editor = PlatformDataKeys.EDITOR.getData(e.getDataContext());
    Project project = PlatformDataKeys.PROJECT.getData(e.getDataContext());

    if (project == null || editor == null) {
      return false;
    }

    PsiFile psi = PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument());
    return psi instanceof PyFile;
  }


  public int getMayaCommandPort(Project p) {
    return MayaSettingsProvider.getInstance(p).getPort();
  }
}
