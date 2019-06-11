// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package jetbrains.communicator.mock;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.util.ArrayUtilRt;
import com.intellij.util.diff.Diff;
import jetbrains.communicator.commands.FindUsersCommand;
import jetbrains.communicator.commands.SendMessageInvoker;
import jetbrains.communicator.core.Pico;
import jetbrains.communicator.core.TestFactory;
import jetbrains.communicator.core.dispatcher.LocalMessage;
import jetbrains.communicator.core.transport.TransportEvent;
import jetbrains.communicator.core.users.User;
import jetbrains.communicator.core.vfs.ProjectsData;
import jetbrains.communicator.core.vfs.VFile;
import jetbrains.communicator.ide.*;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * @author Kir Maximov
 */
public class MockIDEFacade implements IDEFacade {
  private File myDataDir;
  private ProgressIndicator myIndicator = new NullProgressIndicator();
  @NonNls
  private String myLog = "";
  private FindUsersCommand.UsersInfo myUsersInfo = new FindUsersCommand.UsersInfo();
  private LocalMessage myMessageToReturn;
  private String[] myProjects = ArrayUtilRt.EMPTY_STRING_ARRAY;
  private final Map<Cloneable,String> myFileText = new HashMap<>();
  private String myMessage;
  private String myProjectId;
  private String myProjectName;
  private boolean myAnswer;

  public MockIDEFacade() {
    myDataDir = null;
  }

  public MockIDEFacade(Class<?> testClass) {
    try {
      myDataDir = TestFactory.createDir(testClass);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public MockIDEFacade(File dataDir) {
    myDataDir = dataDir;
  }

  @Override
  public void invokeSendMessage(User[] allUsers, User[] defaultTargetUsers, String message, SendMessageInvoker sendMessageInvoker) {
    throw new UnsupportedOperationException("Not implemented in " + getClass().getName());
  }

  @Override
  public void showSearchHistoryResults(List<LocalMessage> foundMessages, User user) {
    throw new UnsupportedOperationException("Not implemented in " + getClass().getName());
  }

  @Override
  public ProjectsData getProjectsData() {
    ProjectsData projectsData = new ProjectsData();
    for (String myProject : myProjects) {
      projectsData.setProjectFiles(myProject, new VFile[0]);
    }
    return projectsData;
  }

  @Override
  public void showUserFiles(User user, ProjectsData data) {
    throw new UnsupportedOperationException("Not implemented in " + getClass().getName());
  }

  @Override
  public boolean hasFile(VFile file) {
    throw new UnsupportedOperationException("Not implemented in " + getClass().getName());
  }

  @Override
  public void open(VFile file) {
    throw new UnsupportedOperationException("Not implemented in " + getClass().getName());
  }

  @Override
  public void fillFileContents(VFile vFile) {
    vFile.setContents(myFileText.get(vFile));
  }

  @Override
  public void showDiffFor(User remoteUser, VFile vFile, String compareWith) {
    throw new UnsupportedOperationException("Not implemented in " + getClass().getName());
  }

  @Override
  public Change[] getDiff(Object[] src, Object[] dest) {
    List<MyChangeAdapter> result = new ArrayList<>();
    Diff.Change change = null;
    try {
      change = Diff.buildChanges(src, dest);
    }
    catch (Exception e) {
      return new Change[0];
    }
    while (change != null) {
      result.add(new MyChangeAdapter(change));
      change = change.link;
    }
    return result.toArray(new Change[0]);
  }

  @Override
  public String[] getProjects() {
    return myProjects;
  }

  @Override
  public FindUsersCommand.UsersInfo chooseUsersToBeAdded(List/*<User>*/ foundNewUsers, String[] availableGroups) {
    myLog += "chooseUsersToBeAdded";
    return myUsersInfo;
  }

  @Override
  public String getMessageLine(String labelText, String titleText) {
    return myMessage;
  }

  @Override
  public String getMessage(String labelText, String titleText, String optionalOKButtonText) {
    return myMessage;
  }

  public void setReturnedMessage(String message) {
    myMessage = message;
  }

  @Override
  public void showMessage(String title, String message) {
    if (Pico.isUnitTest()) {
      System.out.println(title + " : " + message);
    }
    myLog += "showMessage" + message;
  }

  @Override
  public boolean askQuestion(String title, String question) {
    return myAnswer;
  }

  @Override
  public File getCacheDir() {
    if (myDataDir == null) throw new NullPointerException();
    return myDataDir;
  }

  @Override
  public File getConfigDir() {
    if (myDataDir == null) throw new NullPointerException();
    return myDataDir;
  }

  @Override
  public Future<?> runOnPooledThread(Runnable toRun) {
    return ApplicationManager.getApplication().executeOnPooledThread(toRun);
  }

  public void setDataDir(File dataDir) {
    myDataDir = dataDir;
  }


  @Override
  public void runLongProcess(String processTitle, IDEFacade.Process process) throws CanceledException {
    try {
      process.run(myIndicator);
    } catch (Exception e) {
      if ("Canceled".equals(e.getMessage())) {
        throw new CanceledException(e);
      }
    }
  }

  public void setIndicator(ProgressIndicator indicator) {
    myIndicator = indicator;
  }


  @Override
  public LocalMessage createLocalMessageForIncomingEvent(TransportEvent event) {
    return myMessageToReturn;
  }

  @Override
  @Nullable
  public LocalMessage createLocalMessageForOutgoingEvent(OwnMessageEvent event) {
    return myMessageToReturn;
  }

  @Override
  public String getCurrentProjectId() {
    return myProjectId;
  }


  public String getAndClearLog() {
    String log = myLog;
    myLog = "";
    return log;
  }

  public void setReturnedData(FindUsersCommand.UsersInfo usersInfo) {
    myUsersInfo = usersInfo;
  }

  public void setReturnedMessage(LocalMessage message) {
    myMessageToReturn = message;
  }

  public void setReturnedProjects(String[] projectNames) {
    myProjects = projectNames;
  }

  public void setReturnedFileText(VFile vFile, String s) {
    myFileText.put(vFile, s);
  }

  public void setReturnedProjectId(String s) {
    myProjectId = s;
  }

  public void setReturnedProjectName(String projectName) {
    myProjectName = projectName;
  }

  public void setReturnedAnswer(boolean b) {
    myAnswer = b;
  }

  private static class MyChangeAdapter implements Change {
    private final Diff.Change myChange1;

    MyChangeAdapter(Diff.Change change1) {
      myChange1 = change1;
    }

    @Override
    public int getInserted() {
      return myChange1.inserted;
    }

    @Override
    public int getDeleted() {
      return myChange1.deleted;
    }

    @Override
    public int getSrcLine() {
      return myChange1.line0;
    }

    @Override
    public int getDestLine() {
      return myChange1.line1;
    }
  }
}
