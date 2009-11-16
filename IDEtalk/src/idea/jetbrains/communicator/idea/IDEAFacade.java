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
package jetbrains.communicator.idea;

import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.DataConstants;
import com.intellij.openapi.actionSystem.DataProvider;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.IdeFrame;
import com.intellij.openapi.wm.ex.WindowManagerEx;
import com.intellij.openapi.wm.impl.IdeFrameImpl;
import com.intellij.ui.PopupHandler;
import com.intellij.ui.TreeSpeedSearch;
import com.intellij.ui.TreeToolTipHandler;
import com.intellij.util.ArrayUtil;
import com.intellij.util.diff.Diff;
import com.intellij.util.ui.tree.TreeUtil;
import jetbrains.communicator.commands.FindUsersCommand;
import jetbrains.communicator.commands.SendMessageInvoker;
import jetbrains.communicator.core.EventVisitor;
import jetbrains.communicator.core.dispatcher.LocalMessage;
import jetbrains.communicator.core.transport.CodePointerEvent;
import jetbrains.communicator.core.transport.MessageEvent;
import jetbrains.communicator.core.transport.StacktraceEvent;
import jetbrains.communicator.core.transport.TransportEvent;
import jetbrains.communicator.core.users.User;
import jetbrains.communicator.core.vfs.ProjectsData;
import jetbrains.communicator.core.vfs.VFile;
import jetbrains.communicator.ide.*;
import jetbrains.communicator.idea.codePointer.IncomingCodePointerMessage;
import jetbrains.communicator.idea.findUsers.FindUsersDialog;
import jetbrains.communicator.idea.history.ShowHistoryDialog;
import jetbrains.communicator.idea.sendMessage.IncomingLocalMessage;
import jetbrains.communicator.idea.sendMessage.IncomingStacktraceMessage;
import jetbrains.communicator.idea.viewFiles.ViewFilesDialog;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;

/**
 * @author Kir Maximov
 */
public class IDEAFacade implements IDEFacade {
  private static final Logger LOG = Logger.getLogger(IDEAFacade.class);

  private ViewFilesDialog myViewFilesDialog;

  public void showMessage(String title, String message) {
    Messages.showMessageDialog(message, title, Messages.getInformationIcon());
  }

  public boolean askQuestion(String title, String question) {
    //noinspection HardCodedStringLiteral
    int result = JOptionPane.showOptionDialog(null,
        new JLabel("<html><body>"+question.replaceAll("\n","<br>")+"</body></html>"), title,
        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
        null, null, null);

    return result == JOptionPane.YES_OPTION;
  }

  public File getCacheDir() {
    //noinspection HardCodedStringLiteral
    File file = new File(PathManager.getSystemPath(), "ideTalk");
    file.mkdir();
    return file;
  }

  public File getConfigDir() {
    @NonNls File file = new File(PathManager.getConfigPath(), "ideTalk");
    file.mkdir();
    return file;
  }

  public FindUsersCommand.UsersInfo chooseUsersToBeAdded(List<User> foundNewUsers, String[] groups) {

    FindUsersDialog dialog = new FindUsersDialog(foundNewUsers, groups);

    dialog.show();

    if (dialog.isOK()) {
      Set<User> selectedUsers = dialog.getSelectedUsers();
      return new FindUsersCommand.UsersInfo(selectedUsers.toArray(new User[selectedUsers.size()]), dialog.getGroup());
    }

    return new FindUsersCommand.UsersInfo();
  }

  public String getMessageLine(String labelText, String titleText) {
    return Messages.showInputDialog(labelText, titleText, Messages.getQuestionIcon());
  }

  public String getMessage(String labelText, String titleText, String optionalOKButtonText) {
    GetMessageDialog dialog = new GetMessageDialog(titleText, labelText, optionalOKButtonText);
    dialog.show();
    return dialog.getEnteredText();
  }

  public Future<?> runOnPooledThread(Runnable toRun) {
    return ApplicationManager.getApplication().executeOnPooledThread(toRun);
  }

