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
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileSystemItem;
import com.intellij.psi.PsiManager;
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
    Project p = CommonDataKeys.PROJECT.getData(e.getDataContext());

    if (p != null) {

      final int port = getMayaCommandPort(p);

      if (port > 0) {
        String selectionText = getSelectionText(e);
        SendToMayaCommand sendCommand = new SendToMayaCommand(p, port);

        if (selectionText != null) {
          sendCommand = sendCommand.withSelectionText(selectionText);
        }
        else {
          PyFile file = getPythonFile(e);
          if (file != null) {
            sendCommand = sendCommand.withFile(file.getVirtualFile());
          }
          else {
            throw new IllegalStateException();
          }
        }
        sendCommand.run();
      }
    }
    else {
      //TODO: specify command port in settings
    }
  }

  @Nullable
  private static String getFileText(AnActionEvent e) {
    PyFile file = getPythonFile(e);
    if (file != null) {
      return file.getText();
    }

    return null;
  }

  @Nullable
  private static String getSelectionText(AnActionEvent e) {
    Editor editor = CommonDataKeys.EDITOR.getData(e.getDataContext());
    if (editor != null) {
      SelectionModel model = editor.getSelectionModel();
      return model.getSelectedText();
    }
    return null;
  }

  public void update(AnActionEvent e) {
    Presentation presentation = e.getPresentation();
    boolean enabled = false;
    String selectionText = getSelectionText(e);
    if (selectionText != null && isPythonEditor(e)) {
      if (!StringUtil.isEmpty(selectionText)) {
        enabled = true;
        presentation.setText("Send selection to Maya");
      }
    }
    else if (isPythonFile(e) || isPythonEditor(e)) {
      enabled = true;
      presentation.setText("Send file to Maya");
    }

    presentation.setEnabled(enabled);
    presentation.setVisible(enabled);
  }

  @Nullable
  private static PyFile getPythonFile(AnActionEvent e) {
    VirtualFile vFile = e.getData(PlatformDataKeys.VIRTUAL_FILE);
    Project project = CommonDataKeys.PROJECT.getData(e.getDataContext());

    if (project != null && vFile != null) {
      final PsiManager psiManager = PsiManager.getInstance(project);
      PsiFileSystemItem fsItem = vFile.isDirectory() ? psiManager.findDirectory(vFile) : psiManager.findFile(vFile);
      if (fsItem instanceof PyFile) {
        return (PyFile)fsItem;
      }
    }

    return null;
  }

  private static boolean isPythonFile(AnActionEvent e) {
    return getPythonFile(e) != null;
  }

  private static boolean isPythonEditor(AnActionEvent e) {
    Editor editor = CommonDataKeys.EDITOR.getData(e.getDataContext());
    Project project = CommonDataKeys.PROJECT.getData(e.getDataContext());

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
