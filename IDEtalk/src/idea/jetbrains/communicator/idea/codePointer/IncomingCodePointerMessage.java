/*
 * Copyright 2000-2006 JetBrains s.r.o.
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
package jetbrains.communicator.idea.codePointer;

import com.intellij.execution.filters.HyperlinkInfo;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import icons.IdetalkCoreIcons;
import jetbrains.communicator.core.Pico;
import jetbrains.communicator.core.transport.CodePointerEvent;
import jetbrains.communicator.core.vfs.CodePointer;
import jetbrains.communicator.core.vfs.VFile;
import jetbrains.communicator.ide.SendCodePointerEvent;
import jetbrains.communicator.idea.BaseIncomingLocalMessage;
import jetbrains.communicator.idea.IDEAFacade;
import jetbrains.communicator.idea.VFSUtil;
import jetbrains.communicator.util.PositionCorrector;
import jetbrains.communicator.util.StringUtil;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NonNls;

import javax.swing.*;
import java.util.Date;

/**
 * @author Kir
 */
public class IncomingCodePointerMessage extends BaseIncomingLocalMessage {
  @NonNls
  private static final Logger LOG = Logger.getLogger(IncomingCodePointerMessage.class);
  private final CodePointer myCodePointer;
  private final VFile myRemoteFile;
  private transient IDEAFacade myFacade;

  public IncomingCodePointerMessage(CodePointerEvent event, IDEAFacade facade) {
    super(event.getComment(), event.getWhen());
    myCodePointer = event.getCodePointer();
    myRemoteFile = event.getFile();
    myFacade = facade;
  }

  public IncomingCodePointerMessage(SendCodePointerEvent event, IDEAFacade facade) {
    super(event.getMessage(), new Date());
    myCodePointer = event.getCodePointer();
    myRemoteFile = event.getFile();
    myFacade = facade;
  }

  public boolean containsString(String searchString) {
    return super.containsString(searchString) || myRemoteFile.containsSearchString(searchString);
  }

  protected Icon getIcon() {
    return IdetalkCoreIcons.CodePointer;
  }

  public void outputMessage(ConsoleView consoleView) {
    printLink(consoleView);
    printComment(consoleView);
  }

  public void printLink(ConsoleView consoleView) {
    consoleView.print("   ", ConsoleViewContentType.NORMAL_OUTPUT);
    consoleView.printHyperlink(getLinkText(), new MyHyperlinkInfo());
    consoleView.print("\n", ConsoleViewContentType.NORMAL_OUTPUT);
  }

  String getLinkText() {
    return myRemoteFile.getName() + getLineText();
  }

  String getLineText() {
    if (myCodePointer.isSameLine()) {
      return ":" + (1 + myCodePointer.getLine1());
    }
    return " (" + (1 + myCodePointer.getLine1()) + ".." + (1 + myCodePointer.getLine2()) + ')';
  }

  public String getTitle() {
    return StringUtil.getMsg("code.pointer");
  }

  private LogicalPosition getLogicalPosition(int line, int column, PositionCorrector positionCorrector) {
    if (myRemoteFile.getContents() != null) {
      return new LogicalPosition(positionCorrector.getCorrectedLine(line), column);
    }
    return new LogicalPosition(line, column);
  }

  private void updateFacade() {
    if (myFacade == null) {
      myFacade = (IDEAFacade) Pico.getInstance().getComponentInstanceOfType(IDEAFacade.class);
    }
  }

  private PositionCorrector createPositionCorrector() {
    updateFacade();
    VFile localFile = (VFile) myRemoteFile.clone();
    myFacade.fillFileContents(localFile);
    return new PositionCorrector(myFacade, myRemoteFile.getContents(), localFile.getContents());
  }

  private class MyHyperlinkInfo implements HyperlinkInfo {
    public void navigate(Project project) {
      updateFacade();
      VirtualFile virtualFile = VFSUtil.getVirtualFile(myRemoteFile);
      if (virtualFile == null) {
        LOG.info("Unable to find " + myRemoteFile);
        myFacade.showMessage(StringUtil.getMsg("no.file"), StringUtil.getMsg("idea.link.nofile", myRemoteFile.getDisplayName()));
        return;
      }

      final Editor editor = openTextEditor(project, virtualFile);
      if (editor == null) {
        LOG.info("Unable to open text editor for " + virtualFile.getPresentableUrl());
        myFacade.showMessage(StringUtil.getMsg("unable.to.open"),
            StringUtil.getMsg("idea.link.noeditor", virtualFile.getPresentableUrl()));
        return;
      }

      PositionCorrector positionCorrector = createPositionCorrector();
      int startOffset = editor.logicalPositionToOffset(getStartLogicalPosition(positionCorrector));
      int endOffset = editor.logicalPositionToOffset(getEndLogicalPosition(positionCorrector));

      startOffset = moveToTextBounds(startOffset, editor.getDocument().getTextLength());
      endOffset = moveToTextBounds(endOffset, editor.getDocument().getTextLength());

      editor.getCaretModel().moveToOffset(startOffset);
      editor.getScrollingModel().scrollToCaret(ScrollType.CENTER);

      editor.getSelectionModel().setSelection(startOffset, endOffset);
    }

    private int moveToTextBounds(int offset, int textLength) {
      if (offset < 0) return 0;
      if (offset > textLength - 1) return textLength - 1;
      return offset;
    }

    private LogicalPosition getStartLogicalPosition(PositionCorrector positionCorrector) {
      return getLogicalPosition(myCodePointer.getLine1(), myCodePointer.getColumn1(), positionCorrector);
    }

    private LogicalPosition getEndLogicalPosition(PositionCorrector positionCorrector) {
      return getLogicalPosition(myCodePointer.getLine2(), myCodePointer.getColumn2(), positionCorrector);
    }

    private Editor openTextEditor(Project project, VirtualFile virtualFile) {
      Editor editor = null;
      FileEditor[] fileEditors = FileEditorManager.getInstance(project).openFile(virtualFile, true);
      for (FileEditor fileEditor : fileEditors) {
        if (fileEditor instanceof TextEditor) {
          TextEditor textEditor = (TextEditor) fileEditor;
          editor = textEditor.getEditor();
        }
      }
      return editor;
    }
  }
}