  public void runLongProcess(String processTitle, final Process process) throws CanceledException {
    try {
      ProgressManager.getInstance().runProcessWithProgressSynchronously(new Runnable() {
        public void run() {
          final ProgressIndicator progressIndicator = ProgressManager.getInstance().getProgressIndicator();
          if (progressIndicator == null) {
            process.run(new NullProgressIndicator(){
              public void checkCanceled() {
                super.checkCanceled();
                throw new ProcessCanceledException();
              }
            });
          } else {
            progressIndicator.pushState();
            process.run(new jetbrains.communicator.ide.ProgressIndicator() {
              public void setIndefinite(boolean indefinite) {
                progressIndicator.setIndeterminate(indefinite);
              }

              public void setText(String text) {
                progressIndicator.setText(text);
              }

              public void setFraction(double x) {
                progressIndicator.setFraction(x);
              }

              public void checkCanceled() {
                progressIndicator.checkCanceled();
              }
            });
            progressIndicator.popState();
          }
        }
      }, processTitle, true, null);
    } catch (ProcessCanceledException e) {
      throw new CanceledException(e);
    }
  }

  public void invokeSendMessage(User[] allUsers, User[] defaultTargetUsers, String message, SendMessageInvoker sendMessageInvoker) {

    Project project = getProject(null);
    assert project != null: "Null project when sending message";
    IDEtalkMessagesWindow messagesWindow = project.getComponent(IDEtalkMessagesWindow.class);
    if (messagesWindow != null && project.isInitialized()) {
      messagesWindow.expandToolWindow();
      for (User user : defaultTargetUsers) {
        messagesWindow.showUserTabAndRequestFocus(user);
        messagesWindow.appendInputText(user, message);
      }
    }
  }

  public void showSearchHistoryResults(List<LocalMessage> foundMessages, User user) {
    Project project = getProject(null);
    assert project != null;
    new ShowHistoryDialog(project, foundMessages, user).show();
  }

  public ProjectsData getProjectsData() {

    final ProjectsData result = new ProjectsData();

    ApplicationManager.getApplication().runReadAction(new Runnable() {
      public void run() {
        try {
          new ProjectsDataFiller(result).fillProjectsData();
        } catch (Throwable e) {
          LOG.info(e, e);
        }
      }
    });

    return result;
  }

  public String[] getProjects() {
    Project[] projects = ProjectManager.getInstance().getOpenProjects();
    List<String> result = new ArrayList<String>();
    for (Project project : projects) {
      result.add(project.getName());
    }
    return ArrayUtil.toStringArray(result);
  }

  public void showUserFiles(User user, ProjectsData data) {
    if (myViewFilesDialog == null) {
      myViewFilesDialog = new ViewFilesDialog(user, data, this) {
        protected void dispose() {
          super.dispose();
          myViewFilesDialog = null;
        }
      };
      myViewFilesDialog.show();
    }
    else {
      myViewFilesDialog.refreshData(user, data);
    }
  }

  public boolean hasFile(VFile file) {
    VirtualFile virtualFile = VFSUtil.getVirtualFile(file);
    return  virtualFile != null && virtualFile.isValid();
  }

  public void open(VFile file) {
    VirtualFile virtualFile = VFSUtil.getVirtualFile(file);
    Project project = getProject(null);
    assert project != null;
    if (virtualFile != null) {
      FileEditorManager.getInstance(project).openFile(virtualFile, false);
    }
  }

  public void fillFileContents(final VFile vFile) {
    vFile.setContents(null);
    final VirtualFile virtualFile = VFSUtil.getVirtualFile(vFile);
    if (virtualFile == null) return;

    ApplicationManager.getApplication().runReadAction(new Runnable() {
      public void run() {
        Document document = FileDocumentManager.getInstance().getDocument(virtualFile);
        if (document != null) {
          vFile.setContents(document.getText());
        }
      }
    });
  }

  public void showDiffFor(User remoteUser, VFile vFile, String compareWith) {
    Project project = getProject(null);
    assert project != null;
    VirtualFile virtualFile = VFSUtil.getVirtualFile(vFile);
    if (virtualFile != null) {
      new DiffWindowOpener(project, virtualFile, remoteUser, vFile, compareWith).showDiff();
    }
  }

