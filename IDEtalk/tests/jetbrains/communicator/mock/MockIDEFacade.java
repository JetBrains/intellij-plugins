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
package jetbrains.communicator.mock;

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
  private String[] myProjects = new String[0];
  private Map<Cloneable,String> myFileText = new HashMap<Cloneable, String>();
  private String myMessage;
  private String myProjectId;
  private String myProjectName;
  private boolean myAnswer;

  public MockIDEFacade() {
    myDataDir = null;
  }

  public MockIDEFacade(Class<? extends Object> testClass) {
    try {
      myDataDir = TestFactory.createDir(testClass);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public MockIDEFacade(File dataDir) {
    myDataDir = dataDir;
  }

  public void invokeSendMessage(User[] allUsers, User[] defaultTargetUsers, String message, SendMessageInvoker sendMessageInvoker) {
    throw new UnsupportedOperationException("Not implemented in " + getClass().getName());
  }

  public void showSearchHistoryResults(List<LocalMessage> foundMessages, User user) {
    throw new UnsupportedOperationException("Not implemented in " + getClass().getName());
  }

  public ProjectsData getProjectsData() {
    ProjectsData projectsData = new ProjectsData();
    for (String myProject : myProjects) {
      projectsData.setProjectFiles(myProject, new VFile[0]);
    }
    return projectsData;
  }

  public void showUserFiles(User user, ProjectsData data) {
    throw new UnsupportedOperationException("Not implemented in " + getClass().getName());
  }

  public boolean hasFile(VFile file) {
    throw new UnsupportedOperationException("Not implemented in " + getClass().getName());
  }

  public void open(VFile file) {
    throw new UnsupportedOperationException("Not implemented in " + getClass().getName());
  }

  public void fillFileContents(VFile vFile) {
    vFile.setContents(myFileText.get(vFile));
  }

  public void showDiffFor(User remoteUser, VFile vFile, String compareWith) {
    throw new UnsupportedOperationException("Not implemented in " + getClass().getName());
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

  public String[] getProjects() {
    return myProjects;
  }

  public FindUsersCommand.UsersInfo chooseUsersToBeAdded(List/*<User>*/ foundNewUsers, String[] availableGroups) {
    myLog += "chooseUsersToBeAdded";
    return myUsersInfo;
  }

  public String getMessageLine(String labelText, String titleText) {
    return myMessage;
  }

  public String getMessage(String labelText, String titleText, String optionalOKButtonText) {
    return myMessage;
  }

  public void setReturnedMessage(String message) {
    myMessage = message;
  }

  public void showMessage(String title, String message) {
    if (Pico.isUnitTest()) {
      System.out.println(title + " : " + message);
    }
    myLog += "showMessage" + message;
  }

  public boolean askQuestion(String title, String question) {
    return myAnswer;
  }

  public File getCacheDir() {
    if (myDataDir == null) throw new NullPointerException();
    return myDataDir;
  }

  public File getConfigDir() {
    if (myDataDir == null) throw new NullPointerException();
    return myDataDir;
  }

  public void setDataDir(File dataDir) {
    myDataDir = dataDir;
  }


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


  public LocalMessage createLocalMessageForIncomingEvent(TransportEvent event) {
    return myMessageToReturn;
  }

  @Nullable
  public LocalMessage createLocalMessageForOutgoingEvent(OwnMessageEvent event) {
    return myMessageToReturn;
  }

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
}