  public Change[] getDiff(Object[] src, Object[] dest) {
    List<MyChangeAdapter> result = new ArrayList<MyChangeAdapter>();
    Diff.Change change = Diff.buildChanges(src, dest);
    while (change != null) {
      result.add(new MyChangeAdapter(change));
      change = change.link;
    }
    return result.toArray(new Change[result.size()]);
  }

  public LocalMessage createLocalMessageForIncomingEvent(TransportEvent event) {
    final LocalMessage[] result = new LocalMessage[1];
    event.accept(new EventVisitor(){

      @SuppressWarnings({"RefusedBequest"})
      @Override public void visitStacktraceEvent(StacktraceEvent event) {
        result[0] = new IncomingStacktraceMessage(event);
      }

      @SuppressWarnings({"RefusedBequest"})
      @Override public void visitCodePointerEvent(CodePointerEvent event) {
        result[0] = new IncomingCodePointerMessage(event, IDEAFacade.this);
      }

      @SuppressWarnings({"RefusedBequest"})
      @Override public void visitMessageEvent(MessageEvent event) {
        result[0] = new IncomingLocalMessage(event);
      }
    });
    return result[0];
  }

  @Nullable
  public LocalMessage createLocalMessageForOutgoingEvent(OwnMessageEvent event) {
    final LocalMessage[] result = new LocalMessage[1];
    event.accept(new EventVisitor() {

      @Override public void visitSendMessageEvent(SendMessageEvent event) {
        result[0] = new OutgoingLocalMessage(event.getMessage());
      }

      @Override public void visitSendCodePointerEvent(SendCodePointerEvent event) {
        result[0] = new OutgoingCodePointerLocalMessage(event);
      }
    });
    return result[0];
  }

  @Nullable
  public String getCurrentProjectId() {
    Project project = getProject(null);
    if (project != null) {
      return IdProvider.getInstance(project).getId();
    }
    return null;
  }

  public static Object getData(Component c, String dataId) {
    Object result = null;
    while (c != null) {
      if (c instanceof DataProvider) {
        DataProvider provider = (DataProvider) c;
        Object data = provider.getData(dataId);
        if (data != null) {
          result = data;
          break;
        }
      }
      c = c.getParent();
    }
    return result;
  }

  @Nullable
  public static Project getProject(Component component) {
    Project res = null;

    if (component != null) {
      res = (Project) getData(component, DataConstants.PROJECT);
    }
    else {
      IdeFrame[] frames = WindowManagerEx.getInstanceEx().getAllFrames();
      for (IdeFrame frame : frames) {
        final IdeFrameImpl eachFrame = (IdeFrameImpl)frame;
        if (eachFrame.isActive()) {
          res = eachFrame.getProject();
          if (res != null) break;
        }
      }
    }

    if (res == null) {
      Project[] openProjects = ProjectManager.getInstance().getOpenProjects();
      res = openProjects.length > 0 ? openProjects[0] : null;
    }

    return res;
  }

  public static void installPopupMenu(ActionGroup group, Component component, ActionManager actionManager) {
    if (actionManager == null) return;
    PopupHandler.installPopupHandler((JComponent) component, group, "POPUP", actionManager);
  }

  public static void installIdeaTreeActions(JTree t) {
    TreeToolTipHandler.install(t);
    TreeUtil.installActions(t);
    new TreeSpeedSearch(t);
  }

  private static class MyChangeAdapter implements Change {
    private final Diff.Change myChange1;

    MyChangeAdapter(Diff.Change change1) {
      myChange1 = change1;
    }

    public int getInserted() {
      return myChange1.inserted;
    }

    public int getDeleted() {
      return myChange1.deleted;
    }

    public int getSrcLine() {
      return myChange1.line0;
    }

    public int getDestLine() {
      return myChange1.line1;
    }
  }

  public static void main(String[] args) {
    IDEAFacade ideaFacade = new IDEAFacade();
    //noinspection HardCodedStringLiteral
    ideaFacade.askQuestion("title", "some\n<i>question</i>");
  }
}
